package io.goodway;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.goodway.model.adapter.WayAdapter;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.Pair;
import io.goodway.navitia_android.Request;
import io.goodway.navitia_android.Way;

/**
 * Created by antoine on 8/23/15.
 */
public class WayActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeLayout;
    private LinearLayoutManager layoutManager;
    private TextView noWaysFound;
    private WayAdapter adapter;
    private Address from, to;

    private String mail, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_way);

        Bundle extras = this.getIntent().getExtras();
        this.to = extras.getParcelable("TO");
        this.from = extras.getParcelable("FROM");

        Log.d(from.toString(), "from");
        Log.d(to.toString(), "to");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.routes);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.list);
        noWaysFound = (TextView) findViewById(R.id.no_ways_found);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        layoutManager = new LinearLayoutManager(this);
        adapter = new WayAdapter(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);
        onRefresh();

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(R.color.accent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        noWaysFound.setVisibility(View.INVISIBLE);
        ArrayList<Pair> pairs = new ArrayList<>();
        String fromLongitude = ((Double)from.getLongitude()).toString();
        String fromLatitude = ((Double)from.getLatitude()).toString();
        String toLongitude = ((Double)to.getLongitude()).toString();
        String toLatitude = ((Double)to.getLatitude()).toString();

        Date d = new Date();
        String date = (new SimpleDateFormat("yyyyMMdd HHmm")).format(d);

        date = date.replaceAll("\\s+", "T");

        //pairs.add(new Pair("from", fromLongitude.substring(0, fromLongitude.length()-1) + ";" + fromLatitude.substring(0, fromLatitude.length() - 1)));
        //pairs.add(new Pair("to", toLongitude.substring(0, toLongitude.length() - 1) + ";" + toLatitude.substring(0, toLatitude.length() - 1)));
        pairs.add(new Pair("from", fromLongitude+";" + fromLatitude));
        pairs.add(new Pair("to", toLongitude+";" + toLatitude));
        pairs.add(new Pair("datetime", date));

        //pairs.add(new Pair("to", "-1.673421;48.112963"));
        Log.d("from", from.getLongitude() + ";" + from.getLatitude());
        Log.d("to", to.getLongitude() + ";" + to.getLatitude());
        Log.d("sdf time", date);
        Request.getWays(new Action<Way>() {

            @Override
            public void action(Way e) {
                swipeLayout.setRefreshing(false);
                //Toast.makeText(WayActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                adapter.add(e);
            }

        }, pairs, from, to, new ErrorAction() {
            @Override
            public void action() {
                noWaysFound.setVisibility(View.VISIBLE);
            }
        });
    }
}
