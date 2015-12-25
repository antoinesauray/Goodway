package io.goodway;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import io.goodway.model.Group;
import io.goodway.model.GroupEvent;
import io.goodway.model.User;
import io.goodway.model.adapter.GroupLocationAdapter;
import io.goodway.model.callback.FinishCallback;
import io.goodway.model.callback.GroupLocationCallback;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.GroupLocation;
import io.goodway.view.ImageTrans_CircleTransform;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class GroupActivity extends AppCompatActivity{

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
    private Group group;
    private User user;

    private String mail, password;

    private ImageView avatar;
    private GroupLocationAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout upcoming;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Bundle extras = this.getIntent().getExtras();
        group = extras.getParcelable("group");
        user = extras.getParcelable("user");
        boolean joined = extras.getBoolean("joined");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        upcoming = (LinearLayout) findViewById(R.id.upcoming);
        toolbar.setTitle(group.getName());

        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupLocationAdapter(this, new GroupLocationCallback() {
            @Override
            public void action(GroupLocation item) {

            }
        });
        recyclerView.setAdapter(adapter);
        GoodwayHttpsClient.getGroupLocations(this, new Action<GroupLocation>() {
            @Override
            public void action(GroupLocation e) {
                adapter.add(e);
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {

            }
        }, new FinishCallback() {
            @Override
            public void action(int length) {

            }
        }, mail, password, group.getName(), group.getId());

        GoodwayHttpsClient.getUpcomingEvents(this, new Action<GroupEvent>() {
            @Override
            public void action(final GroupEvent e) {
                String[] split = Address.splitIso8601(e.getS_time());
                View upcomingEvent = getLayoutInflater().inflate(R.layout.view_group_event, null);
                ((TextView)upcomingEvent.findViewById(R.id.name)).setText(e.getName());
                ((TextView)upcomingEvent.findViewById(R.id.description)).setText(getString(R.string.organised_by)+" "+group.getName());
                ((TextView)upcomingEvent.findViewById(R.id.date)).setText(split[2]+" "+GroupEvent.formatMonth(split[1]));
                ((TextView)upcomingEvent.findViewById(R.id.time)).setText(split[3]+"h"+split[4]);
                upcomingEvent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(GroupActivity.this, EventActivity.class);
                        i.putExtra("event", e);
                        i.putExtra("user", user);
                        i.putExtra("request", EventActivity.NEWACTIVITY);
                        startActivity(i);

                    }
                });
                Picasso.with(GroupActivity.this)
                        .load(e.getAvatar())
                        .error(R.mipmap.ic_person_white_48dp)
                        .resize(200, 200)
                        .centerCrop()
                        .transform(new ImageTrans_CircleTransform())
                        .into((ImageView) upcomingEvent.findViewById(R.id.avatar));
                upcoming.addView(upcomingEvent);
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {

            }
        }, mail, password, group);

        if(!joined) {
            findViewById(R.id.fab).setVisibility(View.INVISIBLE);
        }

        avatar = (ImageView) findViewById(R.id.avatar);
        Picasso.with(this)
                .load(group.getAvatar())
                .error(R.mipmap.ic_person_white_48dp)
                .resize(200, 200)
                .centerCrop()
                .transform(new ImageTrans_CircleTransform())
                .into(avatar);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);


    }

    public void fabClick(View v){
        GoodwayHttpsClient.joinGroup(this, new Action<Void>() {
            @Override
            public void action(Void e) {
                findViewById(R.id.fab).setVisibility(View.INVISIBLE);
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {
                Toast.makeText(GroupActivity.this, R.string.failure, Toast.LENGTH_SHORT).show();
            }
        }, mail, password, group);
    }
}
