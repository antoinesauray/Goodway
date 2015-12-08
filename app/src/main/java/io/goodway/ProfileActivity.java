package io.goodway;

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
    private TextView impact, home, work;
    private PercentView percentView;

    private ProgressBar impactProgress;
    private View homeButton, workButton;
    private TextView homeText, workText;
    private CheckBox shareHome, shareWork;

    private MapFragment fragHome, fragWork;

    private GoogleMap mapHome, mapWork;
    private Address homeAddr, workAddr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Bundle extras = this.getIntent().getExtras();
        user = extras.getParcelable("USER");
        boolean self = extras.getBoolean("SELF", false);
        toolbar = (Toolbar) findViewById(R.id.mapToolbar);
        toolbar.setTitle(user.getName());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        Log.d("user shares home:" + user.sharesHome(), "user shares home:");
        Log.d("user shares work:" + user.sharesWork(), "user shares work:");

        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);

        impact = (TextView) findViewById(R.id.impact);
        home = (TextView) findViewById(R.id.home);
        work = (TextView) findViewById(R.id.work);
        percentView = (PercentView) findViewById(R.id.percentView);
        impactProgress = (ProgressBar) findViewById(R.id.impactProgress);
        homeButton = findViewById(R.id.homeButton);
        workButton = findViewById(R.id.workButton);

        shareHome = (CheckBox) findViewById(R.id.shareHome);
        shareWork = (CheckBox) findViewById(R.id.shareWork);

        fragHome = (MapFragment) getFragmentManager().findFragmentById(R.id.mapHome);
        fragHome.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mapHome = googleMap;
                if(homeAddr!=null) {
                    setHomeOnMap();
                }
            }});

        fragWork = (MapFragment) getFragmentManager().findFragmentById(R.id.mapWork);
        fragWork.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mapWork = googleMap;
                if(workAddr!=null) {
                    setWorkOnMap();
                }
            }});

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

        GoodwayHttpsClient.getUserCo2(this, new Action<Integer>() {
            @Override
            public void action(Integer e) {
                impactProgress.setVisibility(View.INVISIBLE);
                impact.setText(e + "g co2");
                percentView.setPercentage(30f);
            }
        }, mail, password, user.getId());

        if(user.sharesHome()){
            if(shareHome!=null){shareHome.setChecked(true);}
            shareHome.setOnCheckedChangeListener(this);
            GoodwayHttpsClient.getUserHome(this, new Action<Address>() {
                @Override
                public void action(Address e) {
                    homeAddr=e;
                    if(mapHome!=null){
                        if(homeAddr!=null) {
                            findViewById(R.id.error_home).setVisibility(View.INVISIBLE);
                            setHomeOnMap();
                        }
                        else{
                            findViewById(R.id.error_home).setVisibility(View.VISIBLE);
                        }
                    }
                }
            }, mail, password, user.getId(), user.getFirstName());
        }
        if(user.sharesWork()){
            if(shareWork!=null){shareWork.setChecked(true);}
            shareWork.setOnCheckedChangeListener(this);
            GoodwayHttpsClient.getUserWork(this, new Action<Address>() {
                @Override
                public void action(Address e) {
                    workAddr = e;
                    if (mapHome != null) {
                        if (workAddr != null) {
                            findViewById(R.id.error_work).setVisibility(View.INVISIBLE);
                            setWorkOnMap();
                        } else {
                            findViewById(R.id.error_work).setVisibility(View.VISIBLE);
                        }
                    }
                }
            }, mail, password, user.getId(), user.getFirstName());
        }
    }

    private void setHomeOnMap(){
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(homeAddr.getLatitude(), homeAddr.getLongitude()))      // Sets the center of the map to Mountain View
                .zoom(13)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        mapHome.addMarker(new MarkerOptions()
                .position(new LatLng(homeAddr.getLatitude(), homeAddr.getLongitude()))
                        //.alpha(0.8f)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title(getString(R.string.home)));
        mapHome.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void setWorkOnMap(){
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(workAddr.getLatitude(), workAddr.getLongitude()))      // Sets the center of the map to Mountain View
                .zoom(13)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        mapWork.addMarker(new MarkerOptions()
                .position(new LatLng(workAddr.getLatitude(), workAddr.getLongitude()))
                        //.alpha(0.8f)
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
            public void action() {

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
            Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            switch (requestCode){
                case HOME:
                    homeAddr = address;
                    if(mapHome!=null) {
                        findViewById(R.id.error_home).setVisibility(View.INVISIBLE);
                        setHomeOnMap();
                    }
                    break;
                case WORK:
                    workAddr = address;
                    if(mapWork!=null) {
                        findViewById(R.id.error_work).setVisibility(View.INVISIBLE);
                        setWorkOnMap();
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

        GoodwayHttpsClient.setSharing(this, new Action<Boolean>() {
            @Override
            public void action(Boolean e) {
                dialog.dismiss();

            }
        }, new ErrorAction() {
            @Override
            public void action() {
                dialog.dismiss();
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
}
