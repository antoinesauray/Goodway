package io.goodway.activities.fragment.nested;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import io.goodway.activities.MainActivity;
import io.goodway.R;
import io.goodway.activities.SearchActivity;
import io.goodway.adapters.AdressSearchAdapter;
import io.goodway.model.callback.AddressSelected;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.UserLocation;
import io.goodway.activities.fragment.SearchFragment;


/**
 * Created by sauray on 14/03/15.
 */
public class SearchPlacesFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private View root;

    private GoogleApiClient googleApiClient;

    private RecyclerView recyclerView;
    private AdressSearchAdapter searchAdapter;
    private EditText autocomplete;

    private SearchFragment searchFragment;

    private int request;
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    private String token;

    private AsyncTask selfLocations;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_google_places, container, false);
        request = getArguments().getInt("REQUEST");
        token = getArguments().getString("token");

        searchFragment = (SearchFragment) getParentFragment();
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        recyclerView = (RecyclerView) root.findViewById(R.id.list);
        autocomplete = (EditText) root.findViewById(R.id.autocomplete);

        switch (request){
            case MainActivity.DEPARTURE:
                autocomplete.setHint(R.string.type_address_start);
                break;
            case MainActivity.DESTINATION:
                autocomplete.setHint(R.string.type_address_end);
                break;
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        searchAdapter = new AdressSearchAdapter(new AddressSelected() {
            @Override
            public void action(Address address) {
                    Log.d("click", "click");
                    searchAdapter.clear();
                    finish(address);
            }
        });
        recyclerView.setAdapter(searchAdapter);
        selfLocations = GoodwayHttpClientPost.getMyLocations(getActivity(), new Action<List<UserLocation>>() {
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
                    selfLocations = GoodwayHttpClientPost.getMyLocations(getActivity(), new Action<List<UserLocation>>() {
                        @Override
                        public void action(List<UserLocation> locations) {
                            if(locations!=null) {
                                for (UserLocation location : locations) {
                                    searchAdapter.add(location);
                                }
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
        return root;
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
        switch (request){
            case MainActivity.DEPARTURE:
                ((SearchActivity)getActivity()).setFrom(address);
                break;
            case MainActivity.DESTINATION:
                ((SearchActivity)getActivity()).setTo(address);
                break;
            default:
                ((MainActivity)getActivity()).setFrom(address);
                break;
        }
    }
}