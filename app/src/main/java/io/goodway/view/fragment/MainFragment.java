package io.goodway.view.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.goodway.R;
import io.goodway.model.User;
import io.goodway.view.fragment.nested.FlowFragment;
import io.goodway.view.fragment.nested.ProfileFragment;


/**
 * Created by sauray on 14/03/15.
 */
public class MainFragment extends Fragment{

    private View root;

    private String token;
    private User user;

    private Toolbar toolbar;

    private Fragment home;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private String[] titles;

    public static MainFragment newInstance(Bundle args) {
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_main, container, false);


        Bundle extras = getArguments();
        token = extras.getString("token");
        user = extras.getParcelable("user");

        viewPager = (ViewPager) root.findViewById(R.id.viewpager);
        titles = new String[]{"Accueil", "Notifications", "Profil"};
        setupViewPager(viewPager);

        tabLayout = (TabLayout) root.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        return root;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());
        home = new MainFragmentHome();
        home.setArguments(getArguments());

        FlowFragment t1 = new FlowFragment();
        t1.setArguments(getArguments());

        ProfileFragment profile = new ProfileFragment();
        profile.setArguments(getArguments());

        adapter.addFragment(home);
        adapter.addFragment(t1);
        adapter.addFragment(profile);
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
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
            return titles[position];
        }
    }

}
