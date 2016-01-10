package io.goodway;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.goodway.model.User;
import io.goodway.model.network.GoodwayHttpClientGet;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.view.fragment.MainFragment;
import io.goodway.view.fragment.SearchFragment;


/**
 * Created by sauray on 14/03/15.
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{

    private GoogleApiClient googleApiClient;
    private FloatingActionButton floatingActionButton;
    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;

    private Location userLocation;

    private Address from, to;
    private String token;
    private User user;
    private int nbFriendRequests;

    private Button fromButton, toButton;
    private Spinner startOrEnd;

    private FragmentManager fragmentManager;

    private Button time, date;
    private Calendar departureTime, today;

    public static final int DEPARTURE=1, DESTINATION=2;
    public static final int FROM_LOCATION = 1, TO_LOCATION = 2, EVENT_REQUEST =3, SETLOCATION=4, PROFILE=5, FRIENDS=6;

    private static final int ACCESS_FINE_LOCATION=1;

    // ----------------------------------- Constants
    private static final int MAIN=1, SEARCH=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer){

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

        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();
        actionBar.setDisplayShowTitleEnabled(false);
        toolbar.setLogo(R.drawable.goodway_text_very_small);

        Bundle extras = getIntent().getExtras();
        token = extras.getString("token");
        user = extras.getParcelable("user");
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

        fromButton = (Button) findViewById(R.id.from);
        toButton = (Button) findViewById(R.id.to);
        startOrEnd = (Spinner) findViewById(R.id.spinner);

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


        fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.enter, R.animator.exit, R.animator.enter, R.animator.exit);
        fragmentTransaction.addToBackStack(MainFragment.TAG);
        fragmentTransaction.replace(R.id.fragment, MainFragment.newInstance(null));
        fragmentTransaction.commit();

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        ((TextView)navigationView.getHeaderView(0).findViewById(R.id.name)).setText(user.getName());
        ((TextView)navigationView.getHeaderView(0).findViewById(R.id.version)).setText(getString(R.string.version) + " " + getVersionInfo());

        Log.d("avatar", "avatar" + user.getAvatar());

        GoodwayHttpClientPost.countFriendRequests(this, new Action<Integer>() {
            @Override
            public void action(Integer e) {
                nbFriendRequests = e;
                if (e > 0) {
                    navigationView.getMenu().findItem(R.id.friends).setTitle(getString(R.string.friends) + " (" + e + ")");
                }
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {

            }
        }, token);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.friends:
                        Intent i = new Intent(MainActivity.this, FriendsActivity.class);
                        i.putExtra("user", user);
                        i.putExtra("token", token);
                        i.putExtra("nbFriendRequests", nbFriendRequests);
                        startActivityForResult(i, MainActivity.FRIENDS);
                        break;
                    case R.id.groups:
                        Intent i2 = new Intent(MainActivity.this, UserGroupsActivity.class);
                        i2.putExtra("user", user);
                        i2.putExtra("token", token);
                        startActivity(i2);
                        break;
                }
                return false;
            }
        });

        time = (Button) findViewById(R.id.time);
        date = (Button) findViewById(R.id.date);

        time.setOnClickListener(this);
        date.setOnClickListener(this);

        departureTime =  Calendar.getInstance();
        today = Calendar.getInstance();

        String dateStr = (new SimpleDateFormat("yyyyMMdd HHmmss")).format(departureTime.getTime());
        dateStr = dateStr.replaceAll("\\s+", "T");
        String[] times = Address.splitIso8601(dateStr);
        time.setText(times[3] + "h" + times[4]);
        if(departureTime.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) && departureTime.get(Calendar.MONTH) == today.get(Calendar.MONTH) && departureTime.get(Calendar.YEAR) == today.get(Calendar.YEAR)){
            date.setText(getString(R.string.today));
        }
        else{
            date.setText(departureTime.get(Calendar.DAY_OF_MONTH)
                    + " "+departureTime.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                    + " "+departureTime.get(Calendar.YEAR));
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }
    public void drawerHeaderClick(View v){
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("token", token);
        intent.putExtra("self", true);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this);
            startActivityForResult(intent, PROFILE);
            //startActivityForResult(intent, PROFILE, options.toBundle());
        } else {
            startActivityForResult(intent, PROFILE);
        }
    }

    public String getVersionInfo() {
        PackageInfo packageInfo;
        try {
            packageInfo = getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(
                            getApplicationContext().getPackageName(),
                            0
                    );
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
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
        switch (v.getId()){
                case R.id.time:
                    new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            departureTime.set(departureTime.get(Calendar.YEAR), departureTime.get(Calendar.MONTH), departureTime.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
                            String dateStr = (new SimpleDateFormat("yyyyMMdd HHmm")).format(departureTime.getTime());
                            dateStr = dateStr.replaceAll("\\s+", "T");
                            String[] split = Address.splitIso8601(dateStr);
                            time.setText(split[3] + "h" + split[4]);
                        }
                    }, departureTime.get(Calendar.HOUR_OF_DAY), departureTime.get(Calendar.MINUTE), true).show();
                    break;
                case R.id.date:
                    new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            departureTime.set(year, monthOfYear, dayOfMonth, departureTime.get(Calendar.HOUR_OF_DAY), departureTime.get(Calendar.MINUTE));
                            String dateStr = (new SimpleDateFormat("yyyyMMdd HHmm")).format(departureTime.getTime());
                            dateStr = dateStr.replaceAll("\\s+", "T");
                            if(departureTime.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) && departureTime.get(Calendar.MONTH) == today.get(Calendar.MONTH) && departureTime.get(Calendar.YEAR) == today.get(Calendar.YEAR)){
                                date.setText(getString(R.string.today));
                            }
                            else{
                                date.setText(departureTime.get(Calendar.DAY_OF_MONTH)
                                        + " "+departureTime.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                                        + " "+departureTime.get(Calendar.YEAR));
                            }
                        }
                    }, departureTime.get(Calendar.YEAR), departureTime.get(Calendar.MONTH), departureTime.get(Calendar.DAY_OF_MONTH)).show();
                    break;
            case R.id.fab:
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
                break;
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

    public void switchToSearch(Bundle bundle){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.enter, R.animator.exit, R.animator.enter, R.animator.exit);
        fragmentTransaction.addToBackStack(SearchFragment.TAG);
        fragmentTransaction.replace(R.id.fragment, SearchFragment.newInstance(bundle));
        fragmentTransaction.commit();
        floatingActionButton.setVisibility(View.INVISIBLE);
    }
    public void switchToMain(Bundle bundle, int request){
        fragmentManager.popBackStack(SearchFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        floatingActionButton.setVisibility(View.VISIBLE);
    }

    public void setTopVisibility(int visibility){
        findViewById(R.id.topPanel).setVisibility(visibility);
    }
}
