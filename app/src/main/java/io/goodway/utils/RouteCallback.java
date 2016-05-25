package io.goodway.utils;

import io.goodway.model.gtfs.Route;
import io.goodway.model.gtfs.Schema;

/**
 * Created by Antoine Sauray on 5/25/2016.
 */
public interface RouteCallback {
    public void callback(Route t);
}
