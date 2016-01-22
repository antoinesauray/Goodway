package io.goodway;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import io.goodway.model.adapter.AdressSearchAdapter;
import io.goodway.model.callback.AddressSelected;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.UserLocation;
import io.goodway.view.fragment.SearchFragment;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class GoAddressActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // ----------------------------------- Model

    private Toolbar toolbar;

    private RecyclerView recyclerView;
    private AdressSearchAdapter searchAdapter;
    private EditText autocomplete;

    private SearchFragment searchFragment;

    private Address departure;

    private int request;
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    private String token;

    private AsyncTask selfLocations;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_address);
        //request = getIntent().getExtras().getInt("REQUEST");
        token = getIntent().getExtras().getString("token");
        departure = getIntent().getExtras().getParcelable("departure");

        recyclerView = (RecyclerView) findViewById(R.id.list);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        recyclerView = (RecyclerView) findViewById(R.id.list);
        autocomplete = (EditText) findViewById(R.id.autocomplete);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchAdapter = new AdressSearchAdapter(new AddressSelected() {
            @Override
            public void action(Address address) {
                Log.d("click", "click");
                searchAdapter.clear();
                finish(address);
            }
        });
        recyclerView.setAdapter(searchAdapter);
        selfLocations = GoodwayHttpClientPost.getMyLocations(this, new Action<List<UserLocation>>() {
            @Override
            public void action(List<UserLocation> locations) {
                for (UserLocation location : locations) {
                    searchAdapter.add(location);
                }

            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {

            }
        }, null, token);

        autocomplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(selfLocations!=null){selfLocations.cancel(true);}
                searchAdapter.clear();
                if(s.toString().length()>0) {
                    PendingResult<AutocompletePredictionBuffer> result =
                            Places.GeoDataApi.getAutocompletePredictions(googleApiClient, s.toString(), null, null);
                    result.setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {
                        @Override
                        public void onResult(AutocompletePredictionBuffer autocompletePredictions) {
                            for (int i = 0; i < autocompletePredictions.getCount(); i++) {
                                final AutocompletePrediction prediction = autocompletePredictions.get(i);
                                Places.GeoDataApi.getPlaceById(googleApiClient, prediction.getPlaceId())
                                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                            @Override
                                            public void onResult(PlaceBuffer places) {
                                                if (places.getStatus().isSuccess()) {
                                                    try {
                                                        Place myPlace = places.get(0);
                                                        LatLng queried_location = myPlace.getLatLng();
                                                        searchAdapter.add(new Address(prediction.getPrimaryText(STYLE_BOLD).toString(), prediction.getSecondaryText(STYLE_BOLD).toString(), queried_location.latitude, queried_location.longitude));
                                                    } catch (IllegalStateException e) {
                                                    }

                                                }
                                                places.release();
                                            }
                                        });
                            }
                            //autocompletePredictions.release();
                        }
                    });
                }
                else{
                    selfLocations = GoodwayHttpClientPost.getMyLocations(GoAddressActivity.this, new Action<List<UserLocation>>() {
                        @Override
                        public void action(List<UserLocation> locations) {
                            for(UserLocation location : locations) {
                                searchAdapter.add(location);
                            }
                        }
                    }, new ErrorAction() {
                        @Override
                        public void action(int length) {

                        }
                    }, null, token);
                }
            }
        });
    }


    @Override
    public void onResume(){
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    public void onPause(){
        super.onPause();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public String toString(){
        return "Search fragment";
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void finish(Address address){
        Intent i = new Intent(this, WayActivity.class);
        i.putExtras(getIntent().getExtras());
        i.putExtra("arrival", address);
        startActivity(i);
    }

}
