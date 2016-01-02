package io.goodway;


import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.squareup.picasso.Picasso;

import io.goodway.model.GroupEvent;
import io.goodway.model.User;
import io.goodway.model.network.GoodwayHttpClientGet;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.view.ImageTrans_CircleTransform;
import io.goodway.view.fragment.MainFragment;
import io.goodway.view.fragment.SearchFragment;


/**
 * The main activity of the program
 * @author Antoine Sauray
 * @version 2.0
 */
public class MainActivity extends AppCompatActivity{

    // ----------------------------------- UI

    /**
     * These variables are used to get result from other activities.
     *
     * @see
     */
    public static final int FROM_LOCATION = 1, TO_LOCATION = 2, EVENT_REQUEST =3, SETLOCATION=4, PROFILE=5, FRIENDS=6;

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
     * The user interface for the current mode selected
     */
    private View ui;

    private NavigationView navigationView;

    private FrameLayout fragmentView;


    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private TabLayout tabLayout;

    // ----------------------------------- Model
    /**
     * Provides markers on a marker. The key is the marker title attribute
     */
    private Address from, to;

    private User user;
    private String token;

    public static final int DEPARTURE=1, DESTINATION=2;

    private Fragment current;
    private SearchFragment search;
    private MainFragment main;
    private int nbFriendRequests;

    // ----------------------------------- Constants
    private static final int MAIN=1, SEARCH=2;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkGooglePlayServices();
        setContentView(R.layout.activity_main);


        //from = new Address(R.string.your_location, R.mipmap.ic_home_black_24dp, AddressType.POSITION);

        Bundle extras = this.getIntent().getExtras();
        token = extras.getString("token");

        GoodwayHttpClientPost.me(this, new Action<User>() {
            @Override
            public void action(User user) {
                MainActivity.this.user = user;
                ((TextView)navigationView.getHeaderView(0).findViewById(R.id.name)).setText(user.getName());
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {

            }
        }, null, token);

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

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
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

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();
        actionBar.setDisplayShowTitleEnabled(false);
        toolbar.setLogo(R.drawable.goodway_text_very_small);

        fragmentView = (FrameLayout) findViewById(R.id.fragment);

        main = new MainFragment();
        search = new SearchFragment();

        Address departure = extras.getParcelable("departure");
        Address destination = extras.getParcelable("destination");
        Bundle b = new Bundle();
        if(departure!=null){
            setFrom(departure);
            b.putParcelable("DEPARTURE", departure);
        }
        if(destination!=null){
            setTo(destination);
            b.putParcelable("DESTINATION", departure);
        }

        switchToMain(b, -1);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        ((TextView)navigationView.getHeaderView(0).findViewById(R.id.version)).setText(getString(R.string.version) + " " + getVersionInfo());

        Log.d("avatar", "avatar" + user.getAvatar());


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.friends:
                        Intent i = new Intent(MainActivity.this, FriendsActivity.class);
                        i.putExtra("token", token);
                        i.putExtra("nbFriendRequests", nbFriendRequests);
                        startActivityForResult(i, MainActivity.FRIENDS);
                        break;
                    case R.id.groups:
                        Intent i2 = new Intent(MainActivity.this, UserGroupsActivity.class);
                        i2.putExtra("token", token);
                        startActivity(i2);
                        break;
                    case R.id.uber:
                        /*
                        Intent i3 = new Intent(MainActivity.this, UberLoginActivity.class);
                        startActivity(i3);*/
                        break;
                }
                return false;
            }
        });

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(current==search){
            onBackPressed();
            return true;
        }
        else{
            if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onResume(){
        super.onResume();
        Picasso.with(this)
                .load(user.getAvatar())
                .error(R.mipmap.ic_person_white_48dp)
                .resize(150, 150)
                .centerCrop()
                .transform(new ImageTrans_CircleTransform())
                .into(((ImageView) navigationView.getHeaderView(0).findViewById(R.id.avatar)));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult", "onActivityResult");
        if (requestCode == MainActivity.EVENT_REQUEST){
            if(resultCode == RESULT_OK){
                Log.d("EVENT_REQUEST", "request code");
                GroupEvent groupEvent = data.getExtras().getParcelable("GROUPEVENT");
                Address eventAddr = new Address(groupEvent.getName(), groupEvent.getLatitude(), groupEvent.getLongitude());
                switchAfterResult(data, eventAddr);
            } else{
                search.setCurrentItem(2);
            }
        }
        else if(requestCode==MainActivity.PROFILE){
            if(resultCode == RESULT_OK) {
                Log.d("EVENT_REQUEST", "request code");
                //user = data.getExtras().getParcelable("user");
            }
        }
        else if(requestCode==MainActivity.FRIENDS){
            if(resultCode==RESULT_OK){
                nbFriendRequests = data.getIntExtra("nbFriendRequests", 0);
                if(nbFriendRequests>0){navigationView.getMenu().findItem(R.id.friends).setTitle(getString(R.string.friends)+" ("+nbFriendRequests+")");}
                else{navigationView.getMenu().findItem(R.id.friends).setTitle(getString(R.string.friends));}
            }
        }
    }

    private void switchAfterResult(Intent data, Address addr){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        int request = data.getIntExtra("REQUEST", DESTINATION);
        switch(request){
            case DESTINATION:
                setTo(addr);
                bundle.putParcelable("DESTINATION", addr);
                break;
            case DEPARTURE:
                setFrom(addr);
                bundle.putParcelable("DEPARTURE", addr);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBarDrawerToggle.syncState();
        tabLayout.setVisibility(View.INVISIBLE);
        toolbar.setLogo(R.drawable.goodway_text_very_small);
        main.setArguments(bundle);
        fragmentTransaction.replace(R.id.fragment, main);
        fragmentTransaction.commitAllowingStateLoss();
        current=main;
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


    public void drawerHeaderClick(View v){
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }

    public void setFrom(Address adress){
        this.from = adress;
        Log.d("setFrom", "setFrom : " + adress.getLatitude() + ";" + adress.getLongitude());
    }

    public void setTo(Address adress){
        this.to = adress;
    }

    public void onBackPressed(){
        if(current==search) {
            Bundle extras = new Bundle();
            int request = -1;
            if (from != null) {
                extras.putParcelable("DEPARTURE", from);
                request = DEPARTURE;
            }
            if (to != null) {
                extras.putParcelable("DESTINATION", to);
                request = DESTINATION;
            }
            switchToMain(extras, request);
        }
        else{
            super.onBackPressed();
        }
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
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment, fragment);
        fragmentTransaction.commit();
        current = fragment;
    }

    public void switchToSearch(Bundle bundle){
        switchFragment(search, bundle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tabLayout.setVisibility(View.VISIBLE);
        toolbar.setLogo(null);
    }

    public void switchToMain(Bundle bundle, int request){
        switchFragment(main, bundle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBarDrawerToggle.syncState();
        tabLayout.setVisibility(View.INVISIBLE);
        toolbar.setLogo(R.drawable.goodway_text_very_small);
        switch(request){
            case DEPARTURE:
                setFrom(from);
                main.setFrom(from);
                break;
            case DESTINATION:
                setTo(to);
                main.setTo(to);
                break;
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

}
