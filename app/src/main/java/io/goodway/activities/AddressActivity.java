package io.goodway.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import io.goodway.R;
import io.goodway.navitia_android.UserLocation;
import io.goodway.view.fragment.nested.AddressFragment;
import io.goodway.view.fragment.nested.NewAddressFragment;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class AddressActivity extends AppCompatActivity{

    // ----------------------------------- Model

    public static final int SET_ADDRESS=1;

    private FragmentManager fragmentManager;
    private Bundle bundle;
    private Toolbar toolbar;

    private boolean shared;
    private String name;
    private UserLocation location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        fragmentManager = getFragmentManager();
        bundle = new Bundle();
        bundle.putAll(getIntent().getExtras());
        bundle.putBoolean("self", true);
        bundle.putBoolean("preview", false);
        switchToFragment(AddressFragment.newInstance(bundle));


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    public void switchToFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(0, R.animator.exit, R.animator.enter_pop, R.animator.exit_pop);
        fragmentTransaction.addToBackStack(fragment.getTag());
        fragmentTransaction.replace(R.id.fragment, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed(){
        if(fragmentManager.getBackStackEntryCount()>1){
            fragmentManager.popBackStack();
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode){
                case SET_ADDRESS:
                    /*
                    Address address = data.getParcelableExtra("address");
                    String name = data.getStringExtra("s_name");
                    boolean shared = data.getBooleanExtra("shared", false);

                    switchToFragment(AddressFragment.);

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
                            public void success(Boolean e) {
                                pd.dismiss();
                            }
                        }, new ErrorAction() {
                            @Override
                            public void success(int length) {
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
                    */
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //
                // onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void addAddress(View v){

        bundle.remove("location");
        switchToFragment(NewAddressFragment.newInstance(getIntent().getExtras()));
    }
    public void setAddress(View v){
        if(name.trim().length() > 0) {
            Intent i = new Intent(this, SetLocationActivity.class);
            i.putExtra("s_name", name);
            i.putExtra("shared", shared);
            if(location == null ) {
                i.putExtra("insert", true);
            }
            startActivityForResult(i, SET_ADDRESS);
        }
        else{
            Toast.makeText(AddressActivity.this, "Veuillez rentrer un nom", Toast.LENGTH_SHORT).show();
        }
    }

    public void setShared(boolean shared){
        this.shared = shared;
    }
    public void setName(String name){
        this.name = name;
    }

    public void setLocation(UserLocation location){
        this.location = location;
    }
}
