package io.goodway.view.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.goodway.R;


/**
 * Created by sauray on 14/03/15.
 */
public class MainFragment extends Fragment {

    private View root;

    public static final String TAG="MAIN_FRAGMENT";

    public static MainFragment newInstance(Bundle args) {
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_main_nested, container, false);
        return root;
    }
}
