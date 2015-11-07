package io.goodway;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import io.goodway.model.adapter.WayPartAdapter;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Way;
import io.goodway.navitia_android.WayPart;

/**
 * Created by antoine on 8/26/15.
 */

public class DetailedWayActivity extends AppCompatActivity{

    private Toolbar toolbar;

    private TextView from, to;


    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private WayPartAdapter adapter;

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

        /*
        from = (TextView) findViewById(R.id.from);
        to = (TextView) findViewById(R.id.to);

        to.setText(way.getTo().getName(this));
        from.setText(getString(R.string.from) + " " + way.getFrom().getName(this));
        */

        toolbar.setTitle(way.getFrom().getName(this)+" vers "+way.getTo().getName(this));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.list);

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

        if(way.getParts()!=null){
            for(WayPart p : way.getParts()){
                adapter.add(p);
                Log.d("new part", "new part");
            }
        }

        GoodwayHttpsClient.sendRoute(this, new Action<Integer>() {
            @Override
            public void action(Integer e) {
                Toast.makeText(DetailedWayActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }, null, mail, password, Arrays.asList(2), way);
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
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }
}