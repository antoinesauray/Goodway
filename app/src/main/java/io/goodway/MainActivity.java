package io.goodway;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import io.goodway.model.Event;
import io.goodway.model.User;
import io.goodway.navitia_android.Address;
import io.goodway.view.fragment.BottomFragment;


/**
 * The main activity of the program
 * @author Antoine Sauray
 * @version 2.0
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    // ----------------------------------- UI

    /**
     * These variables are used to get result from other activities.
     *
     * @see
     */
    private static final int FROM_LOCATION = 1, TO_LOCATION = 2, EVENT_REQUEST =3;

    private static final String TAG = "HOME_ACTIVITY";
    /**
     * Displays the modes available for selection
     */
    private ListView mDrawerList;
    /**
     * Toolbar widget
     */
    private Toolbar toolbar;
    /**
     * The map
     */
    private GoogleMap googleMap;
    /**
     * The fragmentView which contains the map
     */
    private MapFragment mapFragment;
    /**
     * The user interface for the current mode selected
     */
    private View ui;

    private NavigationView navigationView;

    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton floatingActionButton;
    private View fragmentView;
    private BottomFragment fragment;
    private DrawerLayout drawerLayout;

    private FusedLocationProviderApi fusedLocationProviderApi;
    private GoogleApiClient googleApiClient;

    // ----------------------------------- Model
    /**
     * Provides markers on a marker. The key is the marker title attribute
     */
    private Address from, to;
    private Location userLocation;

    private User currentUser;

    // ----------------------------------- Constants
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkGooglePlayServices();
        setContentView(R.layout.activity_main);

        //from = new Address(R.string.your_location, R.mipmap.ic_home_black_24dp, AddressType.POSITION);

        fusedLocationProviderApi = LocationServices.FusedLocationApi;
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Bundle extras = this.getIntent().getExtras();
        currentUser = extras.getParcelable("USER");


        toolbar = (Toolbar) findViewById(R.id.toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();
        actionBar.setDisplayShowTitleEnabled(false);
        toolbar.setLogo(getDrawable(R.drawable.goodway_text_very_small));


        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        fragmentView =  findViewById(R.id.fragment);
        fragment = (BottomFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        //((TextView)navigationView.findViewById(R.id.name)).setText(currentUser.getName());
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setBaselineAlignBottom(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.friends:
                        Intent i = new Intent(MainActivity.this, FriendsActivity.class);
                        i.putExtra("USER", currentUser);
                        startActivity(i);
                        break;
                    case R.id.events:
                        Intent i2 = new Intent(MainActivity.this, EventsActivity.class);
                        i2.putExtra("USER", currentUser);
                        startActivityForResult(i2, EVENT_REQUEST);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleApiClient.connect();
        if(from!=null){Log.d("from=" + from.getLatitude() + ";" + from.getLongitude(), "from=" + from.getName(this));}
        if(to!=null){Log.d("to=" + to.getLatitude() + ";" + to.getLongitude(), "to=" + to.getName(this));}
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    protected void onRestoreInstanceState (Bundle savedInstanceState){
        from = savedInstanceState.getParcelable("FROM");
        to = savedInstanceState.getParcelable("TO");

        if(from != null){
            TextView from = (TextView) fragmentView.getRootView().findViewById(R.id.from);
            from.setText(this.from.getName(this));
            from.setAlpha(1f);
        }
        if(to != null){
            TextView to = (TextView) fragmentView.getRootView().findViewById(R.id.to);
            to.setText(this.to.getName(this));
            to.setAlpha(1f);
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable("FROM", from);
        outState.putParcelable("TO", to);
    }

    public void changeLocation(View v){
        changeLocation(v.getId());
    }
    public void changeLocation(int viewId){
        //Intent i = new Intent(this, SearchActivity.class);
        try {
            PlacePicker.IntentBuilder intentBuilder =
                    new PlacePicker.IntentBuilder();
            LatLngBounds bounds = null;
            // Start the intent by requesting a result,
            // identified by a request code.
            int code = -1;
            switch (viewId){
                case R.id.from:
                    //intent.putExtra("FROM", true);
                    code = FROM_LOCATION;
                    if(from==null){bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;}
                    else{
                        bounds = LatLngBounds.builder().include(new LatLng(from.getLatitude(), from.getLongitude())).build();
                    }
                    break;
                case R.id.to:
                    //intent.putExtra("TO", false);
                    code = TO_LOCATION;
                    if(to==null){bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;}
                    else{bounds = LatLngBounds.builder().include(new LatLng(to.getLatitude(), to.getLongitude())).build();}
                    break;
            }
            intentBuilder.setLatLngBounds(bounds);
            Intent intent = intentBuilder.build(this);
            startActivityForResult(intent, code);

        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            // ...
            e.printStackTrace();
        }

    /*
        if(viewId == R.id.speechRecognition){
            i.putExtra("SPEECH", true);
        }
        else{
            i.putExtra("SPEECH", false);
        }
*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EVENT_REQUEST){
            if(resultCode == RESULT_OK){
                Log.d("EVENT_REQUEST", "request code");
                Event event = data.getExtras().getParcelable("EVENT");
                setDestination(new Address(event.getName(), event.getLatitude(), event.getLongitude()));
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        }
        else{
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                Address address = new Address(place.getName().toString(), place.getLatLng().latitude, place.getLatLng().longitude);
                switch (requestCode) {
                    case FROM_LOCATION:
                        Log.d("FROM_LOCATION", "request code");
                        setStart(address);
                        break;
                    case TO_LOCATION:
                        setDestination(address);
                        Log.d("TO_LOCATION", "request code");
                        break;
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setPadding(0, 0, 0, fragment.getView().getHeight());
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setOnMyLocationChangeListener(this);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(47, 2))      // Sets the center of the map to Mountain View
                .zoom(14)                   // Sets the zoom
                        //.tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    public void toolbarClick(View v) {

        Intent i = new Intent(this, SearchActivity.class);

        if(v.getId() == R.id.speechRecognition){
            i.putExtra("SPEECH", true);
        }
        else{
            i.putExtra("SPEECH", false);
        }

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this,
                    findViewById(R.id.appBar), "search");
            MainActivity.this.startActivity(i, options.toBundle());
        }
        else{
            startActivity(i);
        }
    }

    public void swap(View v){
        Log.d("swap", "swap");
        TextView from = (TextView) fragmentView.getRootView().findViewById(R.id.from);
        String fromText = from.getText().toString();
        float fromAlpha = from.getAlpha();
        TextView to = (TextView) fragmentView.getRootView().findViewById(R.id.to);
        from.setText(to.getText().toString());
        from.setAlpha(to.getAlpha());
        to.setText(fromText);
        to.setAlpha(fromAlpha);
    }

    private void openBottomSheet(){
        fragment.setOpenedState(true);
        ObjectAnimator translationTop = ObjectAnimator.ofFloat(fragmentView, "translationY", fragmentView.getHeight(), 0);
        translationTop.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                fragmentView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        translationTop.setDuration(200);
        translationTop.start();
    }

    private void closeBottomSheet(){
        fragment.setOpenedState(false);
        ObjectAnimator translationBack = ObjectAnimator.ofFloat(fragmentView, "translationY", 0, fragmentView.getHeight());
        translationBack.setDuration(200);
        translationBack.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                fragmentView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        translationBack.start();
    }

    public void fabClick(View v){
        if(fragmentView.getVisibility()==View.GONE){
            openBottomSheet();
        }
        else{
            if(this.from == null && this.to==null){
                Snackbar.make(coordinatorLayout, R.string.no_to, Snackbar.LENGTH_LONG)
                        .setAction(R.string.select, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                changeLocation(R.id.to);
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
                                    changeLocation(R.id.from);
                                }
                            }).show();
                }
                else if(this.to==null){
                    Snackbar.make(coordinatorLayout, R.string.your_location_not_available, Snackbar.LENGTH_LONG)
                            .setAction(R.string.select, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    changeLocation(R.id.to);
                                }
                            }).show();
                }
                else{
                    Intent intent = new Intent(MainActivity.this, WayActivity.class);
                    intent.putExtra("FROM", this.from);
                    intent.putExtra("TO", this.to);

                    Log.d(from.toString(), "from");
                    Log.d(to.toString(), "to");
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this);
                        startActivity(intent, options.toBundle());
                    }
                    else{
                        startActivity(intent);
                    }
                }

            }
        }
    }

    public void drawerHeaderClick(View v){
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra("USER", currentUser);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this);
            startActivity(intent, options.toBundle());
        }
        else{
            startActivity(intent);
        }
    }

    private boolean checkGooglePlayServices() {
        final int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            Log.e(TAG, GooglePlayServicesUtil.getErrorString(status));

            // ask user to update google play services.
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 1);
            dialog.show();
            return false;
        } else {
            Log.i(TAG, GooglePlayServicesUtil.getErrorString(status));
            // google play services is updated.
            //your code goes here...
            return true;
        }
    }

    @Override
    public void onMyLocationChange(Location location) {
        userLocation = location;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }

    private void setStart(Address adress){
        this.from = adress;
        Log.d("setStart", "setStart : " + adress.getLatitude() + ";" + adress.getLongitude());
        TextView from = (TextView) fragmentView.getRootView().findViewById(R.id.from);
        from.setText(adress.getName(this));
        from.setAlpha(1f);
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(this.from.getLatitude(), this.from.getLongitude()))
                        //.alpha(0.8f)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                .title("Départ"));

        if(to != null){
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(to.getLatitude(), to.getLongitude()))
                            //.alpha(0.8f)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title("Destination"));
            setAppropriateZoom(new LatLng(this.from.getLatitude(), this.from.getLongitude()), new LatLng(this.to.getLatitude(), this.to.getLongitude()));
        }
        else if(userLocation!=null){
            setAppropriateZoom(new LatLng(this.from.getLatitude(), this.from.getLongitude()), new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude()));
        }


    }

    private void setDestination(Address adress){
        this.to = adress;
        Log.d("setDestination", "setDestination : " + adress.getLatitude() + ";" + adress.getLongitude());
        TextView to = (TextView) (fragmentView.getRootView().findViewById(R.id.to));
        to.setText(adress.getName(this));
        to.setAlpha(1f);

        googleMap.clear();

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(this.to.getLatitude(), this.to.getLongitude()))
                        //.alpha(0.8f)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title("Destination"));

        if(from != null){
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(from.getLatitude(), from.getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .title("Départ"));
            setAppropriateZoom(new LatLng(this.from.getLatitude(), this.from.getLongitude()), new LatLng(this.to.getLatitude(), this.to.getLongitude()));
        }
        else if(userLocation!=null){
            Log.d("userLocation", "userLocation");
            setAppropriateZoom(new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude()), new LatLng(this.to.getLatitude(), this.to.getLongitude()));
        }
        else{
            Log.d("userLocation=null", "userLocation=null");
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(this.to.getLatitude(), this.to.getLongitude()))      // Sets the center of the map to Mountain View
                    .zoom(14)                   // Sets the zoom// Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }
    private void setAppropriateZoom(LatLng from, LatLng to){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Log.d(from.toString(), "from latlng");
        Log.d(to.toString(), "to latlng");
        builder.include(from);
        builder.include(to);
        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.animateCamera(cu);
    }

    @Override
    public void onConnected(Bundle bundle) {
        userLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()))      // Sets the center of the map to Mountain View
                .zoom(14)                   // Sets the zoom
                        //.tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
/*
        if (from != null) {
                TextView from = (TextView) (fragmentView.getRootView().findViewById(R.id.from));
                if(this.from.getType() == AddressType.POSITION) {
                    if(userLocation != null){
                        from.setText(getString(R.string.your_location));
                        this.from.setLatitude(userLocation.getLatitude());
                        this.from.setLongitude(userLocation.getLongitude());
                        setStart(this.from);
                    }
                    else{
                        from.setText(getString(R.string.your_location)+" ("+getString(R.string.not_available)+")");
                    }
                }
                else{
                    from.setText(this.from.getName(this));
                    setStart(this.from);
                }

            } else if (to != null) {
                TextView to = (TextView) (fragmentView.getRootView().findViewById(R.id.to));
                if(this.to.getType() == AddressType.POSITION) {

                    if(userLocation != null){
                        to.setText(getString(R.string.your_location));
                        this.to.setLatitude(userLocation.getLatitude());
                        this.to.setLongitude(userLocation.getLongitude());
                        setDestination(this.to);
                    }
                    else{
                        to.setText(getString(R.string.your_location)+" ("+getString(R.string.not_available)+")");
                    }

                }
                else {
                    to.setText(this.to.getName(this));
                    setStart(this.to);
                }
        }
        */
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(from != null){
            TextView from = (TextView) (fragmentView.getRootView().findViewById(R.id.from));
            from.setText(getString(R.string.your_location)+" ("+getString(R.string.not_available)+")");
        }
        else if (to != null){
            TextView to = (TextView) (fragmentView.getRootView().findViewById(R.id.to));
            to.setText(getString(R.string.your_location)+" ("+getString(R.string.not_available)+")");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        userLocation = location;
        /*
        if(from != null && this.from.getType() == AddressType.POSITION){
            TextView from = (TextView) (fragmentView.getRootView().findViewById(R.id.from));
            from.setText(getString(R.string.your_location));
            this.from.setLatitude(userLocation.getLatitude());
            this.from.setLongitude(userLocation.getLongitude());
            setStart(this.from);
        }
        else if (to != null && this.to.getType() == AddressType.POSITION){
            TextView to = (TextView) (fragmentView.getRootView().findViewById(R.id.to));
            to.setText(getString(R.string.your_location));
            this.to.setLatitude(userLocation.getLatitude());
            this.to.setLongitude(userLocation.getLongitude());
            setDestination(this.to);
        }
        */
    }
}
