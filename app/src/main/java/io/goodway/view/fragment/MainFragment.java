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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import io.goodway.R;


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
        if(extras.getParcelable("DEPARTURE")!=null){
            fromText.setText(extras.getParcelable("DEPARTURE").toString());
        }
        if (extras.getParcelable("DESTINATION") != null) {
            toText.setText(extras.getParcelable("DESTINATION").toString());
        }
        return root;
    }

    @Override
    public String toString(){
        return "Bottom Sheet";
    }

    public void setOpenedState(boolean state){
        this.openedState = state;
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        outState.putBoolean("OPENED", openedState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            openedState = savedInstanceState.getBoolean("OPENED");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setPadding(0, 0, 0, root.getHeight());
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

    public void setFrom(String from){
        Log.d("setfrom", "setfrom: " + from);
        fromText.setText(from);}

    public void setTo(String to){
        Log.d("setto", "setto: " + to);
        toText.setText(to);}

    public GoogleMap getGoogleMap(){return googleMap;}

    @Override
    public void onMyLocationChange(Location location) {
        userLocation = location;
    }
}
