package io.goodway.activities.fragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.goodway.activities.GoAddressActivity;
import io.goodway.activities.GoContactActivity;
import io.goodway.activities.GoPlaceActivity;
import io.goodway.activities.MainActivity;
import io.goodway.R;
import io.goodway.model.User;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.view.ImageTrans_CircleTransform;


/**
 * Created by sauray on 14/03/15.
 */
public class MainFragmentHome extends Fragment implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GpsStatus.Listener {

    private View root;

    private CoordinatorLayout coordinatorLayout;

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

    private View trip;
    private View action1, action2, action3, action4;

    private RecyclerView recyclerView;

    private TextView geolocation;

    private GoogleApiClient googleApiClient;
    private Location userLocation;

    private Address departure;

    public static MainFragmentHome newInstance(Bundle args) {
        MainFragmentHome fragment = new MainFragmentHome();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstancedState){
        super.onCreate(savedInstancedState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            ACCESS_FINE_LOCATION);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                googleApiClient = new GoogleApiClient.Builder(getActivity())
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                LocationManager locationManager = (LocationManager) getActivity()
                        .getSystemService(Context.LOCATION_SERVICE);
                locationManager.addGpsStatusListener(this);
            }
        } else {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            LocationManager locationManager = (LocationManager) getActivity()
                    .getSystemService(Context.LOCATION_SERVICE);
            locationManager.addGpsStatusListener(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_main_home, container, false);

        fragmentManager = getChildFragmentManager();

        Bundle extras = getArguments();
        token = extras.getString("token");
        user = extras.getParcelable("user");

        departure = extras.getParcelable("departure");

        geolocation = (TextView) root.findViewById(R.id.geolocation);

        if(departure!=null){
            geolocation.setText(departure.getName());
        }

        ((TextView)root.findViewById(R.id.what_to_do)).setText(getString(R.string.what_to_do)+" "+user.getFirstName()+" ?");

        Picasso.with(getActivity())
                .load(user.getAvatar())
                .error(R.drawable.ic_account_circle_black_36dp)
                .resize(convertDpToPixel(40, getActivity()), convertDpToPixel(40, getActivity()))
                .centerCrop()
                .transform(new ImageTrans_CircleTransform())
                .into((ImageView) root.findViewById(R.id.avatar));


        recyclerView = (RecyclerView) root.findViewById(R.id.recyclerView);


        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //recyclerView.setAdapter(adapter);

        action = (LinearLayout) root.findViewById(R.id.action);

        actions = (LinearLayout) root.findViewById(R.id.actions);

        action1 = root.findViewById(R.id.action1);
        action2 = root.findViewById(R.id.action2);
        //action3 = root.findViewById(R.id.action3);
        action4 = root.findViewById(R.id.action4);

        action1.findViewById(R.id.avatar).setBackground(getResources().getDrawable(R.drawable.circle_yellow_small));
        ((ImageView) action1.findViewById(R.id.avatar)).setImageResource(R.drawable.ic_home_white_18dp);
        ((TextView) action1.findViewById(R.id.action)).setText("Aller à une addresse");

        action2.findViewById(R.id.avatar).setBackground(getResources().getDrawable(R.drawable.circle_blue_small));
        ((ImageView) action2.findViewById(R.id.avatar)).setImageResource(R.drawable.ic_work_white_18dp);
        ((TextView) action2.findViewById(R.id.action)).setText("Aller chez un contact");

        //action3.findViewById(R.id.avatar).setBackground(getResources().getDrawable(R.drawable.circle_red_small));
        //((ImageView) action3.findViewById(R.id.avatar)).setImageResource(R.drawable.ic_local_cafe_white_18dp);
        //((TextView) action3.findViewById(R.id.success)).setText("Aller boire un café");

        action4.findViewById(R.id.avatar).setBackground(getResources().getDrawable(R.drawable.circle_green_small));
        ((ImageView) action4.findViewById(R.id.avatar)).setImageResource(R.drawable.ic_directions_white_18dp);
        ((TextView) action4.findViewById(R.id.action)).setText("Aller à un lieu");

        action1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), GoAddressActivity.class);
                i.putExtras(getArguments());
                if(from==null && userLocation==null){
                    Snackbar.make(root.findViewById(R.id.coordinatorLayout), R.string.your_location_not_available, Snackbar.LENGTH_LONG)
                            .setAction(R.string.select, (MainActivity)getActivity()).show();
                }
                else if(from!=null){
                    i.putExtra("departure", from);
                    startActivity(i);
                }
                else if(userLocation!=null) {
                    i.putExtra("departure", new Address(getString(R.string.your_location), userLocation.getLatitude(), userLocation.getLongitude()));
                    startActivity(i);
                }

            }
        });

        action2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), GoContactActivity.class);
                i.putExtras(getArguments());
                if(from==null && userLocation==null){
                    Snackbar.make(root.findViewById(R.id.coordinatorLayout), R.string.your_location_not_available, Snackbar.LENGTH_LONG)
                            .setAction(R.string.select, (MainActivity)getActivity()).show();
                }
                else if(from!=null){
                    i.putExtra("departure", from);
                    startActivity(i);
                }
                else if(userLocation!=null) {
                    i.putExtra("departure", new Address(getString(R.string.your_location), userLocation.getLatitude(), userLocation.getLongitude()));
                    startActivity(i);
                }
            }
        });

        action4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), GoPlaceActivity.class);
                i.putExtras(getArguments());
                if (from == null && userLocation == null) {
                    Snackbar.make(root.findViewById(R.id.coordinatorLayout), R.string.your_location_not_available, Snackbar.LENGTH_LONG)
                            .setAction(R.string.select, (MainActivity) getActivity()).show();
                } else if (from != null) {
                    i.putExtra("departure", from);
                    startActivity(i);
                } else if (userLocation != null) {
                    i.putExtra("departure", new Address(getString(R.string.your_location), userLocation.getLatitude(), userLocation.getLongitude()));
                    startActivity(i);
                }
            }
        });


        return root;
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
                    googleApiClient = new GoogleApiClient.Builder(getActivity())
                            .addApi(LocationServices.API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();
                    LocationManager locationManager = (LocationManager) getActivity()
                            .getSystemService(Context.LOCATION_SERVICE);
                    locationManager.addGpsStatusListener(this);
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
    public void onConnected(Bundle bundle) {
        userLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
    if(departure==null&&userLocation!=null) {
            //Toast.makeText(getActivity(), "connected", Toast.LENGTH_SHORT).show();
            Geocoder gcd = new Geocoder(getActivity(), Locale.getDefault());
            List<android.location.Address> addresses = null;
            try {
                addresses = gcd.getFromLocation(userLocation.getLatitude(), userLocation.getLongitude(), 1);
                if (addresses.size() > 0) {
                    geolocation.setText(getString(R.string.current_location)+" ("+addresses.get(0).getLocality()+")");
                    GoodwayHttpClientPost.updateMyCity(getActivity(), new Action<Boolean>() {
                        @Override
                        public void action(Boolean e) {

                        }
                    }, null, token, addresses.get(0).getLocality());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(departure==null){
            Toast.makeText(getActivity(), "unset", Toast.LENGTH_SHORT).show();
            geolocation.setText(getString(R.string.unset));
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getActivity(), "suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "failed", Toast.LENGTH_SHORT).show();
        geolocation.setText(getString(R.string.unset));
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


    public static int convertDpToPixel(int dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = (int) (dp * (metrics.densityDpi / 160f));
        return px;
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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
                case R.id.time:
                    new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
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
                    new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

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
        }
    }

    public void switchToMain(Bundle bundle, int request){
        fragmentManager.popBackStack(SearchFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        //floatingActionButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onGpsStatusChanged(int event) {
        if(event == GpsStatus.GPS_EVENT_STARTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    googleApiClient.reconnect();
                }
            }
            else{
                googleApiClient.reconnect();
            }
        }
    }
}
