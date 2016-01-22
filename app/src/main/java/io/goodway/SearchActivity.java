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
import android.support.v7.app.ActionBar;
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
import io.goodway.view.fragment.TripFragment;


/**
 * Created by sauray on 14/03/15.
 */
public class SearchActivity extends AppCompatActivity{


    private Toolbar toolbar;
    public static final int DEPARTURE=1, DESTINATION=2;

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
        setContentView(R.layout.activity_search);

        token = getIntent().getExtras().getString("token");
        user = getIntent().getExtras().getParcelable("user");
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        fragmentManager = getFragmentManager();

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(0, R.animator.exit, R.animator.enter_pop, R.animator.exit_pop);
        fragmentTransaction.replace(R.id.fragment, TripFragment.newInstance(getIntent().getExtras()));
        fragmentTransaction.commit();
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

    public void switchToFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.enter, R.animator.exit, R.animator.enter_pop, R.animator.exit_pop);
        fragmentTransaction.addToBackStack(fragment.getTag());
        fragmentTransaction.replace(R.id.fragment, fragment);
        fragmentTransaction.commit();
    }
    public void switchToFragmentWithExitAnim(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.enter_pop, R.animator.exit_pop, R.animator.enter_pop, R.animator.exit_pop);
        fragmentTransaction.replace(R.id.fragment, fragment);
        fragmentTransaction.commit();
    }


    public void setFrom(Address address) {
        this.departure = address;
        Bundle b= new Bundle();
        b.putParcelable("departure", departure);
        b.putParcelable("destination", destination);
        b.putAll(getIntent().getExtras());
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        switchToFragmentWithExitAnim(TripFragment.newInstance(b));
    }

    public void setTo(Address address) {
        this.destination = address;
        Bundle b= new Bundle();
        b.putParcelable("departure", departure);
        b.putParcelable("destination", destination);
        b.putAll(getIntent().getExtras());
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        switchToFragmentWithExitAnim(TripFragment.newInstance(b));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if(fragmentManager.getBackStackEntryCount()>0) {
            fragmentManager.popBackStack();
        }
        else{
            finish();
        }
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

}
