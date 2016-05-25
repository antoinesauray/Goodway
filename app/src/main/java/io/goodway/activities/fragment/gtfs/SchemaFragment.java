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
import io.goodway.adapters.SchemaAdapter;
import io.goodway.model.gtfs.Schema;
import io.goodway.sync.gcm.gtfs.HttpRequest;


/**
 * Created by sauray on 14/03/15.
 */
public class SchemaFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private View root;

    private RecyclerView recyclerView;
    private SchemaAdapter adapter;
    private SwipeRefreshLayout swipeLayout;

    public static SchemaFragment newInstance(Bundle args) {
        SchemaFragment fragment = new SchemaFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_search_friends, container, false);
        recyclerView = (RecyclerView) root.findViewById(R.id.list);


        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new SchemaAdapter((SubscribeActivity)getActivity());

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        swipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(R.color.accent);
        swipeLayout.setRefreshing(true);

        HttpRequest.schemas(new HttpRequest.Action<List<Schema>>() {
            @Override
            public void success(List<Schema> schemas) {
                for(Schema s : schemas){
                    adapter.add(s);
                }
            }

            @Override
            public void error() {

            }
        });

        return root;
    }

    @Override
    public void onRefresh() {
        swipeLayout.setRefreshing(true);
        adapter.clear();

        HttpRequest.schemas(new HttpRequest.Action<List<Schema>>() {
            @Override
            public void success(List<Schema> schemas) {
                for(Schema s : schemas){
                    adapter.add(s);
                }
            }

            @Override
            public void error() {

            }
        });
    }
}
