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

import io.goodway.model.User;
import io.goodway.model.adapter.UserAdapter;
import io.goodway.model.callback.UserCallback;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class AddFriendActivity extends AppCompatActivity{

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
    private UserAdapter adapter;
    private String mail, password;
    private EditText findFriends;
    private AsyncTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        Bundle extras = this.getIntent().getExtras();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.find_friend);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        findFriends = (EditText) findViewById(R.id.find_friends);
        findFriends.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (task != null) {
                    task.cancel(true);
                }
                adapter.clear();
                String text = findFriends.getText().toString();
                if (text != null) {
                    String[] split = text.split(" ");
                    if (split != null) {
                        String s1 = null, s2 = null;
                        if (split.length > 0) {
                            s1 = split[0];
                            if (split.length > 1) {
                                s2 = split[1];
                            }
                        }
                        task = GoodwayHttpClientPost.getUsersFromName(AddFriendActivity.this, new Action<User>() {
                            @Override
                            public void action(User e) {
                                adapter.add(e);
                            }
                        }, null, mail, password, s1, s2);
                    }

                }

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

        adapter = new UserAdapter(this, new UserCallback() {
            @Override
            public void action(User u) {
                Intent i = new Intent(AddFriendActivity.this, ProfileActivity.class);
                i.putExtra("user", u);
                i.putExtra("token", mail);
                i.putExtra("password", password);
                i.putExtra("self", false);
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
