package io.goodway.activities.fragment.gtfs;

/**
 * Created by Antoine Sauray on 5/25/2016.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.goodway.R;
import io.goodway.activities.gtfs.SubscribeActivity;
import io.goodway.adapters.ServiceAdapter;
import io.goodway.adapters.StopAdapter;
import io.goodway.model.gtfs.Route;
import io.goodway.model.gtfs.Schema;
import io.goodway.model.gtfs.Service;
import io.goodway.model.gtfs.Stop;
import io.goodway.sync.gcm.gtfs.HttpRequest;


/**
 * Created by sauray on 14/03/15.
 */
public class StopFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private View root;

    private RecyclerView recyclerView;
    private StopAdapter adapter;
    private SwipeRefreshLayout swipeLayout;

    private Schema schema;
    private Service service;

    public static StopFragment newInstance(Bundle args) {
        StopFragment fragment = new StopFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_search_friends, container, false);
        recyclerView = (RecyclerView) root.findViewById(R.id.list);

        Bundle b = getArguments();
        schema = b.getParcelable("schema");
        service = b.getParcelable("service");

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new StopAdapter((SubscribeActivity)getActivity());

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        swipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(R.color.accent);
        swipeLayout.setRefreshing(true);

        HttpRequest.stops(new HttpRequest.Action<List<Stop>>() {
            @Override
            public void success(List<Stop> schemas) {
                Log.d("stops", schemas.toString());
                adapter.addAll(schemas);
            }

            @Override
            public void error() {

            }
        }, schema, service);

        return root;
    }

    @Override
    public void onRefresh() {
        swipeLayout.setRefreshing(true);
        adapter.clear();

        HttpRequest.stops(new HttpRequest.Action<List<Stop>>() {
            @Override
            public void success(List<Stop> schemas) {
                Log.d("stops", schemas.toString());
                adapter.addAll(schemas);
            }

            @Override
            public void error() {

            }
        }, schema, service);
    }
}
