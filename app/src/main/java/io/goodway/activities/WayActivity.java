package io.goodway.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.goodway.R;
import io.goodway.model.ContainerType;
import io.goodway.adapters.WayContainerAdapter;
import io.goodway.model.callback.WayCallback;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.Way;

/**
 * Created by antoine on 8/23/15.
 */
public class WayActivity extends AppCompatActivity implements WayCallback, SwipeRefreshLayout.OnRefreshListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView time, date;
    private WayContainerAdapter adapter;
    private Address from, to;

    private String mail, password;
    private Calendar departureTime, today;

    private static final int ACCESS_FINE_LOCATION=1;
    private GoogleApiClient googleApiClient;

    private ProgressDialog getLocationDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_way);

        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);

        Bundle extras = this.getIntent().getExtras();
        this.from = extras.getParcelable("departure");
        this.to = extras.getParcelable("arrival");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.routes);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        time = (TextView) findViewById(R.id.time);
        date = (TextView) findViewById(R.id.date);

        if(from!=null){
            prepare();
        }
        else{
            getLocationDialog = new ProgressDialog(this);
            getLocationDialog.setTitle("Position");
            getLocationDialog.setMessage("Récupération de la position");
            getLocationDialog.setIndeterminate(true);
            getLocationDialog.show();
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
    }

    private void prepare(){

        departureTime =  Calendar.getInstance();
        today = Calendar.getInstance();

        String dateStr = (new SimpleDateFormat("yyyyMMdd HHmmss")).format(departureTime.getTime());
        dateStr = dateStr.replaceAll("\\s+", "T");

        String fromLongitude = ((Double)from.getLongitude()).toString();
        String fromLatitude = ((Double)from.getLatitude()).toString();
        String toLongitude = ((Double)to.getLongitude()).toString();
        String toLatitude = ((Double)to.getLatitude()).toString();

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

        adapter = new WayContainerAdapter(this, this, from, to, fromLatitude, fromLongitude, toLatitude, toLongitude, dateStr);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.add(ContainerType.navitia);
        adapter.add(ContainerType.carSharing);
        adapter.add(ContainerType.bike);
        adapter.add(ContainerType.uber);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v){

        switch (v.getId()){
            case R.id.time:
                new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        departureTime.set(departureTime.get(Calendar.YEAR), departureTime.get(Calendar.MONTH), departureTime.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
                        String dateStr = (new SimpleDateFormat("yyyyMMdd HHmm")).format(departureTime.getTime());
                        dateStr = dateStr.replaceAll("\\s+", "T");
                        adapter.setDepartureTime(dateStr);
                        String[] split = Address.splitIso8601(dateStr);
                        time.setText(split[3] + "h" + split[4]);
                        refresh();
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
                        adapter.setDepartureTime(dateStr);
                        refresh();
                    }
                }, departureTime.get(Calendar.YEAR), departureTime.get(Calendar.MONTH), departureTime.get(Calendar.DAY_OF_MONTH)).show();
                break;
        }
    }

    private void refresh(){
        adapter.clear();
        adapter.add(ContainerType.navitia);
        adapter.add(ContainerType.carSharing);
        adapter.add(ContainerType.bike);
        adapter.add(ContainerType.uber);
        swipeRefreshLayout.setRefreshing(false);
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
    public void action(View v, Way w) {
        Intent intent = new Intent(WayActivity.this, DetailedWayActivity.class);
        intent.putExtra("WAY", w);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, v, getString(R.string.transition_way_card));
        if (Build.VERSION.SDK_INT >= 16) {
            WayActivity.this.startActivity(intent, options.toBundle());
        }
        else{
            WayActivity.this.startActivity(intent);
        }
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        refresh();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location userLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        from = new Address(getString(R.string.current_location), userLocation.getLatitude(), userLocation.getLongitude());
        prepare();
        getLocationDialog.hide();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Impossible de récupérer la position", Toast.LENGTH_SHORT).show();
        getLocationDialog.hide();
    }
}
