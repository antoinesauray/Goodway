package io.goodway;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import io.goodway.model.User;
import io.goodway.model.adapter.LocationAdapter;
import io.goodway.model.callback.AddressSelected;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.Way;
import io.goodway.view.PercentView;


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
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LocationAdapter adapter;

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
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        //swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        //swipeRefreshLayout.setOnRefreshListener(this);
        adapter = new LocationAdapter(this);
        if(self){
            GoodwayHttpsClient.getSelfLocations(this, new Action<Address>() {
                @Override
                public void action(Address e) {
                    Log.d("adding address", "adding address:"+e.toString());
                    adapter.add(e);
                }
            }, new ErrorAction() {
                @Override
                public void action(int length) {

                }
            }, mail, password, user.getFirstName());
        }
        else if(user.isFriend()){
            GoodwayHttpsClient.getUserLocations(this, new Action<Address>() {
                @Override
                public void action(Address e) {
                    adapter.add(e);
                }
            }, new ErrorAction() {
                @Override
                public void action(int length) {

                }
            }, mail, password, user.getFirstName(), user.getId());
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
}
