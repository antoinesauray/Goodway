package io.goodway.view.fragment;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.goodway.R;
import io.goodway.model.User;
import io.goodway.navitia_android.Address;
import io.goodway.view.ImageTrans_CircleTransform;
import io.goodway.view.fragment.nested.FlowFragment;


/**
 * Created by sauray on 14/03/15.
 */
public class TripFragment extends Fragment implements OnMapReadyCallback {

    private View root;

    private CoordinatorLayout coordinatorLayout;

    private Location userLocation;

    private Address from, to;
    private String token;
    private User user;
    private int nbFriendRequests;

    private Button fromButton, toButton;
    private Spinner startOrEnd;

    private FragmentManager fragmentManager;

    private LinearLayout action, actions, container;

    private Button time, date;
    private Calendar departureTime, today;

    public static final int DEPARTURE=1, DESTINATION=2;
    public static final int FROM_LOCATION = 1, TO_LOCATION = 2, EVENT_REQUEST =3, SETLOCATION=4, PROFILE=5, FRIENDS=6;

    private static final int ACCESS_FINE_LOCATION=1;

    // ----------------------------------- Constants
    private static final int MAIN=1, SEARCH=2;

    public static final String TAG="MAIN_FRAGMENT";

    private int expanded=0;

    private View trip;
    private View action1, action2, action3;

    private MapFragment mapFragment;
    private Address departure, destination;

    public static TripFragment newInstance(Bundle args) {
        TripFragment fragment = new TripFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_trip, container, false);
        Bundle extras = getArguments();
        token = extras.getString("token");
        user = extras.getParcelable("user");
        departure = extras.getParcelable("departure");
        destination = extras.getParcelable("destination");

        fromButton = (Button) root.findViewById(R.id.from);
        toButton = (Button) root.findViewById(R.id.to);
        startOrEnd = (Spinner) root.findViewById(R.id.spinner);

        coordinatorLayout = (CoordinatorLayout) root.findViewById(R.id.coordinatorLayout);

        mapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mapFragment, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);


        LatLng latLng = null;
        if (extras.getParcelable("departure") != null) {
            from = extras.getParcelable("departure");

            fromButton.setText(extras.getParcelable("departure").toString());
            //latLng = new LatLng(a.getLatitude(), a.getLongitude());
        }

        if (extras.getParcelable("destination") != null) {
            to = extras.getParcelable("destination");
            toButton.setText(to.toString());
            //latLng = new LatLng(a.getLatitude(), a.getLongitude());
        }

        return root;
    }

    @Override
    public String toString() {
        return "Trip fragment";
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        if(destination!=null){
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(destination.getLatitude(), destination.getLongitude()))      // Sets the center of the map to Mountain View
                    .zoom(13)                   // Sets the zoom
                    //.bearing(90)                // Sets the orientation of the camera to east
                    //.tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(destination.getLatitude(), destination.getLongitude()))
                    .title(destination.getName())).showInfoWindow();
        }
        else if(departure!=null){
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(departure.getLatitude(), departure.getLongitude()))      // Sets the center of the map to Mountain View
                    .zoom(13)                   // Sets the zoom
                    //.bearing(90)                // Sets the orientation of the camera to east
                    //.tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(departure.getLatitude(), departure.getLongitude()))
                    .title(departure.getName())).showInfoWindow();

        }
        else{

        }
    }
}
