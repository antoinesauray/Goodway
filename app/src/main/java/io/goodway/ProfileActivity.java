package io.goodway;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;

import io.goodway.model.User;
import io.goodway.model.callback.AddressSelected;
import io.goodway.model.callback.FinishCallback;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.model.network.UploadDocument;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.UserLocation;
import io.goodway.view.ImageTrans_CircleTransform;


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

    private String mail, password;
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
        self = extras.getBoolean("self", false);
        toolbar = (Toolbar) findViewById(R.id.mapToolbar);
        toolbar.setTitle(user.getName());

        fab = (FloatingActionButton) findViewById(R.id.fab);

        avatar = (ImageView) findViewById(R.id.avatar);
        ((TextView)findViewById(R.id.title)).setText(user.getTitle(this));
        Picasso.with(this)
                .load(user.getAvatar())
                .error(R.mipmap.ic_person_white_48dp)
                .resize(200, 200)
                .centerCrop()
                .transform(new ImageTrans_CircleTransform())
                .into(avatar);

        if(self){avatar.setOnClickListener(this);}

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);

        bundle = new Bundle();
        bundle.putString("mail", mail);
        bundle.putString("password", password);
        bundle.putBoolean("self", self);
        bundle.putParcelable("user", user);
        addressFragment = new AddressFragment();
        newAddressFragment = new NewAddressFragment();
        addressFragment.setArguments(bundle);
        newAddressFragment.setArguments(bundle);

        if(!self && !user.isFriend()){
            fab.setVisibility(View.VISIBLE);
        }

        addressFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft1 = fragmentManager.beginTransaction();
        ft1.addToBackStack(addressFragment.getTag());
        ft1.replace(R.id.addressContainer, addressFragment);
        ft1.commitAllowingStateLoss();

        activityFragment = new ActivityFragment();
        activityFragment.setArguments(bundle);
        FragmentTransaction ft2 = fragmentManager.beginTransaction();
        ft2.replace(R.id.activityContainer, activityFragment);
        ft2.commitAllowingStateLoss();

        current = addressFragment;
    }

    public void fabClick(View v){
        Log.d("requesting", "requesting friend with id="+user.getId());
        GoodwayHttpsClient.requestFriend(this, new Action<Boolean>() {
            @Override
            public void action(Boolean e) {
                fab.setVisibility(View.INVISIBLE);
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {
                Toast.makeText(ProfileActivity.this, R.string.failure, Toast.LENGTH_SHORT).show();
            }
        }, mail, password, user.getId());
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

                    newAddressFragment.name.setText(name);
                    newAddressFragment.shared.setChecked(shared);
                    if(newAddressFragment.location!=null) {
                        newAddressFragment.location.setName(name);
                        newAddressFragment.location.setA_name(address.getName());
                        newAddressFragment.location.setShared(shared);
                        newAddressFragment.location.setLatitude(address.getLatitude());
                        newAddressFragment.location.setLongitude(address.getLongitude());
                    }
                    else{
                        newAddressFragment.location = new UserLocation(name, address.getName(), user.getFirstName(), address.getLatitude(), address.getLongitude(), shared);
                    }
                    if(data.getBooleanExtra("insert", false)){
                        final ProgressDialog pd = new ProgressDialog(ProfileActivity.this);
                        pd.setMessage(getString(R.string.add_address));
                        pd.setProgressStyle(pd.STYLE_SPINNER);
                        pd.show();
                        GoodwayHttpsClient.addLocation(this, new Action<Boolean>() {
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
                        }, mail, password, newAddressFragment.location);
                    }
                case FILE_SELECT_CODE:
                    Uri selectedImage = data.getData();
                    try {
                        new UploadDocument(this, decodeUri(this, selectedImage, 100),  ProfileActivity.this.avatar, selectedImage.getLastPathSegment(), mail, password).execute();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
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
            if(addressFragment.item != null && newAddressFragment.location != null && !addressFragment.item.equals(newAddressFragment.location)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.apply_modifications)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                newAddressFragment.location.setName(newAddressFragment.name.getText().toString());
                                newAddressFragment.location.setShared(newAddressFragment.shared.isChecked());
                                final ProgressDialog pd = new ProgressDialog(ProfileActivity.this);
                                pd.setMessage(getString(R.string.add_address));
                                pd.setProgressStyle(pd.STYLE_SPINNER);
                                pd.show();
                                currentAsyncTask = GoodwayHttpsClient.updateLocation(ProfileActivity.this, new Action<Boolean>() {
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
                                }, mail, password, newAddressFragment.location);

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

    public void addAddress(View v){
        bundle.remove("location");
        newAddressFragment.location = null;
        addressFragment.item=null;
        switchToNewAdress(bundle);
    }
    public void setAddress(View v){
        if(newAddressFragment.name.getText().toString().trim().length() > 0) {
            Intent i = new Intent(this, SetLocationActivity.class);
            i.putExtra("s_name", newAddressFragment.name.getText().toString());
            i.putExtra("shared", newAddressFragment.shared.isChecked());
            if(newAddressFragment.location == null ) {
                i.putExtra("insert", true);
            }
            startActivityForResult(i, SET_ADDRESS);
        }
        else{
            Toast.makeText(ProfileActivity.this, "Veuillez rentrer un nom", Toast.LENGTH_SHORT).show();
        }
    }

    public void switchToNewAdress(Bundle bundle){
        newAddressFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.addToBackStack(newAddressFragment.getTag());
        fragmentTransaction.replace(R.id.addressContainer, newAddressFragment);
        fragmentTransaction.commitAllowingStateLoss();
        current = newAddressFragment;
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

    public static class AddressFragment extends Fragment{
        public static final String TAG = "address";
        String mail, password;
        LinearLayout locations;
        boolean self;
        User user;
        UserLocation item;

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    R.layout.fragment_addresse, container, false);
            Bundle extras = getArguments();
            mail = extras.getString("mail");
            password = extras.getString("password");
            self = extras.getBoolean("self");
            user = extras.getParcelable("user");

            locations = (LinearLayout) rootView.findViewById(R.id.locations);

            if(self){
                GoodwayHttpsClient.getSelfLocations(getActivity(), new Action<UserLocation>() {
                    @Override
                    public void action(UserLocation e) {
                        Log.d("adding address", "adding address:"+e.toString());
                        addUserLocation(e);
                    }
                }, new ErrorAction() {
                    @Override
                    public void action(int length) {
                        if(length==0){
                            View addAddress = getLayoutInflater(null).inflate(R.layout.view_add_address, null);
                            locations.addView(addAddress);
                        }
                        else{
                            View notFound = getLayoutInflater(null).inflate(R.layout.view_way_not_found, null);
                            ((TextView)notFound.findViewById(R.id.message)).setText(R.string.unavailable);
                            locations.addView(notFound);
                        }
                    }
                }, new FinishCallback() {
                    @Override
                    public void action(int length) {
                        View addAddress = getLayoutInflater(null).inflate(R.layout.view_add_address, null);
                        locations.addView(addAddress);
                    }
                }, mail, password, user.getFirstName());
            }
            else if(user.isFriend()){
                GoodwayHttpsClient.getUserLocations(getActivity(), new Action<UserLocation>() {
                    @Override
                    public void action(UserLocation e) {
                        Log.d("adding address", "adding address:" + e.toString());
                        addUserLocation(e).setOnClickListener(null);
                    }
                }, new ErrorAction() {
                    @Override
                    public void action(int length) {
                        if (length <0) {
                            View notFound = getLayoutInflater(null).inflate(R.layout.view_way_not_found, null);
                            ((TextView) notFound.findViewById(R.id.message)).setText(R.string.unavailable);
                            locations.addView(notFound);
                        }
                    }
                }, null, mail, password, user.getFirstName(), user.getId());
            }
            else{
                View notFound = getLayoutInflater(null).inflate(R.layout.view_not_friend, null);
                locations.addView(notFound);
            }
            return rootView;
        }

        private View addUserLocation(final UserLocation userLocation){
            View location = getActivity().getLayoutInflater().inflate(R.layout.view_location, null);
            location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle b = new Bundle();
                    b.putParcelable("location", userLocation);
                    item = userLocation;
                    ((ProfileActivity) getActivity()).switchToNewAdress(b);
                }
            });
            ((TextView)location.findViewById(R.id.s_name)).setText(userLocation.getName());
            ((TextView)location.findViewById(R.id.name)).setText(userLocation.getA_name());
            locations.addView(location);
            return location;
        }
    }

    public static class NewAddressFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, TextWatcher {
        public static final String TAG = "newaddress";
        String mail, password;
        EditText name;
        CheckBox shared;
        Button set;
        boolean self;
        User user;
        Toolbar toolbar;
        UserLocation location;
        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    R.layout.fragment_new_address, container, false);
            Bundle extras = getArguments();
            mail = extras.getString("mail");
            password = extras.getString("password");
            self = extras.getBoolean("self");
            user = extras.getParcelable("user");

            toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
            name = (EditText) rootView.findViewById(R.id.name);
            shared = (CheckBox) rootView.findViewById(R.id.shared);
            set = (Button) rootView.findViewById(R.id.set);
            toolbar.setTitle("");

            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
            ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            setHasOptionsMenu(true);

            actionBar.setHomeButtonEnabled(true);
            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.user_location, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            switch (id) {
                case R.id.action_delete:
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.confirm)
                            .setMessage(R.string.confirm_delete)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    final ProgressDialog pd = new ProgressDialog(getActivity());
                                    pd.setMessage(getString(R.string.deleting));
                                    pd.setProgressStyle(pd.STYLE_SPINNER);
                                    pd.show();
                                    GoodwayHttpsClient.deleteLocation(getActivity(), new Action<Boolean>() {
                                        @Override
                                        public void action(Boolean e) {
                                            pd.dismiss();
                                            ((ProfileActivity) getActivity()).popStackBack();
                                        }
                                    }, new ErrorAction() {
                                        @Override
                                        public void action(int length) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), R.string.failure, Toast.LENGTH_SHORT).show();
                                        }
                                    }, ((ProfileActivity)getActivity()).mail, ((ProfileActivity)getActivity()).password, location);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                    return true;
            }

            return false;
        }

        @Override
        public void onResume(){
            super.onResume();
            Bundle extras = getArguments();
            UserLocation tmp = extras.getParcelable("location");

            if(tmp!=null){
                location = new UserLocation(tmp);
                name.setText(location.getName());
                shared.setChecked(location.shared());
                set.setText(R.string.change_address);
                shared.setOnCheckedChangeListener(this);
                name.addTextChangedListener(this);
            }
            else if(location!=null){
                name.setText(location.getName());
                shared.setChecked(location.shared());
                set.setText(R.string.change_address);
                shared.setOnCheckedChangeListener(this);
                name.addTextChangedListener(this);
            }
            else{
                shared.setOnCheckedChangeListener(null);
                name.removeTextChangedListener(this);
                name.setText("");
                shared.setChecked(true);
                set.setText(R.string.add_address);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            location.setShared(isChecked);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            location.setName(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

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
                View notFound = getLayoutInflater(null).inflate(R.layout.view_not_friend, null);
                recent_activity.addView(notFound);
            }
            return rootView;
        }
    }
}
