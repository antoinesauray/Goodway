package io.goodway;

import android.animation.Animator;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import java.util.ArrayList;

import io.goodway.model.adapter.AdressSearchAdapter;
import io.goodway.navitia_android.Address;

/**
 * Provides a list of Line available for selection to the user
 * @author Antoine Sauray
 * @version 2.0
 */
public class SearchActivity extends AppCompatActivity implements View.OnLayoutChangeListener, TextWatcher, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // ----------------------------------- UI
    /**
     * Unique identifier for this activity
     */
    private static final String TAG = "LINE_ACTIVITY";
    private static final int SPEECH_RECOGNITION = 1;


    // ----------------------------------- Constants
    /**
     * The current network
     */
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private AdressSearchAdapter adapter;
    private Toolbar toolbar;
    private AutoCompleteTextView textView;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Bundle extras = this.getIntent().getExtras();
        if(extras.getBoolean("SPEECH") == true){
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            startActivityForResult(intent, SPEECH_RECOGNITION);
        }

        toolbar = (Toolbar) findViewById(R.id.mapToolbar);
        toolbar.setTitle("");

        textView = (AutoCompleteTextView) findViewById(R.id.textView);
        textView.addTextChangedListener(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.list);


        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        adapter = new AdressSearchAdapter(this);
        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, "search", Toast.LENGTH_SHORT).show();
            //doMySearch(query);
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        switch (requestCode) {
            case SPEECH_RECOGNITION:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> speech = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    textView.setText(speech.get(0));
                    /*
                    for (String s : speech) {
                        textView.setText(textView.getText() + " " + s);
                    }
                    */
                }
                break;
        }
    }

    public void toolbarClick(View v) {

        switch (v.getId()){
            case R.id.speechRecognition:
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startActivityForResult(intent, SPEECH_RECOGNITION);
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    @Override
    public String toString() {
        return "SEARCH";
    }

    @Override
    public void onLayoutChange(final View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        Log.d("onLayoutChange", "onLayoutChange");
        v.removeOnLayoutChangeListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //int cx = (v.getLeft() + v.getRight()) / 2;
            //int cy = (v.getTop() + v.getBottom()) / 2;

            int finalRadius = Math.max(v.getWidth(), v.getHeight());
            Animator anim = ViewAnimationUtils.createCircularReveal(v, (int)toolbar.getLeft()+toolbar.getWidth()/2, (int)toolbar.getTop()+toolbar.getHeight()/2, 0, finalRadius);
            anim.setDuration(500);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    v.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            anim.start();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        adapter.clear();
        /*
        ArrayList<Pair> r1 = new ArrayList<Pair>();
        r1.add(new Pair(getSequenceType(s.toString()), s.toString()));
        r1.add(new Pair("countrycodes", "fr"));

        Connexion.getPlaces(new Action<Address>() {
                              @Override
                              public void action(Address p) {
                                  adapter.add(p);
                              }
                          },
                r1);

        PendingResult result =
                Places.GeoDataApi.getAutocompletePredictions(googleApiClient, s.toString(),
                        mBounds);
                        */
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private String getSequenceType(String s){
        Log.d(s, "getSequenceType");
        String ret = null;
        if(s.contains("rue") || s.contains("avenue") || s.contains("boulevard")){
            ret = "street";
        }
        if(!s.contains(" ")){
            ret="city";
        }
        Log.d("ret is \""+ret+"\"", "getSequenceType");
        return ret;
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
