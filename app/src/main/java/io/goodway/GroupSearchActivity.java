package io.goodway;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;

import io.goodway.model.Group;
import io.goodway.model.adapter.GroupAdapter;
import io.goodway.model.callback.GroupCallback;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class GroupSearchActivity extends AppCompatActivity{

    // ----------------------------------- Model
    /**
     * Unique identifier for this activity
     */
    private static final String TAG = "STOP_ACTIVITY";

    // ----------------------------------- UI

    /**
     * Toolbar widget
     */
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private GroupAdapter adapter;
    private String mail, password;
    private EditText findGroups;
    private AsyncTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_search);
        Bundle extras = this.getIntent().getExtras();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.find_groups);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        findGroups = (EditText) findViewById(R.id.find_groups);
        findGroups.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (task != null) {
                    task.cancel(true);
                }
                adapter.clear();
                String text = findGroups.getText().toString();
                task = GoodwayHttpsClient.getGroups(GroupSearchActivity.this, new Action<Group>() {
                            @Override
                            public void action(Group e) {
                                adapter.add(e);
                            }
                        }, null, mail, password, text);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.list);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        layoutManager = new LinearLayoutManager(this);

        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);

        adapter = new GroupAdapter(this, new GroupCallback() {
            @Override
            public void action(Group g) {
                Intent i = new Intent(GroupSearchActivity.this, UserGroupsActivity.class);
                i.putExtra("group", g);
                i.putExtra("mail", mail);
                i.putExtra("password", password);
                startActivity(i);
            }
        });


        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
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
}
