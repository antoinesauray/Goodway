package io.goodway.model.network;


import org.json.JSONObject;

/**
 * Created by antoine on 10/25/15.
 */
public interface ProcessJson<T> {

    public T processJson(JSONObject jsonObject);
}
