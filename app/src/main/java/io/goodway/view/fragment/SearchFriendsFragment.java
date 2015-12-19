package io.goodway.view.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;

import io.goodway.MainActivity;
import io.goodway.R;
import io.goodway.model.User;
import io.goodway.model.adapter.UserAdapter;
import io.goodway.model.callback.UserCallback;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;


/**
 * Created by sauray on 14/03/15.
 */
public class SearchFriendsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private View root;

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private SwipeRefreshLayout swipeLayout;

    private MainActivity mainActivity;
    private TextView error;

    private int request;
    private String mail, password;
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_search_friends, container, false);

        request = getArguments().getInt("REQUEST");
        mainActivity = (MainActivity) getActivity();
        recyclerView = (RecyclerView) root.findViewById(R.id.list);

        SharedPreferences shared_preferences = getActivity().getSharedPreferences("shared_preferences_test",
                getActivity().MODE_PRIVATE);
        mail = shared_preferences.getString("mail", null);
        password = shared_preferences.getString("password", null);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        error = (TextView) root.findViewById(R.id.error);
        adapter = new UserAdapter(new UserCallback() {
            @Override
            public void action(final User u) {
                BottomSheet.Builder sheet = new BottomSheet.Builder(getActivity()).title(getString(R.string.places)+ " "+getString(R.string.linked_to)+" "+u.getFirstName()).listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                        }
                    }
                });
                int i=0;
                // add the locations from https request
                sheet.show();
            }
        }, mail, password);

        GoodwayHttpsClient.getFriends(getActivity(), new Action<User>() {
            @Override
            public void action(User e) {
                swipeLayout.setRefreshing(false);
                adapter.add(e);
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {
                switch (length){
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
        }, mail, password);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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
        GoodwayHttpsClient.getFriends(getActivity(), new Action<User>() {
            @Override
            public void action(User e) {
                swipeLayout.setRefreshing(false);
                adapter.add(e);
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {
                switch (length){
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
        },mail, password);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            ((SearchFragment) getParentFragment()).closeKeyboard();
        }
    }

    private void finish(Address address){
        switch (request){
            case MainActivity.DEPARTURE:
                mainActivity.setFrom(address);
                Bundle b1 = new Bundle();
                b1.putParcelable("DEPARTURE", address);
                mainActivity.switchToMain(b1, request);
                break;
            case MainActivity.DESTINATION:
                mainActivity.setTo(address);
                Bundle b2 = new Bundle();
                b2.putParcelable("DESTINATION", address);
                mainActivity.switchToMain(b2, request);
                break;

        }
    }
}
