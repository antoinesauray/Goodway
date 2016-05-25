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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.goodway.R;
import io.goodway.activities.gtfs.SubscribeActivity;
import io.goodway.adapters.RouteAdapter;
import io.goodway.adapters.SchemaAdapter;
import io.goodway.model.gtfs.Route;
import io.goodway.model.gtfs.Schema;
import io.goodway.sync.gcm.gtfs.HttpRequest;


/**
 * Created by sauray on 14/03/15.
 */
public class RouteFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private View root;

    private RecyclerView recyclerView;
    private RouteAdapter adapter;
    private SwipeRefreshLayout swipeLayout;

    private Schema s;

    public static RouteFragment newInstance(Bundle args) {
        RouteFragment fragment = new RouteFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_search_friends, container, false);
        recyclerView = (RecyclerView) root.findViewById(R.id.list);

        s = getArguments().getParcelable("schema");

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new RouteAdapter((SubscribeActivity)getActivity());

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        swipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(R.color.accent);
        swipeLayout.setRefreshing(true);

        HttpRequest.routes(new HttpRequest.Action<List<Route>>() {
            @Override
            public void success(List<Route> schemas) {
                for(Route s : schemas){
                    adapter.add(s);
                }
            }

            @Override
            public void error() {

            }
        }, s);

        return root;
    }

    @Override
    public void onRefresh() {
        swipeLayout.setRefreshing(true);
        adapter.clear();

        HttpRequest.routes(new HttpRequest.Action<List<Route>>() {
            @Override
            public void success(List<Route> schemas) {
                for(Route s : schemas){
                    adapter.add(s);
                }
            }

            @Override
            public void error() {

            }
        }, s);
    }
}
