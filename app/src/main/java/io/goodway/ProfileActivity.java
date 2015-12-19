package io.goodway;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.goodway.model.User;
import io.goodway.model.callback.AddressSelected;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.UserLocation;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class ProfileActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, AddressSelected {

    // ----------------------------------- Model
    /**
     * Unique identifier for this activity
     */
    private static final String TAG = "STOP_ACTIVITY";

    public static final int HOME=1, WORK=2;
    // ----------------------------------- UI

    /**
     * Toolbar widget
     */
    private Toolbar toolbar;
    private User user;

    private String mail, password;
    private LinearLayout locations;

    private boolean self;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Bundle extras = this.getIntent().getExtras();
        user = extras.getParcelable("USER");
        self = extras.getBoolean("SELF", false);
        toolbar = (Toolbar) findViewById(R.id.mapToolbar);
        toolbar.setTitle(user.getName());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);

        locations = (LinearLayout) findViewById(R.id.locations);

        if(self){
            GoodwayHttpsClient.getSelfLocations(this, new Action<UserLocation>() {
                @Override
                public void action(UserLocation e) {
                    Log.d("adding address", "adding address:"+e.toString());
                    addUserLocation(e);
                }
            }, new ErrorAction() {
                @Override
                public void action(int length) {

                }
            }, mail, password, user.getFirstName());
        }
        else if(user.isFriend()){

        }
        else{
            findViewById(R.id.not_friend).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Address address = data.getParcelableExtra("ADDRESS");
            switch (requestCode){
                case HOME:

                case WORK:

                    break;
            }
        }

    }

    @Override
    public void onBackPressed(){
        if(self){
            Intent returnIntent = new Intent();
            returnIntent.putExtra("USER", user);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
        else{
            finish();
        }
    }

    @Override
    public void action(Address address) {

    }

    public void addAddress(View v){

    }

    private void addUserLocation(final UserLocation userLocation){
        View location = getLayoutInflater().inflate(R.layout.view_location, null);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, userLocation.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        ((TextView)location.findViewById(R.id.name)).setText(userLocation.toString());
        if(userLocation.shared()){
            ((TextView)location.findViewById(R.id.shared)).setText("partagé");
        }
        else{
            ((TextView)location.findViewById(R.id.shared)).setText("caché");
        }
        locations.addView(location, 1);
    }
}
