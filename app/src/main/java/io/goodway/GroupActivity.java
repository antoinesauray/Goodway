package io.goodway;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import io.goodway.model.Group;
import io.goodway.model.User;
import io.goodway.model.adapter.GroupAdapter;
import io.goodway.model.callback.GroupCallback;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.ErrorAction;
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

    private String mail, password;

    private FloatingActionButton fab;
    private ImageView avatar;
    private GroupAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Bundle extras = this.getIntent().getExtras();
        group = extras.getParcelable("group");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(group.getName());

        fab = (FloatingActionButton) findViewById(R.id.fab);

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

        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);
    }
}
