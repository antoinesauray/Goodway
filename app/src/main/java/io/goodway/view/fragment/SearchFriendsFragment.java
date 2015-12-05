package io.goodway.view.fragment;

import android.content.DialogInterface;
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

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        error = (TextView) root.findViewById(R.id.error);
        adapter = new UserAdapter(new UserCallback() {
            @Override
            public void action(User u) {
                new BottomSheet.Builder(getActivity()).title("Place").sheet(1, "Home").sheet(2, "Work").listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 1:
                                Toast.makeText(getActivity(), "home", Toast.LENGTH_SHORT).show();
                                break;

                            case 2:
                                Toast.makeText(getActivity(), "work", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }).show();
            }
        }, mail, password);

        Bundle extras = getArguments();
        mail = extras.getString("mail");
        password = extras.getString("password");

        mail ="sauray.a@outlook.com";
        password = "antoine";
        GoodwayHttpsClient.getFriends(getActivity(), new Action<User>() {
            @Override
            public void action(User e) {
                swipeLayout.setRefreshing(false);
                adapter.add(e);
            }
        }, new ErrorAction() {
            @Override
            public void action() {
                swipeLayout.setRefreshing(false);
                error.setText(R.string.no_friends);
                error.setVisibility(View.VISIBLE);
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
            public void action() {
                swipeLayout.setRefreshing(false);
                error.setText(R.string.no_friends);
                error.setVisibility(View.VISIBLE);
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
}
