package io.goodway.model.callback;

import android.view.View;

import io.goodway.navitia_android.Way;

/**
 * Created by antoine on 12/4/15.
 */
public interface WayCallback {

    public void action(View v, Way w);
}
