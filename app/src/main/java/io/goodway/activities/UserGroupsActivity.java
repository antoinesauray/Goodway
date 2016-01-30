package io.goodway.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

import io.goodway.R;
import io.goodway.model.Group;
import io.goodway.model.User;
import io.goodway.model.adapter.GroupAdapter;
import io.goodway.model.callback.GroupCallback;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class UserGroupsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    // ----------------------------------- Model
    /**
     * Unique identifier for this activity
     */
    private static final String TAG = "STOP_ACTIVITY";

    public static final int SET_ADDRESS=1;
    // ----------------------------------- UI

    /**
     * Toolbar widget
     */
    private Toolbar toolbar;
    private User user;

    private String token;

    private FloatingActionButton fab;
    private ImageView avatar;
    private GroupAdapter adapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_groups);
        Bundle extras = this.getIntent().getExtras();
        user = extras.getParcelable("user");
        token = extras.getString("token");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        toolbar.setTitle(R.string.groups);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        adapter = new GroupAdapter(this, new GroupCallback() {
            @Override
            public void action(Group g) {
                Intent i = new Intent(UserGroupsActivity.this, GroupActivity.class);
                i.putExtra("group", g);
                i.putExtra("user", user);
                i.putExtra("token", token);
                i.putExtra("joined", true);
                startActivity(i);
            }
        });
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(R.color.accent);
    }

    @Override
    public void onResume(){
        super.onResume();
        onRefresh();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void fabClick(View v){
        Intent i = new Intent(this, SearchGroupActivity.class);
        i.putExtra("token", token);
        startActivity(i);
    }

    @Override
    public void onRefresh() {
        adapter.clear();
        swipeRefreshLayout.setRefreshing(true);
        GoodwayHttpClientPost.getMyGroups(this, new Action<List<Group>>() {
            @Override
            public void action(List<Group> e) {
                if(e.size()!=0) {
                    for (Group g : e) {
                        adapter.add(g);
                    }
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        }, null, token);
    }
}