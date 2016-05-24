package io.goodway.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import io.goodway.R;
import io.goodway.model.Group;
import io.goodway.model.GroupEvent;
import io.goodway.model.User;
import io.goodway.adapters.GroupLocationAdapter;
import io.goodway.model.callback.FinishCallback;
import io.goodway.model.callback.GroupLocationCallback;
import io.goodway.model.network.GoodwayHttpClientPost;
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

    private String token;

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
        token = extras.getString("token");
        boolean joined = extras.getBoolean("joined");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        upcoming = (LinearLayout) findViewById(R.id.upcoming);
        toolbar.setTitle(group.getName());


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupLocationAdapter(this, new GroupLocationCallback() {
            @Override
            public void action(GroupLocation item) {
                //Intent i = new Intent(GroupActivity.this, MainActivity.class);
                //i.putExtra("user", user);
                //i.putExtra("destination", item);
                //startActivity(i);
            }
        });
        recyclerView.setAdapter(adapter);
        GoodwayHttpClientPost.getGroupLocations(this, new Action<List<GroupLocation>>() {
            @Override
            public void action(List<GroupLocation> e) {
                if(e.size()!=0) {
                    for (GroupLocation location : e) {
                        adapter.add(location);
                    }
                }
                else{
                    // no addresses
                }
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {

            }
        }, new FinishCallback() {
            @Override
            public void action(int length) {

            }
        }, token, group);

        GoodwayHttpClientPost.getUpcomingEvents(this, new Action<List<GroupEvent>>() {
            @Override
            public void action(final List<GroupEvent> e) {
                for (final GroupEvent event : e) {
                    String[] split = Address.splitIso8601(event.getS_time());
                    View upcomingEvent = getLayoutInflater().inflate(R.layout.view_group_event, null);
                    ((TextView) upcomingEvent.findViewById(R.id.name)).setText(event.getName());
                    ((TextView) upcomingEvent.findViewById(R.id.description)).setText(getString(R.string.organised_by) + " " + group.getName());
                    ((TextView) upcomingEvent.findViewById(R.id.date)).setText(split[2] + " " + GroupEvent.formatMonth(split[1]));
                    ((TextView) upcomingEvent.findViewById(R.id.time)).setText(split[3] + "h" + split[4]);
                    upcomingEvent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(GroupActivity.this, EventActivity.class);
                            i.putExtra("event", event);
                            i.putExtra("user", user);
                            i.putExtra("request", EventActivity.NEWACTIVITY);
                            startActivity(i);
                        }
                    });
                    Picasso.with(GroupActivity.this)
                            .load(event.getAvatar())
                            .error(R.mipmap.ic_person_white_48dp)
                            .resize(200, 200)
                            .centerCrop()
                            .transform(new ImageTrans_CircleTransform())
                            .into((ImageView) upcomingEvent.findViewById(R.id.avatar));
                    upcoming.addView(upcomingEvent);
                }
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {
                if (length == 0) {
                    View error = getLayoutInflater().inflate(R.layout.view_way_not_found, null);
                    ((TextView) error.findViewById(R.id.message)).setText(R.string.no_events);
                    upcoming.addView(error);
                }
            }
        }, token, group);

        if(!joined) {
            findViewById(R.id.fab).setVisibility(View.VISIBLE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_quit:
                GoodwayHttpClientPost.quitGroup(this, new Action<Boolean>() {
                    @Override
                    public void action(Boolean e) {
                        if(e){finish();}
                        else{Toast.makeText(GroupActivity.this, R.string.failure, Toast.LENGTH_SHORT).show();}
                    }
                }, null, token, group);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void fabClick(View v){
        GoodwayHttpClientPost.joinGroup(this, new Action<Boolean>() {
            @Override
            public void action(Boolean e) {
                if(e){findViewById(R.id.fab).setVisibility(View.INVISIBLE);}
                else{Toast.makeText(GroupActivity.this, R.string.failure, Toast.LENGTH_SHORT).show();}
            }
        }, null, token, group);
    }
}
