package io.goodway;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.view.fragment.MainFragment;
import io.goodway.view.fragment.SearchFragment;


/**
 * Created by sauray on 14/03/15.
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{

    private GoogleApiClient googleApiClient;
    private boolean openedState;
    private SupportMapFragment mapFragment;
    private FloatingActionButton floatingActionButton;
    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;

    private LinearLayout bottom;

    private Location userLocation;

    private Address from, to;
    private String token;

    private Button fromButton, toButton;

    public static final int DEPARTURE=1, DESTINATION=2;

    private static final int ACCESS_FINE_LOCATION=1;

    private Fragment current;
    private SearchFragment search;
    private MainFragment main;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        Bundle extras = getIntent().getExtras();
        token = extras.getString("token");
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            ACCESS_FINE_LOCATION);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
            else{
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
            }
        }
        else{
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        bottom = (LinearLayout) findViewById(R.id.bottom);

        fromButton = (Button) findViewById(R.id.from);
        toButton = (Button) findViewById(R.id.to);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setBaselineAlignBottom(true);
        floatingActionButton.setOnClickListener(this);

        LatLng latLng = null;
        if (extras.getParcelable("DEPARTURE") != null) {
            from = extras.getParcelable("DEPARTURE");

            fromButton.setText(extras.getParcelable("DEPARTURE").toString());
            //latLng = new LatLng(a.getLatitude(), a.getLongitude());
        }

        if (extras.getParcelable("DESTINATION") != null) {
            to = extras.getParcelable("DESTINATION");
            toButton.setText(to.toString());
            //latLng = new LatLng(a.getLatitude(), a.getLongitude());
        }

        search = new SearchFragment();
        main = new MainFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.enter, R.animator.exit, R.animator.enter, R.animator.exit);
        fragmentTransaction.addToBackStack(main.getTag());
        fragmentTransaction.add(R.id.fragment, main);
        fragmentTransaction.commit();
        current = main;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    googleApiClient = new GoogleApiClient.Builder(this)
                            .addApi(LocationServices.API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public String toString() {
        return "Bottom Sheet";
    }

    public void setFrom(Address from) {
        this.from = from;
        Log.d("setfrom", "setfrom: " + from.toString());
        fromButton.setText(from.toString());
        Log.d("setTo", "setTo : " + from.getLatitude() + ";" + from.getLongitude());
    }

    public void setTo(Address to) {
        this.to = to;
        Log.d("setfrom", "setfrom: " + to.toString());
        toButton.setText(to.toString());
        Log.d("setTo", "setTo : " + to.getLatitude() + ";" + to.getLongitude());
        /*
*/
    }


    @Override
    public void onResume() {
        super.onResume();
        if(googleApiClient!=null){googleApiClient.connect();}
    }

    @Override
    public void onPause() {
        super.onPause();
        if (googleApiClient!=null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            userLocation = LocationServices.FusedLocationApi.getLastLocation(
                    googleApiClient);
            if(userLocation!=null) {
                Geocoder gcd = new Geocoder(this, Locale.getDefault());
                List<android.location.Address> addresses = null;
                try {
                    addresses = gcd.getFromLocation(userLocation.getLatitude(), userLocation.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        GoodwayHttpClientPost.updateMyCity(this, new Action<Boolean>() {
                            @Override
                            public void action(Boolean e) {

                            }
                        }, null, token, addresses.get(0).getLocality());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(from != null){
            Button from = (Button) (findViewById(R.id.from));
            from.setText(getString(R.string.your_location) + " (" + getString(R.string.unavailable) + ")");
        }
        else if (to != null){
            Button to = (Button) (findViewById(R.id.to));
            to.setText(getString(R.string.your_location)+" ("+getString(R.string.unavailable)+")");
        }
    }

    @Override
    public void onClick(View v) {
        if(this.from == null && this.to==null){
            Snackbar.make(coordinatorLayout, R.string.no_to, Snackbar.LENGTH_LONG)
                    .setAction(R.string.select, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MainActivity.this.changeLocation(DESTINATION);
                        }
                    }).show();
        }
        else{
            if(this.from==null && userLocation!=null){
                Log.d("setting user location", "setting user location");
                this.from = new Address(getString(R.string.your_location), userLocation.getLatitude(), userLocation.getLongitude());
            }
            else if (this.to == null & userLocation!=null){
                Log.d("setting user location", "setting user location");
                this.to = new Address(getString(R.string.your_location), userLocation.getLatitude(), userLocation.getLongitude());
            }

            if(this.from==null){
                Snackbar.make(coordinatorLayout, R.string.your_location_not_available, Snackbar.LENGTH_LONG)
                        .setAction(R.string.select, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MainActivity.this.changeLocation(DEPARTURE);
                            }
                        }).show();
            }
            else if(this.to==null){
                Snackbar.make(coordinatorLayout, R.string.your_location_not_available, Snackbar.LENGTH_LONG)
                        .setAction(R.string.select, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MainActivity.this.changeLocation(DESTINATION);
                            }
                        }).show();
            }
            else{
                Intent intent = new Intent(this, WayActivity.class);
                intent.putExtra("FROM", this.from);
                intent.putExtra("TO", this.to);

                Log.d(from.toString(), "from");
                Log.d(to.toString(), "to");
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                    //ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                    startActivity(intent);
                }
                else{
                    startActivity(intent);
                }
            }

        }
    }
    public void changeLocation(View v){
        int request=0;
        switch (v.getId()) {
            case R.id.from:
                request = DEPARTURE;
                break;
            case R.id.to:
                request = DESTINATION;
                break;
        }
        changeLocation(request);
    }

    public void changeLocation(int request){
        Bundle b = new Bundle();
        b.putInt("REQUEST", request);
        b.putString("token", token);
        switchToSearch(b);
    }

    private void switchFragment(Fragment fragment, Bundle bundle)
    {
        if(bundle!=null) {
            if (bundle.getParcelable("DEPARTURE") == null) {
                bundle.putParcelable("DEPARTURE", from);
            }
            if (bundle.getParcelable("DESTINATION") == null) {
                bundle.putParcelable("DESTINATION", to);
            }
            Log.d("fragment with bundle", "fragment with bundle");
            fragment.setArguments(bundle);
        }
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.enter, R.animator.exit, R.animator.enter, R.animator.exit);
        fragmentTransaction.addToBackStack(fragment.getTag());
        fragmentTransaction.add(R.id.fragment, fragment);
        fragmentTransaction.commit();
        current = fragment;
    }

    public void switchToSearch(Bundle bundle){
        if(current==search) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.enter, R.animator.exit, R.animator.enter, R.animator.exit);
            fragmentTransaction.remove(search);
            fragmentTransaction.addToBackStack(search.getTag());
            fragmentTransaction.add(R.id.fragment, search);
            fragmentTransaction.commit();
            current=search;
        }
        else {
            switchFragment(search, bundle);
        }
        //tabLayout.setVisibility(View.VISIBLE);
        //toolbar.setLogo(null);
    }
    public void switchToMain(Bundle bundle, int request){
        //switchFragment(main, bundle);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStack();
        current = main;
    }
}
