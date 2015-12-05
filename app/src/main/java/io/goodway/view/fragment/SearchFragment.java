package io.goodway.view.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.List;

import io.goodway.MainActivity;
import io.goodway.R;


/**
 * Created by sauray on 14/03/15.
 */
public class SearchFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private View root;

    private GoogleApiClient googleApiClient;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private int request;
    private MainActivity mainActivity;

    private SearchPlacesFragment f1;
    private SearchFriendsFragment f2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_search, container, false);

        request = getArguments().getInt("REQUEST");

        mainActivity = (MainActivity) getActivity();
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        viewPager = (ViewPager) root.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) mainActivity.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        int[] icons = new int[]{R.drawable.ic_action_search, R.drawable.ic_person_white};
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(icons[i]);
        }
        return root;
    }

    @Override
    public void onResume(){
        super.onResume();
        viewPager.setCurrentItem(0);
    }

    public void closeKeyboard(){
        f1.closeKeyboard();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        f1 = new SearchPlacesFragment();
        f1.setArguments(getArguments());
        f2 = new SearchFriendsFragment();
        f2.setArguments(getArguments());
        adapter.addFragment(f1);
        adapter.addFragment(f2);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }
}
