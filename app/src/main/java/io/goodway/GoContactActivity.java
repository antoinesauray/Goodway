package io.goodway;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;

import java.util.ArrayList;
import java.util.List;

import io.goodway.model.User;
import io.goodway.model.adapter.UserAdapter;
import io.goodway.model.callback.FinishCallback;
import io.goodway.model.callback.UserCallback;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.UserLocation;
import io.goodway.view.fragment.MainFragmentHome;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class GoContactActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    // ----------------------------------- Model

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private SwipeRefreshLayout swipeLayout;

    private Toolbar toolbar;

    private MainFragmentHome mainFragmentctivity;
    private TextView error;

    private int request;
    private String token;
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    private ArrayList<UserLocation> userLocations;

    private Address departure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_contact);
        //request = getIntent().getExtras().getInt("REQUEST");
        token = getIntent().getExtras().getString("token");
        departure = getIntent().getExtras().getParcelable("departure");

        recyclerView = (RecyclerView) findViewById(R.id.list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(R.color.accent);
        swipeLayout.setRefreshing(true);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);


        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        error = (TextView) findViewById(R.id.error);
        adapter = new UserAdapter(this, new UserCallback() {
            @Override
            public void action(final User u) {
                final BottomSheet.Builder sheet = new BottomSheet.Builder(GoContactActivity.this).title(getString(R.string.addresses)+ " "+getString(R.string.linked_to)+" "+u.getFirstName()).listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(userLocations.get(which-1));
                    }
                });
                final ProgressDialog dialog = new ProgressDialog(GoContactActivity.this);
                dialog.setMessage(getString(R.string.request_places));
                dialog.setProgressStyle(dialog.STYLE_SPINNER);
                dialog.show();
                userLocations = new ArrayList<>();
                GoodwayHttpClientPost.getUserLocations(GoContactActivity.this, new Action<List<UserLocation>>() {
                    @Override
                    public void action(List<UserLocation> locations) {
                        for (UserLocation location : locations) {
                            userLocations.add(location);
                            sheet.sheet(userLocations.size(), location.getName());
                        }
                    }
                }, new ErrorAction() {
                    @Override
                    public void action(int length) {
                        dialog.dismiss();
                        if (length == -1) {
                            Toast.makeText(GoContactActivity.this, R.string.connexion_error, Toast.LENGTH_SHORT).show();
                        } else {
                            sheet.show();
                        }
                    }
                }, new FinishCallback() {
                    @Override
                    public void action(int length) {
                        dialog.dismiss();
                        sheet.show();
                    }
                }, token, u.getId());

                // add the locations from https request
            }
        });

        GoodwayHttpClientPost.getFriends(this, new Action<List<User>>() {
            @Override
            public void action(List<User> e) {
                swipeLayout.setRefreshing(false);
                for (User u : e) {
                    adapter.add(u);
                }
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {
                switch (length) {
                    case 0:
                        swipeLayout.setRefreshing(false);
                        break;
                    case -1:
                        swipeLayout.setRefreshing(false);
                        error.setText(R.string.connexion_error);
                        error.setVisibility(View.VISIBLE);
                        break;
                }

            }
        }, token);
    }

    @Override
    public void onRefresh() {
        swipeLayout.setRefreshing(true);
        adapter.clear();
        error.setVisibility(View.INVISIBLE);
        GoodwayHttpClientPost.getFriends(this, new Action<List<User>>() {
            @Override
            public void action(List<User> e) {
                for(User u : e) {
                    swipeLayout.setRefreshing(false);
                    adapter.add(u);
                }

            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {
                switch (length) {
                    case 0:
                        swipeLayout.setRefreshing(false);
                        error.setText(R.string.no_friends);
                        error.setVisibility(View.VISIBLE);
                        break;
                    case -1:
                        swipeLayout.setRefreshing(false);
                        error.setText(R.string.connexion_error);
                        error.setVisibility(View.VISIBLE);
                        break;
                }

            }
        }, token);
    }

    private void finish(Address address){
        Intent i = new Intent(this, WayActivity.class);
        i.putExtras(getIntent().getExtras());
        i.putExtra("arrival", address);
        startActivity(i);
    }

}
