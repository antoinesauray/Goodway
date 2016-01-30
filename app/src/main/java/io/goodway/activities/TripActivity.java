package io.goodway.activities;

import android.app.FragmentTransaction;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import io.goodway.R;
import io.goodway.navitia_android.Coordinate;
import io.goodway.navitia_android.GeoJSON;
import io.goodway.navitia_android.Way;
import io.goodway.navitia_android.WayPart;

/**
 * Created by antoine on 23/01/16.
 */
public class TripActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {


    private MapFragment mapFragment;
    private GoogleMap googleMap;
    private Way way;

    private LocationManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        Bundle extras = this.getIntent().getExtras();
        way = extras.getParcelable("way");

        mapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mapFragment, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onLocationChanged(Location lastLocation) {
        float bearing= lastLocation.getBearing();
        if(googleMap!=null){
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(way.getFrom().getLatitude(), way.getFrom().getLongitude()))      // Sets the center of the map to Mountain View
                    .zoom(13)                   // Sets the zoom
                    .bearing(bearing)                // Sets the orientation of the camera to east
                    .tilt(60)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);
        Criteria crit = new Criteria();
        crit.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = lm.getBestProvider(crit, true);
        Location location = lm.getLastKnownLocation(provider);
        if(location!=null) {
            onLocationChanged(location);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        lm.removeUpdates(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(way.getFrom().getLatitude(), way.getFrom().getLongitude()))      // Sets the center of the map to Mountain View
                .zoom(13)                   // Sets the zoom
                        //.bearing(90)                // Sets the orientation of the camera to east
                .tilt(60)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        for(WayPart part : way.getParts()){
            Log.d("part", "part");
            GeoJSON geoJSON = part.getGeoJSON();
            Log.d("geoJSON", "geoJSON");
            if(geoJSON!=null){
                Log.d("geoJSON not null", "geoJSON not null");
                for(Coordinate c : geoJSON.getCoordinates()){
                    Polygon polygon = googleMap.addPolygon(new PolygonOptions()
                            .add(new LatLng(c.getLatitude(), c.getLongitude()))
                            .strokeColor(Color.RED)
                            .fillColor(Color.BLUE));
                }
            }

        }

    }
}
