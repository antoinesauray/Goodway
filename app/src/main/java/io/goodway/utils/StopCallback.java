package io.goodway.utils;

import io.goodway.model.gtfs.Service;
import io.goodway.model.gtfs.Stop;

/**
 * Created by Antoine Sauray on 5/25/2016.
 */
public interface StopCallback {
    public void callback(Stop t);
}
