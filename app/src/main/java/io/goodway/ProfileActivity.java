package io.goodway;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import io.goodway.model.User;
import io.goodway.model.adapter.WayAdapter;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.Way;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class ProfileActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    // ----------------------------------- Model
    /**
     * Unique identifier for this activity
     */
    private static final String TAG = "STOP_ACTIVITY";

    // ----------------------------------- UI

    /**
     * Toolbar widget
     */
    private Toolbar toolbar;
    private User user;
    private SwipeRefreshLayout swipeLayout;

    private String mail, password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Bundle extras = this.getIntent().getExtras();
        user = extras.getParcelable("USER");
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
        /*
        GoodwayHttpsClient.getTrips(this, new Action<Way>() {
            @Override
            public void action(Way e) {
                adapter.add(e);
                swipeLayout.setRefreshing(false);
            }
        }, new ErrorAction() {
            @Override
            public void action() {
                swipeLayout.setRefreshing(false);
            }
        }, mail, password, user.getId());
       */
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(R.color.accent);
        swipeLayout.setRefreshing(true);
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
        swipeLayout.setRefreshing(true);
        GoodwayHttpsClient.getTrips(this, new Action<Way>() {
            @Override
            public void action(Way e) {
                swipeLayout.setRefreshing(false);
            }
        }, new ErrorAction() {
            @Override
            public void action() {
                swipeLayout.setRefreshing(false);
            }
        }, mail, password, user.getId());
    }
}
