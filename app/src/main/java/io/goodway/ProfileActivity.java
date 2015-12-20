package io.goodway;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.goodway.model.User;
import io.goodway.model.callback.AddressSelected;
import io.goodway.model.callback.FinishCallback;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.UserLocation;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class ProfileActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, AddressSelected{

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

    private Fragment addressFragment, current;
    private NewAddressFragment newAddressFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Bundle extras = this.getIntent().getExtras();
        user = extras.getParcelable("USER");
        self = extras.getBoolean("SELF", false);
        toolbar = (Toolbar) findViewById(R.id.mapToolbar);
        toolbar.setTitle(user.getName());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);

        Bundle bundle = new Bundle();
        bundle.putString("mail", mail);
        bundle.putString("password", password);
        bundle.putBoolean("self", self);
        bundle.putParcelable("user", user);
        addressFragment = new AddressFragment();
        newAddressFragment = new NewAddressFragment();
        addressFragment.setArguments(bundle);
        newAddressFragment.setArguments(bundle);
        switchAddressFragment(addressFragment, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(current==addressFragment){
                    this.finish();
                }
                else if (current==newAddressFragment){
                    switchAddressFragment(addressFragment, null);
                }

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
                    Address address = data.getParcelableExtra("address");
                    String name = data.getStringExtra("s_name");
                    Boolean shared = data.getBooleanExtra("shared", false);
                    UserLocation userLocation = new UserLocation(address, name, shared);
                    newAddressFragment.name.setText(null);
                    newAddressFragment.shared.setChecked(true);
                    // send online
                    final ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setMessage(getString(R.string.add_address));
                    dialog.setProgressStyle(dialog.STYLE_SPINNER);
                    dialog.show();
                    GoodwayHttpsClient.addLocation(this, new Action<Boolean>() {
                        @Override
                        public void action(Boolean e) {
                            dialog.dismiss();
                            switchAddressFragment(addressFragment, null);
                        }
                    }, new ErrorAction() {
                        @Override
                        public void action(int length) {
                            dialog.dismiss();
                            Toast.makeText(ProfileActivity.this, R.string.failure, Toast.LENGTH_SHORT).show();
                        }
                    }, mail, password, userLocation);

                    break;
            }
        }

    }

    @Override
    public void onBackPressed(){
        if(self){
            Intent returnIntent = new Intent();
            returnIntent.putExtra("USER", user);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
        else{
            finish();
        }
    }

    @Override
    public void action(Address address) {

    }

    public void addAddress(View v){
        switchAddressFragment(newAddressFragment, null);
    }
    public void setAddress(View v){
        if(newAddressFragment.name.getText().toString().trim().length() > 0) {
            Intent i = new Intent(this, SetLocationActivity.class);
            i.putExtra("s_name", newAddressFragment.name.getText().toString());
            i.putExtra("shared", newAddressFragment.shared.isChecked());
            startActivityForResult(i, SET_ADDRESS);
        }
        else{
            Toast.makeText(ProfileActivity.this, "Veuillez rentrer un nom", Toast.LENGTH_SHORT).show();
        }
    }

    private void switchAddressFragment(Fragment fragment, Bundle bundle)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.addressContainer, fragment);
        fragmentTransaction.commitAllowingStateLoss();
        current = fragment;
    }

    public static class AddressFragment extends Fragment{
        public static final String TAG = "address";
        String mail, password;
        LinearLayout locations;
        boolean self;
        User user;

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
                            ((TextView)notFound.findViewById(R.id.message)).setText(R.string.not_available);
                            locations.addView(notFound);
                        }
                    }
                }, new FinishCallback() {
                    @Override
                    public void action() {
                        View addAddress = getLayoutInflater(null).inflate(R.layout.view_add_address, null);
                        locations.addView(addAddress);
                    }
                }, mail, password, user.getFirstName());
            }
            else if(user.isFriend()){

            }
            else{
                rootView.findViewById(R.id.not_friend).setVisibility(View.VISIBLE);
            }
            return rootView;
        }

        private void addUserLocation(final UserLocation userLocation){
            View location = getActivity().getLayoutInflater().inflate(R.layout.view_location, null);
            location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), userLocation.getName(), Toast.LENGTH_SHORT).show();
                }
            });
            ((TextView)location.findViewById(R.id.s_name)).setText(userLocation.getName());
            ((TextView)location.findViewById(R.id.name)).setText(userLocation.getA_name());
            locations.addView(location, 1);
        }
    }

    public static class NewAddressFragment extends Fragment{
        public static final String TAG = "newaddress";
        String mail, password;
        EditText name;
        CheckBox shared;
        Button set;
        boolean self;
        User user;
        Toolbar toolbar;
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
            actionBar.setHomeButtonEnabled(true);
            return rootView;
        }
    }
}
