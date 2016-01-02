package io.goodway;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.squareup.picasso.Picasso;

import io.goodway.model.Group;
import io.goodway.model.User;
import io.goodway.model.adapter.GroupAdapter;
import io.goodway.model.callback.GroupCallback;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.view.ImageTrans_CircleTransform;


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

    private String mail, password;

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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        toolbar.setTitle(user.getName() + " " + getString(R.string.groups));

        fab = (FloatingActionButton) findViewById(R.id.fab);

        avatar = (ImageView) findViewById(R.id.avatar);
        Picasso.with(this)
                .load(user.getAvatar())
                .error(R.mipmap.ic_person_white_48dp)
                .resize(200, 200)
                .centerCrop()
                .transform(new ImageTrans_CircleTransform())
                .into(avatar);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);

        adapter = new GroupAdapter(this, new GroupCallback() {
            @Override
            public void action(Group g) {
                Intent i = new Intent(UserGroupsActivity.this, GroupActivity.class);
                i.putExtra("group", g);
                i.putExtra("user", user);
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
        startActivity(new Intent(this, SearchGroupActivity.class));
    }

    @Override
    public void onRefresh() {
        adapter.clear();
        swipeRefreshLayout.setRefreshing(true);
        GoodwayHttpClientPost.getGroups(this, new Action<Group>() {
            @Override
            public void action(Group e) {
                adapter.add(e);
                swipeRefreshLayout.setRefreshing(false);
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, mail, password);
    }
}
