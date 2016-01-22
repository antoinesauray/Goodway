package io.goodway.view.fragment.nested;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import io.goodway.AddressActivity;
import io.goodway.ProfileActivity;
import io.goodway.R;
import io.goodway.model.User;
import io.goodway.model.callback.FinishCallback;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.UserLocation;

/**
 * Created by antoine on 18/01/16.
 */
public class AddressFragment extends Fragment {
    public static final String TAG = "address";
    String token;
    LinearLayout locations;
    boolean self;
    User user;
    UserLocation item;

    public static AddressFragment newInstance(Bundle args) {
        AddressFragment fragment = new AddressFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.fragment_addresse, container, false);
        Bundle extras = getArguments();
        token = extras.getString("token");
        self = extras.getBoolean("self");
        user = extras.getParcelable("user");

        locations = (LinearLayout) rootView.findViewById(R.id.locations);
        if(self){
            final boolean preview = extras.getBoolean("preview", false);
            GoodwayHttpClientPost.getMyLocations(getActivity(), new Action<List<UserLocation>>() {
                @Override
                public void action(List<UserLocation> locations) {
                    for (UserLocation location : locations) {
                        addUserLocation(location, preview);
                    }
                }
            }, new ErrorAction() {
                @Override
                public void action(int length) {
                    if (length == 0) {
                        if(!preview) {
                            View addAddress = getActivity().getLayoutInflater().inflate(R.layout.view_add_address, null);
                            locations.addView(addAddress);
                        }
                    } else {
                        View notFound = getActivity().getLayoutInflater().inflate(R.layout.view_way_not_found, null);
                        ((TextView) notFound.findViewById(R.id.message)).setText(R.string.unavailable);
                        locations.addView(notFound);
                    }
                }
            }, new FinishCallback() {
                @Override
                public void action(int length) {
                    if(!preview) {
                        View addAddress = getActivity().getLayoutInflater().inflate(R.layout.view_add_address, null);
                        locations.addView(addAddress);
                    }
                }
            }, token);
        }
        else if(user.isFriend()){
            GoodwayHttpClientPost.getUserLocations(getActivity(), new Action<List<UserLocation>>() {
                @Override
                public void action(List<UserLocation> locations) {
                    for (UserLocation location : locations) {
                        Log.d("adding address", "adding address:" + location.toString());
                        addUserLocation(location, true);
                    }
                }
            }, new ErrorAction() {
                @Override
                public void action(int length) {
                    if (length < 0) {
                        View notFound = getActivity().getLayoutInflater().inflate(R.layout.view_way_not_found, null);
                        ((TextView) notFound.findViewById(R.id.message)).setText(R.string.unavailable);
                        locations.addView(notFound);
                    }
                }
            }, null, token, user.getId());
        }
        else{
            View notFound = getActivity().getLayoutInflater().inflate(R.layout.view_not_friend, null);
            locations.addView(notFound);
        }
        return rootView;
    }

    private View addUserLocation(final UserLocation userLocation, boolean preview){
        View location = getActivity().getLayoutInflater().inflate(R.layout.view_location, null);
        if(!preview) {
            location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle b = new Bundle();
                    b.putParcelable("location", userLocation);
                    b.putString("token", token);
                    item = userLocation;

                    ((AddressActivity) getActivity()).switchToFragment(NewAddressFragment.newInstance(b));

                    //FragmentManager fragmentManager = getActivity().getFragmentManager();
                    //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    //fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                    //fragmentTransaction.addToBackStack(NewAddressFragment.TAG);
                    //fragmentTransaction.replace(R.id.fragment, NewAddressFragment.newInstance(b));
                    //fragmentTransaction.commitAllowingStateLoss();
                }
            });
        }
        ((TextView) location.findViewById(R.id.s_name)).setText(userLocation.getName());
        ((TextView) location.findViewById(R.id.name)).setText(userLocation.getA_name());
        locations.addView(location);
        return location;
    }

    public UserLocation getItem(){
        return item;
    }

    public void setItem(UserLocation location){
        this.item = location;
    }
}