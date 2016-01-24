package io.goodway.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import io.goodway.R;
import io.goodway.model.adapter.WayPartAdapter;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.Way;
import io.goodway.navitia_android.WayPart;
import io.goodway.view.DividerItemDecoration;

/**
 * Created by antoine on 8/26/15.
 */

public class DetailedWayActivity extends AppCompatActivity{

    private Toolbar toolbar;
    private AppBarLayout appBarLayout;

    private TextView from, to;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private WayPartAdapter adapter;

    private LinearLayout cardLayout;

    // Model
    private Way way;
    private String mail, password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_way_detailed);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        Bundle extras = this.getIntent().getExtras();
        way = extras.getParcelable("WAY");

        toolbar.setTitle(way.getFrom().getName() + " vers " + way.getTo().getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.list);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);

        cardLayout = (LinearLayout) findViewById(R.id.cardLayout);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        View viewWay = findViewById(R.id.view_way);
        ((TextView)viewWay.findViewById(R.id.description)).setText(way.getFrom().toString() + " - " + way.getTo().toString());
        String[] departureTime = Address.splitIso8601(way.getDepartureDateTime());
        String[] arrivalTime = Address.splitIso8601(way.getArrivalDateTime());
        ((TextView) viewWay.findViewById(R.id.departure)).setText(departureTime[3] + ":" + departureTime[4]);
        ((TextView)viewWay.findViewById(R.id.arrival)).setText(arrivalTime[3] + ":" + arrivalTime[4]);
        ((TextView) viewWay.findViewById(R.id.duration)).setText(getString(R.string.duration)+" "+Address.secondToStr(this, way.getDuration()));

        // Test public-transport or bike
        if(way.getLabel().equals("non_pt_bss")) ((ImageView) viewWay.findViewById(R.id.icon)).setImageResource(R.mipmap.ic_direction_bike_black);


        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        layoutManager = new LinearLayoutManager(this);
        adapter = new WayPartAdapter(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);


        if(way.getParts()!=null){
            for(WayPart p : way.getParts()){
                adapter.add(p);
                Log.d("new part", "new part");
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }

    public void onlinePaiement(View v){

    }

    public void irlPaiement(View v){

    }

    public void fabClick(View v){
        Intent i = new Intent(this, TripActivity.class);
        i.putExtra("way", way);
        startActivity(i);
    }

    private LatLngBounds getCenter(LatLng from, LatLng to){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Log.d(from.toString(), "from latlng");
        Log.d(to.toString(), "to latlng");
        builder.include(from);
        builder.include(to);
        LatLngBounds bounds = builder.build();
        return bounds;
    }

}