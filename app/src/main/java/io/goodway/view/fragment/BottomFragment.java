package io.goodway.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.goodway.R;

/**
 * Created by sauray on 14/03/15.
 */
public class BottomFragment extends Fragment {

    private View root;
    private boolean openedState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_bottom, container, false);
        return root;
    }

    @Override
    public String toString(){
        return "Bottom Sheet";
    }

    public void setOpenedState(boolean state){
        this.openedState = state;
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        outState.putBoolean("OPENED", openedState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            openedState = savedInstanceState.getBoolean("OPENED");
        }
    }

}
