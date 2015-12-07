package io.goodway.view.fragment;

import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import io.goodway.R;
import io.goodway.navitia_android.Address;


/**
 * Created by sauray on 14/03/15.
 */
public class MainFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener {

    private View root;
    private boolean openedState;
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private FloatingActionButton floatingActionButton;

    private LinearLayout bottom;

    private Location userLocation;
    private TextView fromText, toText;

    private Address from, to;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_map, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        bottom = (LinearLayout) root.findViewById(R.id.bottom);
        fromText = (TextView) root.findViewById(R.id.from);
        toText = (TextView) root.findViewById(R.id.to);

        floatingActionButton = (FloatingActionButton) root.findViewById(R.id.fab);
        floatingActionButton.setBaselineAlignBottom(true);

        Bundle extras = getArguments();
        LatLng latLng = null;
        if(extras.getParcelable("DEPARTURE")!=null){
            Address a = extras.getParcelable("DEPARTURE");
            fromText.setText(extras.getParcelable("DEPARTURE").toString());
            latLng = new LatLng(a.getLatitude(), a.getLongitude());
        }

        if (extras.getParcelable("DESTINATION") != null) {
            Address a = extras.getParcelable("DESTINATION");
            toText.setText(a.toString());
            latLng = new LatLng(a.getLatitude(), a.getLongitude());
        }
        return root;
    }

    @Override
    public String toString(){
        return "Bottom Sheet";
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Toast.makeText(getActivity(), "map ready", Toast.LENGTH_SHORT).show();
        googleMap.setPadding(0, 0, 0, root.getHeight());
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setOnMyLocationChangeListener(this);

        CameraPosition cameraPosition = null;
        if(from==null&&to==null&&userLocation!=null) {
            cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()))      // Sets the center of the map to Mountain View
                    .zoom(14)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        else {
            if(from!=null){
                cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(from.getLatitude(), from.getLongitude()))      // Sets the center of the map to Mountain View
                        .zoom(14)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
            if(to!=null){
                cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(to.getLatitude(), to.getLongitude()))      // Sets the center of the map to Mountain View
                        .zoom(14)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
        if(cameraPosition!=null) {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    public void setFrom(Address from){
        this.from = from;
        Log.d("setfrom", "setfrom: " + from.toString());
        fromText.setText(from.toString());
        Log.d("setTo", "setTo : " + from.getLatitude() + ";" + from.getLongitude());
    }

    public void setTo(Address to){
        this.to=to;
        Log.d("setfrom", "setfrom: " + to.toString());
        fromText.setText(to.toString());
        Log.d("setTo", "setTo : " + to.getLatitude() + ";" + to.getLongitude());
        /*
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(to.getLatitude(), to.getLongitude()))
                        //.alpha(0.8f)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title(getString(R.string.arrival)));
*/
        Log.d("userLocation=null", "userLocation=null");

    }

    @Override
    public void onMyLocationChange(Location location) {
        userLocation = location;
    }


}
