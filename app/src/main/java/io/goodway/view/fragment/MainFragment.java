package io.goodway.view.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
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
        titles = new String[]{"Accueil",
                //"Notifications",
                "Profil"};
        setupViewPager(viewPager);

        tabLayout = (TabLayout) root.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_navigation_white_48dp);
        //tabLayout.getTabAt(1).setIcon(R.drawable.ic_public_white_48dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_menu_white_48dp);


        return root;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        home = new MainFragmentHome();
        home.setArguments(getArguments());

        //FlowFragment t1 = new FlowFragment();
        //t1.setArguments(getArguments());

        ProfileFragment profile = new ProfileFragment();
        profile.setArguments(getArguments());

        adapter.addFragment(home);
        //adapter.addFragment(t1);
        adapter.addFragment(profile);

        viewPager.setAdapter(adapter);
    }
/*
    private void revealFab(){

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
        int cx = floatingActionButton.getWidth() / 2;
        int cy = floatingActionButton.getHeight() / 2;

        float finalRadius = (float) Math.hypot(cx, cy);

        Animator anim =
                null;

            anim = ViewAnimationUtils.createCircularReveal(floatingActionButton, cx, cy, 0, finalRadius);
        floatingActionButton.setVisibility(View.VISIBLE);
        anim.start();
        }
        else{
            floatingActionButton.setVisibility(View.VISIBLE);
        }
    }

    private void unrevealFab(){

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            int cx = floatingActionButton.getWidth() / 2;
            int cy = floatingActionButton.getHeight() / 2;

            float initialRadius = (float) Math.hypot(cx, cy);

            Animator anim =
                    ViewAnimationUtils.createCircularReveal(floatingActionButton, cx, cy, initialRadius, 0);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    floatingActionButton.setVisibility(View.INVISIBLE);
                }
            });

<<<<<<< HEAD
    @Override
    public void onConnected(Bundle bundle) {
        userLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        Geocoder gcd = new Geocoder(getActivity(), Locale.getDefault());
        List<android.location.Address> addresses = null;
        if(userLocation!=null) {
            try {
                addresses = gcd.getFromLocation(userLocation.getLatitude(), userLocation.getLongitude(), 1);
                if (addresses.size() > 0) {
                    GoodwayHttpsClient.updateUserCity(getActivity(), new Action<Boolean>() {
                        @Override
                        public void action(Boolean e) {

                        }
                    }, null, mail, password, addresses.get(0).getLocality());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
=======
            anim.start();
>>>>>>> feature-new_ui_old_state
        }
        else{
            floatingActionButton.setVisibility(View.INVISIBLE);
        }
    }
*/
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
            return "";
        }
    }

}
