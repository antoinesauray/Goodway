package io.goodway;

import android.app.Activity;
import android.content.Intent;
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

import io.goodway.model.Event;
import io.goodway.model.User;
import io.goodway.model.adapter.EventAdapter;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.ErrorAction;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class EventsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    // ----------------------------------- Model
    /**
     * Unique identifier for this activity
     */
    private static final String TAG = "STOP_ACTIVITY";
    public static final int EVENT_REQUEST = 1;

    // ----------------------------------- UI

    /**
     * Toolbar widget
     */
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private EventAdapter adapter;
    private User user;
    private SwipeRefreshLayout swipeLayout;

    private String mail, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        Bundle extras = this.getIntent().getExtras();
        user = extras.getParcelable("USER");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.events));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.list);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                //return mDataset.get(position).getSpans();
                return 3;
            }
        });
        adapter = new EventAdapter(this);
        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);
        GoodwayHttpsClient.getEvents(this, new Action<Event>() {
            @Override
            public void action(Event e) {
                adapter.add(e);
                swipeLayout.setRefreshing(false);
            }
        }, new ErrorAction() {
            @Override
            public void action() {
                swipeLayout.setRefreshing(false);
            }
        }, mail, password);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(R.color.accent);
        swipeLayout.setRefreshing(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == EVENT_REQUEST) {
            if(resultCode == Activity.RESULT_OK){
                Event event =data.getParcelableExtra("EVENT");
                Intent returnIntent = new Intent();
                returnIntent.putExtra("EVENT", event);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        }
    }//onActivityResult

    @Override
    public void onRefresh() {
        adapter.clear();
        swipeLayout.setRefreshing(true);
        GoodwayHttpsClient.getEvents(this, new Action<Event>() {
            @Override
            public void action(Event e) {
                adapter.add(e);
                swipeLayout.setRefreshing(false);
            }
        }, new ErrorAction() {
            @Override
            public void action() {
                swipeLayout.setRefreshing(false);
            }
        },mail, password);
    }
}
