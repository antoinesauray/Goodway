package io.goodway.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;

import java.util.ArrayList;
import java.util.List;

import io.goodway.R;
import io.goodway.model.Group;
import io.goodway.adapters.GroupAdapter;
import io.goodway.model.callback.FinishCallback;
import io.goodway.model.callback.GroupCallback;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.GroupLocation;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class GoPlaceActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    // ----------------------------------- Model

    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private SwipeRefreshLayout swipeLayout;

    private Toolbar toolbar;

    private TextView error;

    private int request;
    private String token;
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    private ArrayList<GroupLocation> groupLocations;
    private Address departure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_place);
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

        recyclerView = (RecyclerView) findViewById(R.id.list);
        error = (TextView) findViewById(R.id.error);


        adapter = new GroupAdapter(this, new GroupCallback() {
            @Override
            public void action(Group e) {
                final BottomSheet.Builder sheet = new BottomSheet.Builder(GoPlaceActivity.this).title(getString(R.string.addresses)+ " "+getString(R.string.linked_to)+" "+e.getName()).listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(groupLocations.get(which-1));
                    }
                });
                final ProgressDialog dialog = new ProgressDialog(GoPlaceActivity.this);
                dialog.setMessage(getString(R.string.request_places));
                dialog.setProgressStyle(dialog.STYLE_SPINNER);
                dialog.show();
                groupLocations = new ArrayList<>();
                GoodwayHttpClientPost.getGroupLocations(GoPlaceActivity.this, new Action<List<GroupLocation>>() {
                    @Override
                    public void action(List<GroupLocation> e) {
                        for(GroupLocation location : e) {
                            groupLocations.add(location);
                            sheet.sheet(groupLocations.size(), location.getName());
                        }
                    }
                }, new ErrorAction() {
                    @Override
                    public void action(int length) {
                        dialog.dismiss();
                        if (length == 0) {
                            sheet.show();
                        } else {
                            Toast.makeText(GoPlaceActivity.this, R.string.connexion_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new FinishCallback() {
                    @Override
                    public void action(int length) {
                        dialog.dismiss();
                        sheet.show();
                    }
                }, token, e);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        GoodwayHttpClientPost.getMyGroups(GoPlaceActivity.this, new Action<List<Group>>() {
            @Override
            public void action(List<Group> e) {
                if (e != null) {
                    Log.d("e.size", "e.size = " + e.size());
                    if (e.size() != 0) {
                        for (Group g : e) {
                            adapter.add(g);
                            swipeLayout.setRefreshing(false);
                        }
                    } else {
                        error.setText(R.string.no_groups);
                        error.setVisibility(View.VISIBLE);
                        swipeLayout.setRefreshing(false);
                    }
                }
            }
        }, null, token);

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(R.color.accent);
        swipeLayout.setRefreshing(true);
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

    @Override
    public void onRefresh() {
        swipeLayout.setRefreshing(true);
        adapter.clear();
        error.setVisibility(View.INVISIBLE);
        GoodwayHttpClientPost.getMyGroups(this, new Action<List<Group>>() {
            @Override
            public void action(List<Group> e) {
                if (e.size() != 0) {
                    for (Group g : e) {
                        adapter.add(g);
                        swipeLayout.setRefreshing(false);
                    }
                } else {
                    error.setText(R.string.no_groups);
                    error.setVisibility(View.VISIBLE);
                    swipeLayout.setRefreshing(false);
                }
            }
        }, null, token);
    }

    private void finish(Address address){
        Intent i = new Intent(this, WayActivity.class);
        i.putExtras(getIntent().getExtras());
        i.putExtra("arrival", address);
        startActivity(i);
    }
}
