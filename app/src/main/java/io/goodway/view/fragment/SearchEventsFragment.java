package io.goodway.view.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.goodway.EventActivity;
import io.goodway.MainActivity;
import io.goodway.R;
import io.goodway.model.Event;
import io.goodway.model.adapter.EventAdapter;
import io.goodway.model.callback.EventCallback;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;


/**
 * Created by sauray on 14/03/15.
 */
public class SearchEventsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private View root;

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private SwipeRefreshLayout swipeLayout;

    private MainActivity mainActivity;
    private TextView error;

    private int request;
    private String mail, password;
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_search_events, container, false);

        request = getArguments().getInt("REQUEST");
        mainActivity = (MainActivity) getActivity();
        recyclerView = (RecyclerView) root.findViewById(R.id.list);
        error = (TextView) root.findViewById(R.id.error);

        SharedPreferences shared_preferences = getActivity().getSharedPreferences("shared_preferences_test",
                getActivity().MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);

        adapter = new EventAdapter(getActivity(), new EventCallback() {
            @Override
            public void action(Event e) {
                Intent i = new Intent(getActivity(), EventActivity.class);
                i.putExtra("EVENT", e);
                i.putExtra("REQUEST", request);
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
                    getActivity().startActivityForResult(i, MainActivity.EVENT_REQUEST);
                }
                else{
                    getActivity().startActivityForResult(i, MainActivity.EVENT_REQUEST);
                }
            }
        });

        GoodwayHttpsClient.getEvents(getActivity(), new Action<Event>() {
            @Override
            public void action(Event e) {
                adapter.add(e);
                swipeLayout.setRefreshing(false);
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {
                switch (length) {
                    case 0:
                        error.setText(R.string.no_events);
                        break;
                    case -1:
                        error.setText(R.string.connexion_error);
                        break;
                }
                error.setVisibility(View.VISIBLE);
                swipeLayout.setRefreshing(false);
            }
        }, mail, password);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        swipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(R.color.accent);
        swipeLayout.setRefreshing(true);

        return root;
    }

    @Override
    public void onRefresh() {
        swipeLayout.setRefreshing(true);
        adapter.clear();
        error.setVisibility(View.INVISIBLE);
        GoodwayHttpsClient.getEvents(getActivity(), new Action<Event>() {
            @Override
            public void action(Event e) {
                adapter.add(e);
                swipeLayout.setRefreshing(false);
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {
                switch (length){
                    case 0:
                        error.setText(R.string.no_events);
                        break;
                    case -1:
                        error.setText(R.string.connexion_error);
                        break;
                }
                swipeLayout.setRefreshing(false);
                error.setVisibility(View.VISIBLE);
            }
        }, mail, password);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            ((SearchFragment) getParentFragment()).closeKeyboard();
        }
    }
}
