package io.goodway.view.fragment.nested;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.goodway.R;


/**
 * Created by sauray on 14/03/15.
 */
public class FlowFragment extends Fragment {

    private View root;

    public static final String TAG="MAIN_FRAGMENT";

    public static FlowFragment newInstance(Bundle args) {
        FlowFragment fragment = new FlowFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_main_flow, container, false);
        return root;
    }
}
