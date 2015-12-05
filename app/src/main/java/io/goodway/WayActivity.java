package io.goodway;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.goodway.model.adapter.WayAdapter;
import io.goodway.model.callback.WayCallback;
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
    private TextView time, date;
    private TextView noWaysFound;
    private WayAdapter adapter;
    private Address from, to;

    private String mail, password;
    private Calendar departureTime, today;

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
        time = (TextView) findViewById(R.id.time);
        date = (TextView) findViewById(R.id.date);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        layoutManager = new LinearLayoutManager(this);
        adapter = new WayAdapter(this, new WayCallback() {
            @Override
            public void action(Way w) {
                Intent intent = new Intent(WayActivity.this, DetailedWayActivity.class);
                intent.putExtra("WAY", w);
                //ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(WayActivity.this,
                //        icon, WayActivity.this.getString(R.string.transition_way_image));
                WayActivity.this.startActivity(intent);
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);
        departureTime =  Calendar.getInstance();
        today = Calendar.getInstance();
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

        String dateStr = (new SimpleDateFormat("yyyyMMdd HHmm")).format(departureTime.getTime());
        dateStr = dateStr.replaceAll("\\s+", "T");

        //pairs.add(new Pair("from", fromLongitude.substring(0, fromLongitude.length()-1) + ";" + fromLatitude.substring(0, fromLatitude.length() - 1)));
        //pairs.add(new Pair("to", toLongitude.substring(0, toLongitude.length() - 1) + ";" + toLatitude.substring(0, toLatitude.length() - 1)));
        pairs.add(new Pair("from", fromLongitude + ";" + fromLatitude));
        pairs.add(new Pair("to", toLongitude + ";" + toLatitude));
        pairs.add(new Pair("datetime", dateStr));

        Log.d("from", from.getLongitude() + ";" + from.getLatitude());
        Log.d("to", to.getLongitude() + ";" + to.getLatitude());
        Log.d("sdf time", dateStr);

        String[] times = Address.toHumanTime(dateStr);
        time.setText(times[3]+"h"+times[4]);

        if(departureTime.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) && departureTime.get(Calendar.MONTH) == today.get(Calendar.MONTH) && departureTime.get(Calendar.YEAR) == today.get(Calendar.YEAR)){
            date.setText(getString(R.string.today));
        }
        else{
            date.setText(departureTime.get(Calendar.DAY_OF_MONTH)
                    + " "+departureTime.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                    + " "+departureTime.get(Calendar.YEAR));
        }

        adapter.clear();
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

    public void onClick(View v){
        switch (v.getId()){
            case R.id.time:
                new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        departureTime.set(departureTime.get(Calendar.YEAR), departureTime.get(Calendar.MONTH), departureTime.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
                        onRefresh();
                    }
                }, departureTime.get(Calendar.HOUR_OF_DAY), departureTime.get(Calendar.MINUTE), true).show();
                break;
            case R.id.date:
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        departureTime.set(year, monthOfYear, dayOfMonth, departureTime.get(Calendar.HOUR_OF_DAY), departureTime.get(Calendar.MINUTE));
                        onRefresh();
                    }
                }, departureTime.get(Calendar.YEAR), departureTime.get(Calendar.MONTH), departureTime.get(Calendar.DAY_OF_MONTH)).show();
                break;
        }
    }
}
