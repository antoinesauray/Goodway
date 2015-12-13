package io.goodway.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

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

import io.goodway.MainActivity;
import io.goodway.ProfileActivity;
import io.goodway.R;
import io.goodway.SetLocationActivity;
import io.goodway.model.User;
import io.goodway.model.adapter.AdressSearchAdapter;
import io.goodway.model.callback.AddressSelected;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.Home;
import io.goodway.navitia_android.Work;


/**
 * Created by sauray on 14/03/15.
 */
public class SearchPlacesFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private View root;

    private GoogleApiClient googleApiClient;

    private RecyclerView recyclerView;
    private AdressSearchAdapter searchAdapter;
    private EditText autocomplete;

    private MainActivity mainActivity;

    private int request;
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    private User user;
    private String mail, password;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_google_places, container, false);
        request = getArguments().getInt("REQUEST");
        user = getArguments().getParcelable("USER");

        SharedPreferences shared_preferences = getActivity().getSharedPreferences("shared_preferences_test",
                getActivity().MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);

        mainActivity = (MainActivity) getActivity();
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
                autocomplete.setHint(R.string.departure);
                break;
            case MainActivity.DESTINATION:
                autocomplete.setHint(R.string.arrival);
                break;
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        searchAdapter = new AdressSearchAdapter(new AddressSelected() {
            @Override
            public void action(Address address) {
                if(address.getClass() == Home.class){
                    final ProgressDialog dialog = new ProgressDialog(getActivity());
                    dialog.setMessage(getString(R.string.request_location));
                    dialog.setProgressStyle(dialog.STYLE_SPINNER);
                    dialog.show();
                    GoodwayHttpsClient.getUserHome(getActivity(), new Action<Address>() {
                        @Override
                        public void action(Address e) {
                            dialog.dismiss();
                            if(e!=null) {
                                searchAdapter.clear();
                                finish(e);
                            }
                            else{
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(R.string.home_not_set)
                                        .setMessage(R.string.set_address)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // continue with delete
                                                Intent i = new Intent(getActivity(), SetLocationActivity.class);
                                                i.putExtra("REQUEST", request);
                                                i.putExtra("PLACE", ProfileActivity.HOME);
                                                i.putExtra("USER", user);
                                                startActivityForResult(i, MainActivity.SETLOCATION);
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {}
                                        })
                                        .show();
                            }
                        }
                    }, new ErrorAction() {
                        @Override
                        public void action(int length) {
                            dialog.dismiss();
                            Toast.makeText(getActivity(), R.string.connexion_error, Toast.LENGTH_SHORT).show();
                        }
                    }, mail, password, user.getFirstName());
                }
                else if(address.getClass()==Work.class){
                    final ProgressDialog dialog = new ProgressDialog(getActivity());
                    dialog.setMessage(getString(R.string.request_location));
                    dialog.setProgressStyle(dialog.STYLE_SPINNER);
                    dialog.show();
                    GoodwayHttpsClient.getUserWork(getActivity(), new Action<Address>() {
                        @Override
                        public void action(Address e) {
                            dialog.dismiss();
                            if (e != null) {
                                searchAdapter.clear();
                                finish(e);
                            } else {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(R.string.work_not_set)
                                        .setMessage(R.string.set_address)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // continue with delete
                                                Intent i = new Intent(getActivity(), SetLocationActivity.class);
                                                i.putExtra("REQUEST", request);
                                                i.putExtra("PLACE", ProfileActivity.WORK);
                                                i.putExtra("USER", user);
                                                startActivityForResult(i, MainActivity.SETLOCATION);
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .show();
                            }
                        }
                    }, new ErrorAction() {
                        @Override
                        public void action(int length) {
                            dialog.dismiss();
                            Toast.makeText(getActivity(), R.string.connexion_error, Toast.LENGTH_SHORT).show();
                        }
                    }, mail, password, user.getFirstName());
                }
                else{
                    searchAdapter.clear();
                    finish(address);
                }

            }
        });
        recyclerView.setAdapter(searchAdapter);
        LatLng home = user.getHome();
        LatLng work = user.getWork();
        Log.d("home=", "home="+home);
        Log.d("work=", "work="+work);
        if(home!=null) {searchAdapter.add(new Home(getString(R.string.home), R.mipmap.ic_home_black, home.latitude, home.longitude));}
        else{searchAdapter.add(new Home(getString(R.string.home), R.mipmap.ic_home_black));}

        if(work!=null){searchAdapter.add(new Work(getString(R.string.work), R.mipmap.ic_work_black, work.latitude, work.longitude));}
        else{searchAdapter.add(new Work(getString(R.string.work), R.mipmap.ic_work_black));}
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
                                                    searchAdapter.add(new Address(prediction.getFullText(STYLE_BOLD).toString(), queried_location.latitude, queried_location.longitude));
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
        return root;
    }


    @Override
    public void onResume(){
        super.onResume();
        googleApiClient.connect();
        if(!getArguments().getBoolean("DEMO") && isVisible()) {
            showKeyboard();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        closeKeyboard();
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getActivity()!=null && autocomplete!=null) {
            if (isVisibleToUser) {
                showKeyboard();
            }
        }
    }

    public void closeKeyboard(){
        if(autocomplete!=null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(autocomplete.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void showKeyboard(){
        autocomplete.requestFocus();
        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(autocomplete, InputMethodManager.SHOW_IMPLICIT);
    }

    private void finish(Address address){
        switch (request){
            case MainActivity.DEPARTURE:
                mainActivity.setFrom(address);
                Bundle b1 = new Bundle();
                b1.putParcelable("DEPARTURE", address);
                mainActivity.switchToMain(b1, request);
                break;
            case MainActivity.DESTINATION:
                mainActivity.setTo(address);
                Bundle b2 = new Bundle();
                b2.putParcelable("DESTINATION", address);
                mainActivity.switchToMain(b2, request);
                break;

        }
    }
}
