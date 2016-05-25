package io.goodway.sync.gcm.gtfs;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.goodway.model.gtfs.Route;
import io.goodway.model.gtfs.Schema;
import io.goodway.model.gtfs.Service;
import io.goodway.model.gtfs.Stop;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by antoine on 5/14/16.
 */
public class HttpRequest {

    private static String TAG = "HttpRequest";

    private static final String BASE_URL = "http://gtfs.goodway.io/";

    public static void schemas(final Action<List<Schema>> action){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL+"schemas/list")
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                action.error();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    JSONObject jsonResult = null;
                    ArrayList<Schema> schemas = new ArrayList<Schema>();
                    try {
                        jsonResult = new JSONObject(response.body().string());
                        JSONArray array = jsonResult.optJSONArray("schemas");
                        for(int i=0;i<array.length();i++){
                            JSONObject obj = array.optJSONObject(i);
                            schemas.add(new Schema(obj.optString("schema_name")));
                        }
                        action.success(schemas);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        action.error();
                    }
                }
            }
        });
    }

    public static void routes(final Action<List<Route>> action, Schema schema){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL+"routes/list?schema="+schema.getName())
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                action.error();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    JSONObject jsonResult = null;
                    ArrayList<Route> routes = new ArrayList<Route>();
                    try {
                        jsonResult = new JSONObject(response.body().string());
                        JSONArray array = jsonResult.optJSONArray("routes");
                        for(int i=0;i<array.length();i++){
                            JSONObject obj = array.optJSONObject(i);
                            routes.add(new Route(
                                    obj.optString("route_id"),
                                    obj.optString("route_short_name")
                            ));
                        }
                        action.success(routes);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        action.error();
                    }
                }
            }
        });
    }

    public static void services(final Action<List<Service>> action, Schema schema, Route route){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL+"services/list?schema="+schema.getName()+"&route_id="+route.getRoute_id())
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                action.error();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    JSONObject jsonResult = null;
                    ArrayList<Service> services = new ArrayList<Service>();
                    try {
                        jsonResult = new JSONObject(response.body().string());
                        JSONArray array = jsonResult.optJSONArray("services");
                        for(int i=0;i<array.length();i++){
                            JSONObject obj = array.optJSONObject(i);
                            services.add(new Service(
                                    obj.optString("route_id"),
                                    obj.optString("trip_headsign"),
                                    obj.optInt("direction_id")
                                    ));
                        }
                        action.success(services);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        action.error();
                    }
                }
            }
        });
    }

    public static void stops(final Action<List<Stop>> action, Schema schema, Service service){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL+"stops/list?schema="+schema.getName()+"&route_id="+service.getRoute_id()+"&direction_id="+service.getDirection())
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                action.error();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    JSONObject jsonResult = null;
                    ArrayList<Stop> services = new ArrayList<Stop>();
                    try {
                        jsonResult = new JSONObject(response.body().string());
                        JSONArray array = jsonResult.optJSONArray("stops");
                        for(int i=0;i<array.length();i++){
                            JSONObject obj = array.optJSONObject(i);
                            services.add(new Stop(
                                    obj.optString("stop_name"),
                                    obj.optString("stop_id")
                            ));
                        }
                        action.success(services);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        action.error();
                    }
                }
            }
        });
    }

    public static void subscribe(final Action<Void> action, String token, Schema schema, Route route, Stop stop){
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("token",token)
                .add("schema", schema.getName())
                .add("route", route.getRoute_id())
                .add("schema", stop.getStop_id())
                .build();

        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/messages/add")
                .post(formBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                action.error();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                action.success(null);
            }
        });
    }

    public interface Action<T>{
        public void success(T t);
        public void error();
    }


}
