package io.goodway;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
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
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import io.goodway.model.User;
import io.goodway.model.adapter.AdressSearchAdapter;
import io.goodway.model.callback.AddressSelected;
import io.goodway.navitia_android.Address;


/**
 * The main activity of the program
 * @author Antoine Sauray
 * @version 2.0
 */
public class SetLocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // ----------------------------------- UI

    /**
     * These variables are used to get result from other activities.
     *
     * @see
     */

    private static final String TAG = "SET_LOCATION_ACTIVITY";
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    private GoogleApiClient googleApiClient;
    private RecyclerView recyclerView;
    private AdressSearchAdapter searchAdapter;
    private EditText autocomplete;
    /**
     * Toolbar widget
     */
    private Toolbar toolbar;



    private User currentUser;
    private String mail, password;
    private String s_name;
    private boolean shared;

    // ----------------------------------- Constants
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_location);

        //from = new Address(R.string.your_location, R.mipmap.ic_home_black_24dp, AddressType.POSITION);


        Bundle extras = this.getIntent().getExtras();
        currentUser = extras.getParcelable("USER");
        s_name = extras.getString("s_name");
        shared = extras.getBoolean("shared");
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        toolbar.setLogo(R.drawable.goodway_text_very_small);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        recyclerView = (RecyclerView) findViewById(R.id.list);
        autocomplete = (EditText) findViewById(R.id.autocomplete);

        autocomplete.setHint(s_name);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchAdapter = new AdressSearchAdapter(new AddressSelected() {
            @Override
            public void action(Address address) {
                finishWithAddress(address);
            }
            });
        recyclerView.setAdapter(searchAdapter);

        autocomplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                PendingResult<AutocompletePredictionBuffer> result =
                        Places.GeoDataApi.getAutocompletePredictions(googleApiClient, s.toString(), null, null);
                result.setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {
                    @Override
                    public void onResult(AutocompletePredictionBuffer autocompletePredictions) {
                        searchAdapter.clear();
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
        });
        autocomplete.requestFocus();
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(autocomplete, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    private void finishWithAddress(Address address){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(autocomplete.getWindowToken(), InputMethodManager.RESULT_HIDDEN);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("address", address);
        returnIntent.putExtra("s_name", s_name);
        returnIntent.putExtra("shared", shared);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
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
}
