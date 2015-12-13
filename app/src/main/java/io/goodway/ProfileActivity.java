package io.goodway;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import io.goodway.model.User;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.Way;
import io.goodway.view.PercentView;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class ProfileActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    // ----------------------------------- Model
    /**
     * Unique identifier for this activity
     */
    private static final String TAG = "STOP_ACTIVITY";

    public static final int HOME=1, WORK=2;
    // ----------------------------------- UI

    /**
     * Toolbar widget
     */
    private Toolbar toolbar;
    private User user;

    private String mail, password;
    private TextView home, work;

    private ProgressBar impactProgress;
    private View homeButton, workButton;
    private TextView homeText, workText;
    private CheckBox shareHome, shareWork;

    private MapFragment fragHome, fragWork;

    private GoogleMap mapHome, mapWork;
    private Address homeAddr, workAddr;

    private boolean self;

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

        Log.d("shares home", "shares home=" + user.sharesHome());
        Log.d("shares work", "shares work="+user.sharesWork());

        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);
        shareHome = (CheckBox) findViewById(R.id.shareHome);
        shareWork = (CheckBox) findViewById(R.id.shareWork);

        shareHome.setChecked(user.sharesHome());
        shareWork.setChecked(user.sharesWork());

        shareHome.setOnCheckedChangeListener(this);
        shareWork.setOnCheckedChangeListener(this);

        home = (TextView) findViewById(R.id.home);
        work = (TextView) findViewById(R.id.work);
        homeButton = findViewById(R.id.homeButton);
        workButton = findViewById(R.id.workButton);


        fragHome = (MapFragment) getFragmentManager().findFragmentById(R.id.mapHome);
        fragHome.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mapHome = googleMap;
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                Log.d("map ready home", "map ready home");
                if(homeAddr!=null) {
                    Log.d("homeAddr not null", "homeAddr not null");
                    setHomeOnMap(new LatLng(homeAddr.getLatitude(), homeAddr.getLongitude()));
                }
                else if (self){
                    LatLng home=user.getHome();
                    if(home.latitude!=0 && home.longitude!=0){setHomeOnMap(home);}
                }
            }});

        fragWork = (MapFragment) getFragmentManager().findFragmentById(R.id.mapWork);
        fragWork.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mapWork = googleMap;
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                Log.d("map ready work", "map ready work");
                if (workAddr != null) {
                    Log.d("workAddr not null", "workAddr not null");
                    setWorkOnMap(new LatLng(workAddr.getLatitude(), workAddr.getLongitude()));
                }
                else if (self){
                    LatLng work=user.getWork();
                    if(work.latitude!=0 && work.longitude!=0){setHomeOnMap(work);}
                }
            }
        });

        if(self){
            homeButton.setVisibility(View.VISIBLE);
            workButton.setVisibility(View.VISIBLE);
            homeText = (TextView) findViewById(R.id.homeText);
            workText = (TextView) findViewById(R.id.workText);

            shareHome.setVisibility(View.VISIBLE);
            shareWork.setVisibility(View.VISIBLE);

            homeButton.setOnClickListener(this);
            workButton.setOnClickListener(this);
        }
        else{
            home.setVisibility(View.VISIBLE);
            work.setVisibility(View.VISIBLE);
        }

        if(user.isFriend() || self) {
            Log.d("friend self", "friend self");
            if(self){
                GoodwayHttpsClient.getUserHome(this, new Action<Address>() {
                        @Override
                        public void action(Address e) {
                            if(e!=null){
                                homeAddr = e;
                                findViewById(R.id.error_home).setVisibility(View.INVISIBLE);
                                setHomeOnMap(new LatLng(e.getLatitude(), e.getLongitude()));
                            }
                            else{
                                findViewById(R.id.error_home).setVisibility(View.VISIBLE);
                            }
                        }
                    }, new ErrorAction() {
                        @Override
                        public void action(int length) {
                            Toast.makeText(ProfileActivity.this, R.string.connexion_error, Toast.LENGTH_SHORT).show();
                        }
                    }, mail, password, user.getFirstName());
            GoodwayHttpsClient.getUserWork(this, new Action<Address>() {
                        @Override
                        public void action(Address e) {
                            if(e!=null){
                                workAddr = e;
                                findViewById(R.id.error_work).setVisibility(View.INVISIBLE);
                                setWorkOnMap(new LatLng(e.getLatitude(), e.getLongitude()));
                            }
                            else{
                                findViewById(R.id.error_work).setVisibility(View.VISIBLE);
                            }
                        }
                    }, new ErrorAction() {
                        @Override
                        public void action(int length) {
                            Toast.makeText(ProfileActivity.this, R.string.connexion_error, Toast.LENGTH_SHORT).show();
                        }
            }, mail, password, user.getFirstName());
            }
            else{
                Log.d("not friend self", "not friend self");
                if(user.sharesHome()){
                    Log.d("shares home", "shares home");
                    GoodwayHttpsClient.getUserHome(this, new Action<Address>() {
                        @Override
                        public void action(Address e) {
                            homeAddr = e;
                            if (mapHome != null) {
                                findViewById(R.id.error_home).setVisibility(View.INVISIBLE);
                                if(homeAddr!=null) {setHomeOnMap(new LatLng(homeAddr.getLatitude(), homeAddr.getLongitude()));}
                                else{findViewById(R.id.error_home).setVisibility(View.VISIBLE);}
                            }
                        }
                    }, new ErrorAction() {
                        @Override
                        public void action(int length) {
                            findViewById(R.id.error_home).setVisibility(View.VISIBLE);
                        }
                    }, mail, password, user.getId(), user.getFirstName());
                }
                else{
                    home.setText(R.string.home_not_shared);
                }
                if(user.sharesWork()){
                    GoodwayHttpsClient.getUserWork(this, new Action<Address>() {
                        @Override
                        public void action(Address e) {
                            workAddr = e;
                            if (mapWork != null) {
                                findViewById(R.id.error_home).setVisibility(View.INVISIBLE);
                                if(workAddr!=null){setWorkOnMap(new LatLng(workAddr.getLatitude(), workAddr.getLongitude()));}
                                else{findViewById(R.id.error_work).setVisibility(View.VISIBLE);}
                            }
                        }
                    }, new ErrorAction() {
                        @Override
                        public void action(int length) {
                            findViewById(R.id.error_work).setVisibility(View.VISIBLE);
                        }
                    }, mail, password, user.getId(), user.getFirstName());
                }
                else{
                    work.setText(R.string.work_not_shared);
                }
            }
        } else {
            findViewById(R.id.hiddenContent1).setVisibility(View.INVISIBLE);
            findViewById(R.id.hiddenContent2).setVisibility(View.INVISIBLE);
            TextView no_friend = (TextView) findViewById(R.id.no_friend);
            no_friend.setText(getString(R.string.content_hidden)+" "+user.getFirstName());
            no_friend.setVisibility(View.VISIBLE);
        }
    }

    private void setHomeOnMap(LatLng position){
        Log.d("setHomeOnMap", "setHomeOnMap");
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(position)      // Sets the center of the map to Mountain View
                    .zoom(13)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            mapHome.addMarker(new MarkerOptions()
                    .position(position)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title(getString(R.string.home)));
            mapHome.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void setWorkOnMap(LatLng position){
        Log.d("setWorkOnMap", "setWorkOnMap");
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(position)      // Sets the center of the map to Mountain View
                    .zoom(13)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            mapWork.addMarker(new MarkerOptions()
                    .position(position)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title(getString(R.string.work)));
            mapWork.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        GoodwayHttpsClient.getTrips(this, new Action<Way>() {
            @Override
            public void action(Way e) {

            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {

            }
        }, mail, password, user.getId());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.homeButton:
                Intent i = new Intent(this, SetLocationActivity.class);
                i.putExtra("USER", user);
                i.putExtra("PLACE", HOME);
                startActivityForResult(i, HOME);
                break;
            case R.id.workButton:
                Intent i2 = new Intent(this, SetLocationActivity.class);
                i2.putExtra("USER", user);
                i2.putExtra("PLACE", WORK);
                startActivityForResult(i2, WORK);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Address address = data.getParcelableExtra("ADDRESS");
            switch (requestCode){
                case HOME:
                    homeAddr = address;
                    user.setHome(homeAddr);
                    GoodwayHttpsClient.updateHome(this, new Action<Void>() {
                        @Override
                        public void action(Void e) {

                        }
                    }, new ErrorAction() {
                        @Override
                        public void action(int length) {
                            if(length==-1){
                                new AlertDialog.Builder(ProfileActivity.this)
                                        .setTitle(R.string.update_location)
                                        .setMessage(R.string.failure)
                                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {}
                                        })
                                        .show();
                            }
                        }
                    }, mail, password, homeAddr.getLatitude(), homeAddr.getLongitude());
                    if(mapHome!=null) {
                        findViewById(R.id.error_home).setVisibility(View.INVISIBLE);
                        setHomeOnMap(new LatLng(homeAddr.getLatitude(), homeAddr.getLongitude()));
                    }
                    break;
                case WORK:
                    workAddr = address;
                    user.setWork(workAddr);
                    GoodwayHttpsClient.updateWork(this, new Action<Void>() {
                        @Override
                        public void action(Void e) {

                        }
                    }, new ErrorAction() {
                        @Override
                        public void action(int length) {
                            if(length==-1){
                                new AlertDialog.Builder(ProfileActivity.this)
                                        .setTitle(R.string.update_location)
                                        .setMessage(R.string.failure)
                                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {}
                                        })
                                        .show();
                            }
                        }
                    }, mail, password, workAddr.getLatitude(), workAddr.getLongitude());
                    if(mapWork!=null) {
                        findViewById(R.id.error_work).setVisibility(View.INVISIBLE);
                        setWorkOnMap(new LatLng(workAddr.getLatitude(), workAddr.getLongitude()));
                    }
                    break;
            }
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.updating_share_options));
        dialog.setProgressStyle(dialog.STYLE_SPINNER);
        dialog.show();

        Log.d("isChecked", "isChecked=" + isChecked);
        Log.d("isChecked()", "isChecked()="+shareHome.isChecked());
        GoodwayHttpsClient.setSharing(this, new Action<Boolean>() {
            @Override
            public void action(Boolean e) {
                dialog.dismiss();
                user.setSharesHome(shareHome.isChecked());
                user.setSharesWork(shareWork.isChecked());
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {
                dialog.dismiss();
                shareHome.setChecked(user.sharesHome());
                shareWork.setChecked(user.sharesWork());
                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle(R.string.updating_share_options)
                        .setMessage(R.string.failed_updating_options)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                                //.setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
        },mail, password, shareHome.isChecked(), shareWork.isChecked());
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
}
