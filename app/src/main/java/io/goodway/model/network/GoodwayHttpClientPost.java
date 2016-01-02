package io.goodway.model.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import io.goodway.model.Group;
import io.goodway.model.GroupEvent;
import io.goodway.model.User;
import io.goodway.model.callback.FinishCallback;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.GroupLocation;
import io.goodway.navitia_android.UserLocation;

/**
 * Created by antoine on 10/25/15.
 */


public class GoodwayHttpClientPost<T> extends AsyncTask<AbstractMap.SimpleEntry<String, String>, T, Integer>{

    private Context c;
    private Action<T> action;
    private ErrorAction error;
    private FinishCallback finish;
    private ProcessJson<T> processJson;
    private String url;

    private GoodwayHttpClientPost(Context c, ProcessJson<T> processJson, Action<T> action, ErrorAction error, final String URL){
        this.c = c;
        this.action = action;
        this.error = error;
        this.processJson = processJson;
        this.url = URL;
    }
    private GoodwayHttpClientPost(Context c, ProcessJson<T> processJson, Action<T> action, ErrorAction error, FinishCallback finish, final String URL){
        this.c = c;
        this.action = action;
        this.error = error;
        this.finish = finish;
        this.processJson = processJson;
        this.url = URL;
    }

    public static AsyncTask getMyLocations(final Context c, Action<List<UserLocation>> action, ErrorAction error, FinishCallback finish, String token){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<List<UserLocation>>() {
            @Override
            public List<UserLocation> processJson(JSONObject jsonObject) throws JSONException {
                if(jsonObject.optBoolean("success")) {
                    ArrayList userLocation = new ArrayList();
                    JSONArray jsonArray = jsonObject.getJSONArray("locations");
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject obj = jsonArray.getJSONObject(i);
                        int id = obj.optInt("id");
                        String a_name = obj.optString("a_name");
                        String s_name = obj.optString("s_name");
                        String lat = obj.optString("st_y");
                        String lng = obj.optString("st_x");
                        boolean shared = obj.optBoolean("shared");
                        try {
                            userLocation.add(new UserLocation(id, s_name, a_name, Double.parseDouble(lat), Double.parseDouble(lng), shared));
                        } catch (NumberFormatException e) {}
                    }
                    return userLocation;

                }
                return null;
            }
        }, action, error, finish, "http://developer.goodway.io/api/v1/me/locations").execute(new AbstractMap.SimpleEntry<String, String>("token", token));
    }

    public static AsyncTask getUserLocations(final Context c, Action<List<UserLocation>> action, ErrorAction error, FinishCallback finish, String token, int id){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<List<UserLocation>>() {
            @Override
            public List<UserLocation> processJson(JSONObject jsonObject) throws JSONException {
                if(jsonObject.optBoolean("success")) {
                    ArrayList userLocation = new ArrayList();
                    JSONArray jsonArray = jsonObject.getJSONArray("locations");
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject obj = jsonArray.getJSONObject(i);
                        int id = obj.optInt("id");
                        String a_name = obj.optString("a_name");
                        String s_name = obj.optString("s_name");
                        String lat = obj.optString("st_y");
                        String lng = obj.optString("st_x");
                        boolean shared = obj.optBoolean("shared");
                        try {
                            userLocation.add(new UserLocation(id, s_name, a_name, Double.parseDouble(lat), Double.parseDouble(lng), shared));
                        } catch (NumberFormatException e) {}
                    }
                    return userLocation;

                }
                return null;
            }
        }, action, error, finish, "http://developer.goodway.io/api/v1/user/locations").execute(new AbstractMap.SimpleEntry<String, String>("token", token), new AbstractMap.SimpleEntry<String, String>("id", Integer.toString(id)));
    }

    public static AsyncTask getGroupLocations(final Context c, Action<GroupLocation> action, ErrorAction error, FinishCallback finish, String token, final Group group){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<GroupLocation>() {
            @Override
            public GroupLocation processJson(JSONObject jsonObject) {
                String a_name = jsonObject.optString("a_name");
                String s_name = jsonObject.optString("s_name");
                String lat = jsonObject.optString("st_y");
                String lng = jsonObject.optString("st_x");
                try{
                    return new GroupLocation(group.getId(), s_name, a_name, Double.parseDouble(lat), Double.parseDouble(lng));
                }
                catch (NumberFormatException e){
                    return null;
                }
            }
        }, action, error, finish, "http://developer.goodway.io/api/v1/group/locations").execute(new AbstractMap.SimpleEntry<String, String>("token", token), new AbstractMap.SimpleEntry<String, String>("id", Integer.toString(group.getId())));
    }

    public static AsyncTask me(final Context c, Action<User> action, ErrorAction error, FinishCallback finish, String token){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<User>() {
            @Override
            public User processJson(JSONObject jsonObject) throws JSONException {
                JSONObject obj = jsonObject.getJSONArray("user").getJSONObject(0);
                int id = obj.optInt("id");
                String mail = obj.optString("mail");
                String fname = obj.optString("fname");
                String lname = obj.optString("lname");
                String avatar = obj.optString("avatar");
                int title = obj.optInt("title");
                String city = obj.optString("city");
                return new User(id, mail, fname, lname, avatar, title, city, false);
            }
        }, action, error, finish, "http://developer.goodway.io/api/v1/me").execute(new AbstractMap.SimpleEntry<String, String>("token", token));
    }


    public static AsyncTask addLocation(final Context c, Action<Boolean> action, ErrorAction error, String token, UserLocation address){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<Boolean>() {
            @Override
            public Boolean processJson(JSONObject jsonObject) {
                return true;
            }
        }, action, error, "http://developer.goodway.io/api/v1/me/locations/add").execute(new AbstractMap.SimpleEntry<String, String>("token", token), new AbstractMap.SimpleEntry<String, String>("a_name", address.getA_name()), new AbstractMap.SimpleEntry<String, String>("s_name", address.getName()),
                new AbstractMap.SimpleEntry<String, String>("shared", Boolean.toString(address.shared())), new AbstractMap.SimpleEntry<String, String>("lat", Double.toString(address.getLatitude())), new AbstractMap.SimpleEntry<String, String>("lng", Double.toString(address.getLongitude())));
    }

    public static AsyncTask updateLocation(final Context c, Action<Boolean> action, ErrorAction error, String token, UserLocation address){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<Boolean>() {
            @Override
            public Boolean processJson(JSONObject jsonObject) {
                return true;
            }
        }, action, error, "http://developer.goodway.io/api/v1/me/locations/update").execute(new AbstractMap.SimpleEntry<String, String>("token", token), new AbstractMap.SimpleEntry<String, String>("a_name", address.getA_name()), new AbstractMap.SimpleEntry<String, String>("s_name", address.getName()),
                new AbstractMap.SimpleEntry<String, String>("id", Integer.toString(address.getId())), new AbstractMap.SimpleEntry<String, String>("shared", Boolean.toString(address.shared())), new AbstractMap.SimpleEntry<String, String>("lat", Double.toString(address.getLatitude())), new AbstractMap.SimpleEntry<String, String>("lng", Double.toString(address.getLongitude())));
    }

    public static AsyncTask deleteLocation(final Context c, Action<Boolean> action, ErrorAction error, String token, UserLocation address){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<Boolean>() {
            @Override
            public Boolean processJson(JSONObject jsonObject) {
                return true;
            }
        }, action, error, "http://developer.goodway.io/api/v1/me/locations/delete").execute(new AbstractMap.SimpleEntry<String, String>("token", token), new AbstractMap.SimpleEntry<String, String>("id", Integer.toString(address.getId())));
    }

    public static AsyncTask authenticate(Context c, Action<User> action, ErrorAction error, final String mail, String password){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<User>() {
            @Override
            public User processJson(JSONObject jsonObject) throws JSONException {
                boolean success = jsonObject.getBoolean("success");
                if(success) {
                    String token = jsonObject.optString("token");
                    JSONObject obj = jsonObject.getJSONArray("user").getJSONObject(0);
                    int id = obj.optInt("id");
                    String mail = obj.optString("mail");
                    String fname = obj.optString("fname");
                    String lname = obj.optString("lname");
                    String avatar = obj.optString("avatar");
                    int title = obj.optInt("title");
                    String city = obj.optString("city");
                    return new User(id, mail, fname, lname, avatar, title, city, false, token);
                }
                return null;
            }
        }, action, error, "http://developer.goodway.io/api/v1/authentication/user").execute(new AbstractMap.SimpleEntry<String, String>("mail", mail), new AbstractMap.SimpleEntry<String, String>("password", password));
    }
    public static AsyncTask register(Context c, Action<User> action, ErrorAction error, final String mail, String password, final String fname, final String lname, String birthday){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<User>() {
            @Override
            public User processJson(JSONObject jsonObject) {
                int id = jsonObject.optInt("id");
                int title = jsonObject.optInt("title");
                return new User(id, mail, fname, lname, mail, title, null, false);
            }
        }, action, error, "http://developer.goodway.io/api/v1/authentication/register?").execute(new AbstractMap.SimpleEntry<String, String>("mail", mail), new AbstractMap.SimpleEntry<String, String>("password", password)
        , new AbstractMap.SimpleEntry<String, String>("fname", fname), new AbstractMap.SimpleEntry<String, String>("lname", lname), new AbstractMap.SimpleEntry<String, String>("birthday", birthday));
    }
    public static AsyncTask getFriends(Context c, Action<List<User>> action, ErrorAction error, String token){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<List<User>>() {
            @Override
            public List<User> processJson(JSONObject jsonObject) throws JSONException {
                if(jsonObject.optBoolean("success")) {
                    ArrayList users = new ArrayList();
                    JSONArray jsonArray = jsonObject.getJSONArray("friends");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        Integer id = obj.optInt("id");
                        String fname = obj.optString("fname");
                        String lname = obj.optString("lname");
                        String avatar = obj.optString("avatar");
                        int title = obj.optInt("title");
                        String city = obj.optString("city");
                        if (city == "null") {
                            city = null;
                        }
                        users.add(new User(id, fname, lname, avatar, title, city, true));
                    }
                    return users;
                }
                return null;
            }
        }, action, error, "http://developer.goodway.io/api/v1/me/friends").execute(new AbstractMap.SimpleEntry<String, String>("token", token));
    }

    public static AsyncTask getFriendRequests(Context c, Action<List<User>> action, ErrorAction error, FinishCallback finish, String token){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<List<User>>() {
            @Override
            public List<User> processJson(JSONObject jsonObject) throws JSONException {
                if(jsonObject.optBoolean("success")) {
                    ArrayList users = new ArrayList();
                    JSONArray jsonArray = jsonObject.getJSONArray("requests");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        Integer id = obj.optInt("id");
                        String fname = obj.optString("fname");
                        String lname = obj.optString("lname");
                        String avatar = obj.optString("avatar");
                        int title = obj.optInt("title");
                        String city = obj.optString("city");
                        if (city == "null") {
                            city = null;
                        }
                        users.add(new User(id, fname, lname, avatar, title, city, true));
                    }
                    return users;
                }
                return null;
            }
        }, action, error, finish, "http://developer.goodway.io/api/v1/me/friends/requests").execute(new AbstractMap.SimpleEntry<String, String>("token", token));
    }
    public static AsyncTask countFriendRequests(Context c, Action<Integer> action, ErrorAction error, String token){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<Integer>() {
            @Override
            public Integer processJson(JSONObject jsonObject) throws JSONException {
                return jsonObject.getJSONArray("count").getJSONObject(0).optInt("count");
            }
        }, action, error, null, "http://developer.goodway.io/api/v1/me/friends/requests/count").execute(new AbstractMap.SimpleEntry<String, String>("token", token));
    }
    public static AsyncTask getEvents(Context c, Action<GroupEvent> action, ErrorAction error, FinishCallback finish, String token){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<GroupEvent>() {
            @Override
            public GroupEvent processJson(JSONObject jsonObject) {
                Integer id = jsonObject.optInt("id");
                String name = jsonObject.optString("name");
                String avatar = jsonObject.optString("avatar");
                String s_time = jsonObject.optString("s_time");
                String e_time = jsonObject.optString("e_time");
                double lat = jsonObject.optDouble("st_x");
                double lng = jsonObject.optDouble("st_y");
                String html = jsonObject.optString("html");
                return new GroupEvent(id, name, html, avatar, s_time, e_time, lat, lng);
            }
        }, action, error,finish, "http://developer.goodway.io/api/v1/me/events").execute(new AbstractMap.SimpleEntry<String, String>("token", token));
    }

    public static AsyncTask acceptFriend(Context c, Action<Integer> action, ErrorAction error, String mail, String password, int id){
            return new GoodwayHttpClientPost<>(c, new ProcessJson<Integer>() {
                @Override
                public Integer processJson(JSONObject jsonObject) {
                    Integer id = jsonObject.optInt("Id");
                    return id;
                }
            }, action, error, "https://api.goodway.io/accept_friend.php").execute(
                    new AbstractMap.SimpleEntry<String, String>("mail", mail), new AbstractMap.SimpleEntry<String, String>("pass", password),
                    new AbstractMap.SimpleEntry<String, String>("f_id", Integer.toString(id)));
    }
    public static AsyncTask requestFriend(Context c, Action<Boolean> action, ErrorAction error, String mail, String password, int id){
            return new GoodwayHttpClientPost<>(c, new ProcessJson<Boolean>() {
                @Override
                public Boolean processJson(JSONObject jsonObject) {
                    return true;
                }
            }, action, error, "https://api.goodway.io/request_friend.php").execute(
                    new AbstractMap.SimpleEntry<String, String>("uid", Integer.toString(id)),
                    new AbstractMap.SimpleEntry<String, String>("mail", mail), new AbstractMap.SimpleEntry<String, String>("pass", password));
    }

    public static AsyncTask updateMyCity(Context c, Action<Boolean> action, ErrorAction error, String token, String city){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<Boolean>() {
            @Override
            public Boolean processJson(JSONObject jsonObject) {
                return true;
            }
        }, action, error, "http://developer.goodway.io/api/v1/me/city/update").execute(
                new AbstractMap.SimpleEntry<String, String>("token", token),
                new AbstractMap.SimpleEntry<String, String>("city", city));
    }

    public static AsyncTask getUsersFromName(Context c, Action<User> action, ErrorAction error, String mail, String password, String fname, String lname){
        if(fname != null ){
            if(lname!=null){
                return new GoodwayHttpClientPost<>(c, new ProcessJson<User>() {
                    @Override
                    public User processJson(JSONObject jsonObject) {
                        Integer id = jsonObject.optInt("id");
                        String fname = jsonObject.optString("fname");
                        String lname = jsonObject.optString("lname");
                        String avatar = jsonObject.optString("avatar");
                        int title = jsonObject.optInt("title");
                        return new User(id, fname, lname, avatar, title, null, false);
                    }
                }, action, error, "https://api.goodway.io/users.php").execute(
                        new AbstractMap.SimpleEntry<String, String>("u1", fname), new AbstractMap.SimpleEntry<String, String>("u2", lname),
                        new AbstractMap.SimpleEntry<String, String>("mail", mail), new AbstractMap.SimpleEntry<String, String>("pass", password));
            }
            else{
                return new GoodwayHttpClientPost<>(c, new ProcessJson<User>() {
                    @Override
                    public User processJson(JSONObject jsonObject) {
                        Integer id = jsonObject.optInt("id");
                        String fname = jsonObject.optString("fname");
                        String lname = jsonObject.optString("lname");
                        String avatar = jsonObject.optString("avatar");
                        int title = jsonObject.optInt("title");
                        return new User(id, fname, lname, avatar, title, null, false);
                    }
                }, action, error, "https://api.goodway.io/users.php").execute(
                        new AbstractMap.SimpleEntry<String, String>("u1", fname),
                        new AbstractMap.SimpleEntry<String, String>("mail", mail), new AbstractMap.SimpleEntry<String, String>("pass", password));
            }
        }
        return null;
    }

    public static AsyncTask findGroups(Context c, Action<List<Group>> action, ErrorAction error, String token, String name) {
        if(name!=null) {
            return new GoodwayHttpClientPost<>(c, new ProcessJson<List<Group>>() {
                @Override
                public List<Group> processJson(JSONObject jsonObject) throws JSONException {
                    if (jsonObject.optBoolean("success")) {
                        ArrayList groups = new ArrayList();
                        JSONArray jsonArray = jsonObject.getJSONArray("groups");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Integer id = jsonObject.optInt("id");
                            String name = jsonObject.optString("name");
                            String description = jsonObject.optString("description");
                            String avatar = jsonObject.optString("avatar");
                            groups.add(new Group(id, name, description, avatar));
                        }
                        return groups;
                    }
                    return null;
                }
            }, action, error, "http://developer.goodway.io/api/v1/me/groups/find").execute(
                    new AbstractMap.SimpleEntry<String, String>("token", token), new AbstractMap.SimpleEntry<String, String>("name", name));
        }
        return null;
    }
    public static AsyncTask getMyGroups(Context c, Action<List<Group>> action, ErrorAction error, String token) {
            return new GoodwayHttpClientPost<>(c, new ProcessJson<List<Group>>() {
                @Override
                public List<Group> processJson(JSONObject jsonObject) throws JSONException {
                    if(jsonObject.optBoolean("success")) {
                        ArrayList groups = new ArrayList();
                        JSONArray jsonArray = jsonObject.getJSONArray("groups");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Integer id = jsonObject.optInt("id");
                            String name = jsonObject.optString("name");
                            String description = jsonObject.optString("description");
                            String avatar = jsonObject.optString("avatar");
                            groups.add(new Group(id, name, description, avatar));
                        }
                        return groups;
                    }
                    return null;
                }
            }, action, error, "http://developer.goodway.io/api/v1/me/groups").execute(
                    new AbstractMap.SimpleEntry<String, String>("token", token));
    }
    public static AsyncTask joinGroup(Context c, Action<Void> action, ErrorAction error, String token, Group group) {
            return new GoodwayHttpClientPost<>(c, new ProcessJson<Void>() {
                @Override
                public Void processJson(JSONObject jsonObject) {
                    return null;
                }
            }, action, error, "http://developer.goodway.io/api/v1/group/join").execute(
                    new AbstractMap.SimpleEntry<String, String>("id", Integer.toString(group.getId())),
                    new AbstractMap.SimpleEntry<String, String>("token", token));
    }

    public static AsyncTask quitGroup(Context c, Action<Void> action, ErrorAction error, String token, Group group) {
        return new GoodwayHttpClientPost<>(c, new ProcessJson<Void>() {
            @Override
            public Void processJson(JSONObject jsonObject) {
                return null;
            }
        }, action, error, "http://developer.goodway.io/api/v1/group/quit").execute(
                new AbstractMap.SimpleEntry<String, String>("id", Integer.toString(group.getId())),
                new AbstractMap.SimpleEntry<String, String>("token", token));
    }

    public static AsyncTask getUpcomingEvents(Context c, Action<GroupEvent> action, ErrorAction error, String token, Group group) {
        return new GoodwayHttpClientPost<>(c, new ProcessJson<GroupEvent>() {
            @Override
            public GroupEvent processJson(JSONObject jsonObject) {
                Integer id = jsonObject.optInt("id");
                String name = jsonObject.optString("name");
                String avatar = jsonObject.optString("avatar");
                String s_time = jsonObject.optString("s_time");
                String e_time = jsonObject.optString("e_time");
                double lat = jsonObject.optDouble("st_x");
                double lng = jsonObject.optDouble("st_y");
                String html = jsonObject.optString("html");
                return new GroupEvent(id, name, html, avatar, s_time, e_time, lat, lng);
            }
        }, action, error, "http://developer.goodway.io/api/v1/group/events/upcoming").execute(
                new AbstractMap.SimpleEntry<String, String>("id", Integer.toString(group.getId())),
                new AbstractMap.SimpleEntry<String, String>("token", token));
    }

    public static AsyncTask getUberPrices(Context c, Action<GroupEvent> action, ErrorAction error, double start_latitude, double start_longitude, double end_latitude, double end_longitude) {
        return new GoodwayHttpClientPost<>(c, new ProcessJson<GroupEvent>() {
            @Override
            public GroupEvent processJson(JSONObject jsonObject) {
                Integer id = jsonObject.optInt("id");
                String name = jsonObject.optString("name");
                String avatar = jsonObject.optString("avatar");
                String s_time = jsonObject.optString("s_time");
                String e_time = jsonObject.optString("e_time");
                double lat = jsonObject.optDouble("st_x");
                double lng = jsonObject.optDouble("st_y");
                String html = jsonObject.optString("html");
                return new GroupEvent(id, name, html, avatar, s_time, e_time, lat, lng);
            }
        }, action, error, "https://uber.goodway.io/prices").execute(
                new AbstractMap.SimpleEntry<String, String>("start_latitude", Double.toString(start_latitude)),
                new AbstractMap.SimpleEntry<String, String>("start_longitude", Double.toString(start_longitude)),
                new AbstractMap.SimpleEntry<String, String>("end_latitude", Double.toString(end_latitude)),
                new AbstractMap.SimpleEntry<String, String>("end_longitude", Double.toString(end_longitude)));
    }

    @Override
    protected Integer doInBackground(AbstractMap.SimpleEntry<String, String>... entries) {

        int length=0;
        try {
            HttpURLConnection urlConnection = GoodwayProtocol.getHttpPostUrlConnection(this.url, entries);
            Log.d("url=", "url= "+urlConnection.toString());
            int serverResponseCode = urlConnection.getResponseCode();
            String serverResponseMessage = urlConnection.getResponseMessage();
            Log.d(serverResponseCode+"", "Response code");
            Log.d(serverResponseMessage, "Response message");
            String jsonResult;
            if (serverResponseCode == 201 || serverResponseCode == 200) {
                Log.d(urlConnection.getResponseCode() + "", "response code");
                InputStream response = urlConnection.getInputStream();
                jsonResult = GoodwayProtocol.convertStreamToString(response);
                Log.d("response:", jsonResult.toString());
                try {
                    JSONObject obj= new JSONObject(jsonResult.toString());
                    publishProgress(processJson.processJson(obj));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Log.d("error", "json exception");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            length=-1;
            e.printStackTrace();
        }
        return length;
    }
    @Override
    protected void onProgressUpdate(T...progress){
        action.action(progress[0]);
    }

    protected void onPostExecute(Integer length){
        if(length<1 && error!=null){
            error.action(length);
        }
        else if(finish!=null){
            finish.action(length);
        }
    }
}
