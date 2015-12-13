package io.goodway;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Arrays;

import io.goodway.model.adapter.WayPartAdapter;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.Way;
import io.goodway.navitia_android.WayPart;

/**
 * Created by antoine on 8/26/15.
 */

public class DetailedWayActivity extends AppCompatActivity{

    private Toolbar toolbar;
    private AppBarLayout appBarLayout;

    private TextView from, to;


    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private WayPartAdapter adapter;

    // Model
    private Way way;
    private String mail, password;
    private TextView duration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_way_detailed);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        Bundle extras = this.getIntent().getExtras();
        way = extras.getParcelable("WAY");

        toolbar.setTitle(way.getFrom().getName(this) + " vers " + way.getTo().getName(this));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.list);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        layoutManager = new GridLayoutManager(this, 3);
        adapter = new WayPartAdapter(this, layoutManager);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);

        duration = (TextView) findViewById(R.id.duration);
        duration.setText(WayPartAdapter.secondToStr(this, way.getDuration()));

        if(way.getParts()!=null){
            for(WayPart p : way.getParts()){
                adapter.add(p);
                Log.d("new part", "new part");
            }
        }

        if(way.getImgUrl()!=null) {
            Picasso.with(this).load(way.getImgUrl())
                    .error(R.mipmap.ic_event_black_36dp).into(new Target() {

                @Override
                public void onPrepareLoad(Drawable arg0) {
                }

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom arg1) {
                    // TODO Create your drawable from bitmap and append where you like.
                    appBarLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
                }

                @Override
                public void onBitmapFailed(Drawable arg0) {
                }
            });
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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }

    public void fabClick(View v){
        GoodwayHttpsClient.sendRoute(this, new Action<Integer>() {
            @Override
            public void action(Integer e) {
                Toast.makeText(DetailedWayActivity.this, "route sent", Toast.LENGTH_SHORT).show();
            }
        }, null, mail, password, Arrays.asList(2), way);
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