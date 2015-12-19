package io.goodway;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.goodway.model.ContainerType;
import io.goodway.model.adapter.WayContainerAdapter;
import io.goodway.model.callback.WayCallback;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.Way;
import io.goodway.view.DividerItemDecoration;

/**
 * Created by antoine on 8/23/15.
 */
public class WayActivity extends AppCompatActivity implements WayCallback, SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView time, date;
    private WayContainerAdapter adapter;
    private Address from, to;

    private String mail, password;
    private Calendar departureTime, today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_way);

        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);

        Bundle extras = this.getIntent().getExtras();
        this.to = extras.getParcelable("TO");
        this.from = extras.getParcelable("FROM");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.routes);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        time = (TextView) findViewById(R.id.time);
        date = (TextView) findViewById(R.id.date);

        departureTime =  Calendar.getInstance();
        today = Calendar.getInstance();

        String dateStr = (new SimpleDateFormat("yyyyMMdd HHmmss")).format(departureTime.getTime());
        dateStr = dateStr.replaceAll("\\s+", "T");

        String fromLongitude = ((Double)from.getLongitude()).toString();
        String fromLatitude = ((Double)from.getLatitude()).toString();
        String toLongitude = ((Double)to.getLongitude()).toString();
        String toLatitude = ((Double)to.getLatitude()).toString();

        String[] times = Address.splitIso8601(dateStr);
        time.setText(times[3] + "h" + times[4]);

        if(departureTime.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) && departureTime.get(Calendar.MONTH) == today.get(Calendar.MONTH) && departureTime.get(Calendar.YEAR) == today.get(Calendar.YEAR)){
            date.setText(getString(R.string.today));
        }
        else{
            date.setText(departureTime.get(Calendar.DAY_OF_MONTH)
                    + " "+departureTime.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                    + " "+departureTime.get(Calendar.YEAR));
        }

        adapter = new WayContainerAdapter(this, this, from, to, fromLatitude, fromLongitude, toLatitude, toLongitude, dateStr);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.add(ContainerType.navitia);
        adapter.add(ContainerType.carSharing);
        adapter.add(ContainerType.bike);
        adapter.add(ContainerType.uber);
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

    public void onClick(View v){

        switch (v.getId()){
            case R.id.time:
                new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        departureTime.set(departureTime.get(Calendar.YEAR), departureTime.get(Calendar.MONTH), departureTime.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
                        String dateStr = (new SimpleDateFormat("yyyyMMdd HHmm")).format(departureTime.getTime());
                        dateStr = dateStr.replaceAll("\\s+", "T");
                        adapter.setDepartureTime(dateStr);
                        refresh();
                    }
                }, departureTime.get(Calendar.HOUR_OF_DAY), departureTime.get(Calendar.MINUTE), true).show();
                break;
            case R.id.date:
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        departureTime.set(year, monthOfYear, dayOfMonth, departureTime.get(Calendar.HOUR_OF_DAY), departureTime.get(Calendar.MINUTE));
                        String dateStr = (new SimpleDateFormat("yyyyMMdd HHmm")).format(departureTime.getTime());
                        dateStr = dateStr.replaceAll("\\s+", "T");
                        adapter.setDepartureTime(dateStr);
                        refresh();
                    }
                }, departureTime.get(Calendar.YEAR), departureTime.get(Calendar.MONTH), departureTime.get(Calendar.DAY_OF_MONTH)).show();
                break;
        }
    }

    private void refresh(){
        adapter.clear();
        adapter.add(ContainerType.navitia);
        adapter.add(ContainerType.carSharing);
        adapter.add(ContainerType.bike);
        adapter.add(ContainerType.uber);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void action(View v, Way w) {
        Intent intent = new Intent(WayActivity.this, DetailedWayActivity.class);
        intent.putExtra("WAY", w);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, v, getString(R.string.transition_way_card));
        if (Build.VERSION.SDK_INT >= 16) {
            WayActivity.this.startActivity(intent, options.toBundle());
        }
        else{
            WayActivity.this.startActivity(intent);
        }
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        refresh();
    }
}
