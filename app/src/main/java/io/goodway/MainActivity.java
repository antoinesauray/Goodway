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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.goodway.model.User;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.view.fragment.MainFragment;
import io.goodway.view.fragment.SearchFragment;


/**
 * Created by sauray on 14/03/15.
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{


    private Toolbar toolbar;
    public static final int DEPARTURE=1, DESTINATION=2;
    private int nbFriendRequests;

    private String token;
    private User user;
    private FragmentManager fragmentManager;

    private Address departure, destination;


    private GoogleApiClient googleApiClient;
    private Location userLocation;

    private static final int ACCESS_FINE_LOCATION=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        token = getIntent().getExtras().getString("token");
        user = getIntent().getExtras().getParcelable("user");
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        fragmentManager = getFragmentManager();

        switchToFragment(MainFragment.newInstance(getIntent().getExtras()));

        GoodwayHttpClientPost.countFriendRequests(this, new Action<Integer>() {
            @Override
            public void action(Integer e) {
                nbFriendRequests = e;
                if (e > 0) {

                }
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {

            }
        }, token);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
            } else {
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
            }
        } else {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
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
        switchToFragment(SearchFragment.newInstance(b));
    }

    private void switchToFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(0, R.animator.exit, R.animator.enter_pop, R.animator.exit_pop);
        fragmentTransaction.addToBackStack(fragment.getTag());
        fragmentTransaction.replace(R.id.fragment, fragment);
        fragmentTransaction.commit();
    }
    private void switchToFragmentWithExitAnim(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.enter_pop, R.animator.exit_pop, R.animator.enter_pop, R.animator.exit_pop);
        fragmentTransaction.addToBackStack(fragment.getTag());
        fragmentTransaction.replace(R.id.fragment, fragment);
        fragmentTransaction.commit();
    }

    public void setFrom(Address address) {
        this.departure = address;
        Bundle b= new Bundle();
        b.putParcelable("departure", departure);
        b.putParcelable("destination", destination);
        b.putAll(getIntent().getExtras());
        switchToFragmentWithExitAnim(MainFragment.newInstance(b));
    }

    public void setTo(Address address) {
        this.destination = address;
        Bundle b= new Bundle();
        b.putParcelable("departure", departure);
        b.putParcelable("destination", destination);
        b.putAll(getIntent().getExtras());
        switchToFragmentWithExitAnim(MainFragment.newInstance(b));
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                fragmentManager.popBackStack();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        fragmentManager.popBackStack();
    }

    public void fabClick(View v){
        if((departure==null || destination == null) && userLocation==null){
            if(departure!=null){
                Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.your_location_not_available, Snackbar.LENGTH_LONG)
                        .setAction(R.string.select, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                changeLocation(DEPARTURE);
                            }
                        }).show();
            }
            else{
                Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.no_to, Snackbar.LENGTH_LONG)
                        .setAction(R.string.select, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                changeLocation(DESTINATION);
                            }
                        }).show();
            }
        }
        else {
            if(departure==null){departure=new Address(getString(R.string.your_location), userLocation.getLatitude(), userLocation.getLongitude());}
            if(destination==null){destination=new Address(getString(R.string.your_location), userLocation.getLatitude(), userLocation.getLongitude());;}
            Intent i = new Intent(this, WayActivity.class);
            i.putExtra("user", user);
            i.putExtra("token", token);
            i.putExtra("departure", departure);
            i.putExtra("destination", destination);
            startActivity(i);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
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

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.contacts:
                Intent i = new Intent(this, FriendsActivity.class);
                i.putExtra("token", token);
                i.putExtra("user", user);
                i.putExtra("nbFriendRequests", nbFriendRequests);
                startActivity(i);
                break;
            case R.id.places:
                Intent i2 = new Intent(this, PlacesActivity.class);
                i2.putExtra("token", token);
                i2.putExtra("user", user);
                i2.putExtra("nbFriendRequests", nbFriendRequests);
                startActivity(i2);
                break;
            case R.id.groups:
                Intent i3 = new Intent(this, UserGroupsActivity.class);
                i3.putExtra("token", token);
                i3.putExtra("user", user);
                startActivity(i3);
                break;
            case R.id.share:
                String message = "Téléchargez Goodway, l'application de déplacement moderne";
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, message);

                startActivity(Intent.createChooser(share, "Partager"));
                break;
        }
    }

}
