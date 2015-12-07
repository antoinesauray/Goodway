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
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.goodway.model.User;
import io.goodway.model.adapter.WayAdapter;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.Way;
import io.goodway.view.PercentView;


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

    private String mail, password;
    private TextView impact, home, work;
    private PercentView percentView;

    private ProgressBar impactProgress;

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

        impact = (TextView) findViewById(R.id.impact);
        home = (TextView) findViewById(R.id.home);
        work = (TextView) findViewById(R.id.work);
        percentView = (PercentView) findViewById(R.id.percentView);
        impactProgress = (ProgressBar) findViewById(R.id.impactProgress);



        GoodwayHttpsClient.getUserCo2(this, new Action<Integer>() {
            @Override
            public void action(Integer e) {
                impactProgress.setVisibility(View.INVISIBLE);
                impact.setText(e + "g co2");
                percentView.setPercentage(30f);
            }
        }, mail, password, user.getId());
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
        GoodwayHttpsClient.getTrips(this, new Action<Way>() {
            @Override
            public void action(Way e) {

            }
        }, new ErrorAction() {
            @Override
            public void action() {

            }
        }, mail, password, user.getId());
    }
}
