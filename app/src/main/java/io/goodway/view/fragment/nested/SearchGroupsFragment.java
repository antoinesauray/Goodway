package io.goodway.view.fragment.nested;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;

import java.util.ArrayList;
import java.util.List;

import io.goodway.MainActivity;
import io.goodway.R;
import io.goodway.SearchActivity;
import io.goodway.model.Group;
import io.goodway.model.adapter.GroupAdapter;
import io.goodway.model.callback.FinishCallback;
import io.goodway.model.callback.GroupCallback;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.GroupLocation;
import io.goodway.view.fragment.TripFragment;


/**
 * Created by sauray on 14/03/15.
 */
public class SearchGroupsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private View root;

    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private SwipeRefreshLayout swipeLayout;

    private TripFragment tripFragment;
    private TextView error;

    private int request;
    private String token;
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    private ArrayList<GroupLocation> groupLocations;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_search_groups, container, false);

        request = getArguments().getInt("REQUEST");
        token = getArguments().getString("token");
        tripFragment = (TripFragment) getParentFragment();
        recyclerView = (RecyclerView) root.findViewById(R.id.list);
        error = (TextView) root.findViewById(R.id.error);


        adapter = new GroupAdapter(getActivity(), new GroupCallback() {
            @Override
            public void action(Group e) {
                final BottomSheet.Builder sheet = new BottomSheet.Builder(getActivity()).title(getString(R.string.addresses)+ " "+getString(R.string.linked_to)+" "+e.getName()).listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(groupLocations.get(which-1));
                    }
                });
                final ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage(getString(R.string.request_places));
                dialog.setProgressStyle(dialog.STYLE_SPINNER);
                dialog.show();
                groupLocations = new ArrayList<>();
                GoodwayHttpClientPost.getGroupLocations(getActivity(), new Action<List<GroupLocation>>() {
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
                            Toast.makeText(getActivity(), R.string.connexion_error, Toast.LENGTH_SHORT).show();
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

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        GoodwayHttpClientPost.getMyGroups(getActivity(), new Action<List<Group>>() {
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
        GoodwayHttpClientPost.getMyGroups(getActivity(), new Action<List<Group>>() {
            @Override
            public void action(List<Group> e) {
                if(e.size()!=0) {
                    for (Group g : e) {
                        adapter.add(g);
                        swipeLayout.setRefreshing(false);
                    }
                }
                else{
                    error.setText(R.string.no_groups);
                    error.setVisibility(View.VISIBLE);
                    swipeLayout.setRefreshing(false);}
            }
        }, null, token);
    }

    private void finish(Address address){
        switch (request){
            case MainActivity.DEPARTURE:
                ((SearchActivity)getActivity()).setFrom(address);
                Bundle b1 = new Bundle();
                b1.putParcelable("departure", address);
                b1.putString("token", token);
                //tripFragment.switchToMain(b1, request);
                ((SearchActivity)getActivity()).switchToFragmentWithExitAnim(TripFragment.newInstance(b1));
                break;
            case MainActivity.DESTINATION:
                ((SearchActivity)getActivity()).setTo(address);
                Bundle b2 = new Bundle();
                b2.putParcelable("destination", address);
                b2.putString("token", token);
                ((SearchActivity)getActivity()).switchToFragmentWithExitAnim(TripFragment.newInstance(b2));
                break;
            default:

        }
    }
}