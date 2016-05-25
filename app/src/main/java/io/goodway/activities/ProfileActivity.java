package io.goodway.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;

import io.goodway.R;
import io.goodway.model.User;
import io.goodway.model.callback.AddressSelected;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.UserLocation;
import io.goodway.view.ImageTrans_CircleTransform;
import io.goodway.view.fragment.nested.AddressFragment;
import io.goodway.view.fragment.nested.NewAddressFragment;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class ProfileActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, AddressSelected, View.OnClickListener {

    // ----------------------------------- Model
    /**
     * Unique identifier for this activity
     */
    private static final String TAG = "STOP_ACTIVITY";

    public static final int SET_ADDRESS=1;
    // ----------------------------------- UI

    /**
     * Toolbar widget
     */
    private Toolbar toolbar;
    private User user;
    private String token;

    private LinearLayout locations;

    private boolean self;

    private Fragment activityFragment, current;
    private NewAddressFragment newAddressFragment;
    private AddressFragment addressFragment;

    private Bundle bundle;
    private AsyncTask currentAsyncTask;

    private static final int FILE_SELECT_CODE = 2, READ_EXTERNAL_STORAGE=3;
    private FloatingActionButton fab;
    private ImageView avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Bundle extras = this.getIntent().getExtras();
        user = extras.getParcelable("user");
        token = extras.getString("token");
        self = extras.getBoolean("self", false);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(user.getName());

        fab = (FloatingActionButton) findViewById(R.id.fab);

        avatar = (ImageView) findViewById(R.id.avatar);
        ((TextView)findViewById(R.id.title)).setText(user.getTitle(this));
        if(!user.getAvatar().isEmpty()) {
            Picasso.with(this)
                    .load(user.getAvatar())
                    .error(R.mipmap.ic_person_white_48dp)
                    .resize(200, 200)
                    .centerCrop()
                    .transform(new ImageTrans_CircleTransform())
                    .into(avatar);
        }


        if(self){avatar.setOnClickListener(this);}

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);


        bundle = extras;
        addressFragment = new AddressFragment();
        newAddressFragment = new NewAddressFragment();
        addressFragment.setArguments(bundle);
        newAddressFragment.setArguments(bundle);

        if(!self && !user.isFriend()){
            fab.setVisibility(View.VISIBLE);
        }

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft1 = fragmentManager.beginTransaction();
        ft1.addToBackStack(addressFragment.getTag());
        ft1.replace(R.id.addressContainer, AddressFragment.newInstance(bundle));
        ft1.commitAllowingStateLoss();

        activityFragment = new ActivityFragment();
        activityFragment.setArguments(bundle);
        FragmentTransaction ft2 = fragmentManager.beginTransaction();
        ft2.replace(R.id.activityContainer, activityFragment);
        ft2.commitAllowingStateLoss();

    }

    public void fabClick(View v){
        Log.d("requesting", "requesting friend with id="+user.getId());
        /*
        GoodwayHttpClientPost.requestFriend(this, new Action<Boolean>() {
            @Override
            public void success(Boolean e) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        }, new ErrorAction() {
            @Override
            public void success(int length) {
                Toast.makeText(ProfileActivity.this, R.string.failure, Toast.LENGTH_SHORT).show();
            }
        }, token, user.getId());
        */
    }

    private void popStackBack(){
        getSupportFragmentManager().popBackStack();
        current = addressFragment;
    }

    @Override
    public void onPause(){
        super.onPause();
        if(currentAsyncTask!=null){currentAsyncTask.cancel(true);}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.empty_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                checkBeforeLeaving();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode){
                case SET_ADDRESS:
                    newAddressFragment.getArguments().remove("location");
                    Address address = data.getParcelableExtra("address");
                    String name = data.getStringExtra("s_name");
                    boolean shared = data.getBooleanExtra("shared", false);

                    newAddressFragment.getName().setText(name);
                    newAddressFragment.getShared().setChecked(shared);
                    if(newAddressFragment.getLocation()!=null) {
                        newAddressFragment.getLocation().setName(name);
                        newAddressFragment.getLocation().setA_name(address.getName());
                        newAddressFragment.getLocation().setShared(shared);
                        newAddressFragment.getLocation().setLatitude(address.getLatitude());
                        newAddressFragment.getLocation().setLongitude(address.getLongitude());
                    }
                    else{
                        newAddressFragment.setLocation(new UserLocation(name, address.getName(), address.getLatitude(), address.getLongitude(), shared));
                    }
                    if(data.getBooleanExtra("insert", false)){
                        final ProgressDialog pd = new ProgressDialog(ProfileActivity.this);
                        pd.setMessage(getString(R.string.add_address));
                        pd.setProgressStyle(pd.STYLE_SPINNER);
                        pd.show();
                        GoodwayHttpClientPost.addLocation(this, new Action<Boolean>() {
                            @Override
                            public void action(Boolean e) {
                                pd.dismiss();
                            }
                        }, new ErrorAction() {
                            @Override
                            public void action(int length) {
                                pd.dismiss();
                                Toast.makeText(ProfileActivity.this, R.string.failure, Toast.LENGTH_SHORT).show();
                            }
                        }, token, newAddressFragment.getLocation());
                    }
                case FILE_SELECT_CODE:
                    Uri selectedImage = data.getData();
                    /*
                    try {
                        new UploadDocument(this, decodeUri(this, selectedImage, 100),  ProfileActivity.this.avatar, selectedImage.getLastPathSegment(), token).execute();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    */
            }
        }
    }
    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize)
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth
                , height_tmp = o.outHeight;
        int scale = 1;

        while(true) {
            if(width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }

    @Override
    public void onBackPressed(){
        if(current==newAddressFragment){
            checkBeforeLeaving();
        }
        else{
            if(self){
                Intent returnIntent = new Intent();
                returnIntent.putExtra("user", user);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
            else{
                finish();
            }
        }
    }

    private void checkBeforeLeaving(){
        if(current==addressFragment){
            this.finish();
        }
        else if (current==newAddressFragment){
            if(addressFragment.getItem() != null && newAddressFragment.getLocation() != null && !addressFragment.getItem().equals(newAddressFragment.getLocation())) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.apply_modifications)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                newAddressFragment.getLocation().setName(newAddressFragment.getName().getText().toString());
                                newAddressFragment.getLocation().setShared(newAddressFragment.getShared().isChecked());
                                final ProgressDialog pd = new ProgressDialog(ProfileActivity.this);
                                pd.setMessage(getString(R.string.add_address));
                                pd.setProgressStyle(pd.STYLE_SPINNER);
                                pd.show();

                                currentAsyncTask = GoodwayHttpClientPost.updateLocation(ProfileActivity.this, new Action<Boolean>() {
                                    @Override
                                    public void action(Boolean e) {
                                        pd.dismiss();
                                        popStackBack();
                                    }
                                }, new ErrorAction() {
                                    @Override
                                    public void action(int length) {
                                        pd.dismiss();
                                        Toast.makeText(ProfileActivity.this, R.string.failure, Toast.LENGTH_SHORT).show();
                                    }
                                }, token, newAddressFragment.getLocation());


                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                popStackBack();
                            }
                        })
                        .show();
            }
            else{
                popStackBack();
            }
        }
    }

    @Override
    public void action(Address address) {

    }

    public void setAddress(View v){
        if(newAddressFragment.getName().getText().toString().trim().length() > 0) {
            Intent i = new Intent(this, SetLocationActivity.class);
            i.putExtra("s_name", newAddressFragment.getName().getText().toString());
            i.putExtra("shared", newAddressFragment.getShared().isChecked());
            if(newAddressFragment.getLocation() == null ) {
                i.putExtra("insert", true);
            }
            startActivityForResult(i, SET_ADDRESS);
        }
        else{
            Toast.makeText(ProfileActivity.this, "Veuillez rentrer un nom", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            READ_EXTERNAL_STORAGE);
                }
            }
            else{
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, ProfileActivity.FILE_SELECT_CODE);
            }
        }
        else {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, ProfileActivity.FILE_SELECT_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, ProfileActivity.FILE_SELECT_CODE);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public static class ActivityFragment extends Fragment {
        public static final String TAG = "address";
        String mail, password;
        LinearLayout recent_activity;
        boolean self;
        User user;

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    R.layout.fragment_activity_container, container, false);
            recent_activity = (LinearLayout) rootView.findViewById(R.id.recent_activity);
            self = getArguments().getBoolean("self");
            user = getArguments().getParcelable("user");
            if(self || user.isFriend()){
                View location = getActivity().getLayoutInflater().inflate(R.layout.view_way_not_found, null);
                ((TextView)location.findViewById(R.id.message)).setText(R.string.unavailable);
                recent_activity.addView(location);
            }
            else{
                View notFound = getActivity().getLayoutInflater().inflate(R.layout.view_not_friend, null);
                recent_activity.addView(notFound);
            }
            return rootView;
        }
    }
}
