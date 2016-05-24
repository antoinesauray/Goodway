package io.goodway.activities;

import android.content.Intent;
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

import java.util.List;

import io.goodway.R;
import io.goodway.model.Group;
import io.goodway.adapters.GroupAdapter;
import io.goodway.model.callback.GroupCallback;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class SearchGroupActivity extends AppCompatActivity{

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
    private String token;
    private EditText find_groups;
    private AsyncTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_group);
        token = getIntent().getExtras().getString("token");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.find_groups);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);


        find_groups = (EditText) findViewById(R.id.find_groups);
        find_groups.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (task != null) {
                    task.cancel(true);
                }
                adapter.clear();
                String text = find_groups.getText().toString();
                task = GoodwayHttpClientPost.findGroups(SearchGroupActivity.this, new Action<List<Group>>() {
                    @Override
                    public void action(List<Group> e) {
                        for(Group g : e) {
                            adapter.add(g);
                        }
                    }
                }, null, token, text);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.list);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        layoutManager = new LinearLayoutManager(this);

        adapter = new GroupAdapter(this, new GroupCallback() {
            @Override
            public void action(Group g) {
                Intent i = new Intent(SearchGroupActivity.this, GroupActivity.class);
                i.putExtra("group", g);
                i.putExtra("token", token);
                i.putExtra("joined", false);
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
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
