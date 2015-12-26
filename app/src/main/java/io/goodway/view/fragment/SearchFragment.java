package io.goodway.view.fragment;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

import io.goodway.MainActivity;
import io.goodway.R;
import io.goodway.model.User;


/**
 * Created by sauray on 14/03/15.
 */
public class SearchFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private View root;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private int request;
    private MainActivity mainActivity;

    private SearchPlacesFragment f2;
    private SearchFriendsFragment f1;
    private SearchGroupsFragment f3;

    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_search, container, false);
        int item = getArguments().getInt("ITEM", 1);
        request = getArguments().getInt("REQUEST");
        user = getArguments().getParcelable("user");
        mainActivity = (MainActivity) getActivity();

        viewPager = (ViewPager) root.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) mainActivity.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        int[] icons = new int[]{R.mipmap.ic_person_white, R.mipmap.ic_action_search, R.mipmap.ic_group_search};
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(icons[i]);
        }

        viewPager.setCurrentItem(item);
        return root;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        f1 = new SearchFriendsFragment();
        f1.setArguments(getArguments());
        f2 = new SearchPlacesFragment();
        f2.setArguments(getArguments());
        f3 = new SearchGroupsFragment();
        f3.setArguments(getArguments());
        adapter.addFragment(f1);
        adapter.addFragment(f2);
        adapter.addFragment(f3);
        viewPager.setAdapter(adapter);
    }

    public void setCurrentItem(int item){
        viewPager.setCurrentItem(item);
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
