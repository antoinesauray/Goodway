package io.goodway;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.goodway.model.User;
import io.goodway.model.adapter.UserAdapter;
import io.goodway.model.callback.FinishCallback;
import io.goodway.model.callback.UserCallback;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.ErrorAction;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class FriendsActivity extends AppCompatActivity {

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
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private TabLayout tabLayout;

    private User user;
    private String token;
    private int nbFriendRequests;

    public static final int FRIENDACCEPTANCE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Bundle extras = this.getIntent().getExtras();
        user = extras.getParcelable("user");
        token = extras.getString("token");
        nbFriendRequests = extras.getInt("nbFriendRequests");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.contacts);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(pagerAdapter);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("nbFriendRequests", nbFriendRequests);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    public void fabClick(View v){
        Intent i = new Intent(FriendsActivity.this, AddFriendActivity.class);
        i.putExtra("token", token);
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult=", "onActivityResult");
        if (resultCode == RESULT_OK) {
            nbFriendRequests--;
            Log.d("nbFriendRequests=", "nbFriendRequests="+nbFriendRequests);
            pagerAdapter.notifyDataSetChanged();
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    // Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
    public class PagerAdapter extends FragmentStatePagerAdapter {

        FriendsFragment f1;
        FriendRequestFragment f2;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
            f1 = new FriendsFragment();
            f2 = new FriendRequestFragment();

            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putString("token", token);
            f1.setArguments(args);
            f2.setArguments(args);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i){
                case 0:
                    return f1;
                case 1:
                    return f2;
                default:
                    return null;
            }
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {}

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return getString(R.string.contacts);
                case 1:
                    if(nbFriendRequests!=0) {
                        return getString(R.string.pending) + " (" +nbFriendRequests+")";
                    }
                    else{
                        return getString(R.string.pending);
                    }
                default:
                    return null;
            }
        }
    }

    // Instances of this class are fragments representing a single
// object in our collection.
    public static class FriendsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
        public static final String ARG_OBJECT = "object";
        RecyclerView recyclerView;
        SwipeRefreshLayout swipeLayout;
        LinearLayoutManager layoutManager;
        TextView error;
        UserAdapter adapter;
        String token;
        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    R.layout.fragment_friends, container, false);

            Bundle extras = getArguments();
            token = extras.getString("token");

            recyclerView = (RecyclerView) rootView.findViewById(R.id.list);

            recyclerView.setHasFixedSize(true);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            error = (TextView) rootView.findViewById(R.id.error);
            layoutManager = new LinearLayoutManager(getActivity());
            adapter = new UserAdapter(getActivity(), new UserCallback() {
                @Override
                public void action(User u) {
                    Intent i = new Intent(getActivity(), ProfileActivity.class);
                    i.putExtra("user", u);
                    i.putExtra("token", token);
                    i.putExtra("self", false);
                    startActivity(i);
                }
            });

            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
            swipeLayout.setOnRefreshListener(this);
            swipeLayout.setColorSchemeColors(R.color.accent);

            return rootView;
        }

        @Override
        public void onResume(){
            super.onResume();
            onRefresh();
        }

        @Override
        public void onRefresh() {
            swipeLayout.setRefreshing(true);
            adapter.clear();
            error.setVisibility(View.INVISIBLE);
            GoodwayHttpClientPost.getFriends(getActivity(), new Action<List<User>>() {
                @Override
                public void action(List<User> e) {
                    if(e!=null) {
                        for (User u : e) {
                            swipeLayout.setRefreshing(false);
                            adapter.add(u);
                        }
                    }
                }
            }, new ErrorAction() {
                @Override
                public void action(int length) {
                    switch (length) {
                        case 0:
                            //error.setText(R.string.no_friends);
                            break;
                        case -1:

                            error.setText(R.string.connexion_error);
                            break;
                    }
                    swipeLayout.setRefreshing(false);
                    error.setVisibility(View.VISIBLE);
                }
            }, token);
        }
    }
    public static class FriendRequestFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
        public static final String ARG_OBJECT = "object";
        RecyclerView recyclerView;
        SwipeRefreshLayout swipeLayout;
        LinearLayoutManager layoutManager;
        TextView error;
        UserAdapter adapter;
        String token;
        String nbRequests="";
        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    R.layout.fragment_friends, container, false);
            recyclerView = (RecyclerView) rootView.findViewById(R.id.list);

            recyclerView.setHasFixedSize(true);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            layoutManager = new LinearLayoutManager(getActivity());

            error = (TextView) rootView.findViewById(R.id.error);

            Bundle extras = getArguments();
            token = extras.getString("token");

            adapter = new UserAdapter(getActivity(), new UserCallback() {
                @Override
                public void action(User u) {
                    Intent i = new Intent(getActivity(), ProfileActivity.class);
                    i.putExtra("user", u);
                    i.putExtra("token", token);
                    i.putExtra("self", false);
                    startActivityForResult(i, FRIENDACCEPTANCE);
                }
            });


            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
            swipeLayout.setOnRefreshListener(this);
            swipeLayout.setColorSchemeColors(R.color.accent);
            return rootView;
        }

        @Override
        public void onResume(){
            super.onResume();
            onRefresh();
        }

        @Override
        public void onRefresh() {
            swipeLayout.setRefreshing(true);
            adapter.clear();
            error.setVisibility(View.INVISIBLE);
            GoodwayHttpClientPost.getFriendRequests(getActivity(), new Action<List<User>>() {
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
                            error.setText(R.string.no_friend_requests);
                            break;
                        case -1:
                            error.setText(R.string.connexion_error);
                            break;
                    }
                    swipeLayout.setRefreshing(false);
                    error.setVisibility(View.VISIBLE);
                }
            }, new FinishCallback() {
                @Override
                public void action(int length) {

                }
            }, token);
        }
    }
}
