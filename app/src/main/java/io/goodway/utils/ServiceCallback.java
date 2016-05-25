package io.goodway.utils;

import io.goodway.model.gtfs.Route;
import io.goodway.model.gtfs.Service;

/**
 * Created by Antoine Sauray on 5/25/2016.
 */
public interface ServiceCallback {
    public void callback(Service t);
}
