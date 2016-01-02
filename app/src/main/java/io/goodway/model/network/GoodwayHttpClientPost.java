package io.goodway.model.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.AbstractMap;

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

    public static AsyncTask getMyLocations(final Context c, Action<UserLocation> action, ErrorAction error, FinishCallback finish, String token){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<UserLocation>() {
            @Override
            public UserLocation processJson(JSONObject jsonObject) {
                int id = jsonObject.optInt("id");
                String a_name = jsonObject.optString("a_name");
                String s_name = jsonObject.optString("s_name");
                String lat = jsonObject.optString("st_y");
                String lng = jsonObject.optString("st_x");
                boolean shared = jsonObject.optBoolean("shared");
                try{
                    return new UserLocation(id, s_name, a_name, Double.parseDouble(lat), Double.parseDouble(lng), shared);
                }
                catch (NumberFormatException e){
                    return null;
                }
            }
        }, action, error, finish, "http://developer.goodway.io/api/v1/me/locations").execute(new AbstractMap.SimpleEntry<String, String>("token", token));
    }

    public static AsyncTask getUserLocations(final Context c, Action<UserLocation> action, ErrorAction error, FinishCallback finish, String token, int id){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<UserLocation>() {
            @Override
            public UserLocation processJson(JSONObject jsonObject) {
                int id = jsonObject.optInt("id");
                String a_name = jsonObject.optString("a_name");
                String s_name = jsonObject.optString("s_name");
                String lat = jsonObject.optString("st_y");
                String lng = jsonObject.optString("st_x");
                try{
                    return new UserLocation(id, s_name, a_name, Double.parseDouble(lat), Double.parseDouble(lng), true);
                }
                catch (NumberFormatException e){
                    return null;
                }
            }
        }, action, error, finish, "http://developer.goodway.io/api/v1/user/locations").execute(new AbstractMap.SimpleEntry<String, String>("token", token), new AbstractMap.SimpleEntry<String, String>("id", Integer.toString(id)));
    }

    public static AsyncTask getGroupLocations(final Context c, Action<GroupLocation> action, ErrorAction error, FinishCallback finish, String mail, String password, final int id){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<GroupLocation>() {
            @Override
            public GroupLocation processJson(JSONObject jsonObject) {
                String a_name = jsonObject.optString("a_name");
                String s_name = jsonObject.optString("s_name");
                String lat = jsonObject.optString("st_y");
                String lng = jsonObject.optString("st_x");
                try{
                    return new GroupLocation(id, s_name, a_name, Double.parseDouble(lat), Double.parseDouble(lng));
                }
                catch (NumberFormatException e){
                    return null;
                }
            }
        }, action, error, finish, "https://api.goodway.io/group_locations.php").execute(new AbstractMap.SimpleEntry<String, String>("mail", mail), new AbstractMap.SimpleEntry<String, String>("pass", password), new AbstractMap.SimpleEntry<String, String>("id", Integer.toString(id)));
    }

    public static AsyncTask me(final Context c, Action<User> action, ErrorAction error, FinishCallback finish, String token){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<User>() {
            @Override
            public User processJson(JSONObject jsonObject) {
                int id = jsonObject.optInt("id");
                String mail = jsonObject.optString("mail");
                String fname = jsonObject.optString("fname");
                String lname = jsonObject.optString("lname");
                String avatar = jsonObject.optString("avatar");
                int title = jsonObject.optInt("title");
                String city = jsonObject.optString("city");
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
        }, action, error, "http://developer.goodway.io/api/v1/me/location/add").execute(new AbstractMap.SimpleEntry<String, String>("token", token), new AbstractMap.SimpleEntry<String, String>("a_name", address.getA_name()), new AbstractMap.SimpleEntry<String, String>("s_name", address.getName()),
                new AbstractMap.SimpleEntry<String, String>("shared", Boolean.toString(address.shared())), new AbstractMap.SimpleEntry<String, String>("lat", Double.toString(address.getLatitude())), new AbstractMap.SimpleEntry<String, String>("lng", Double.toString(address.getLongitude())));
    }

    public static AsyncTask updateLocation(final Context c, Action<Boolean> action, ErrorAction error, String mail, String password, UserLocation address){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<Boolean>() {
            @Override
            public Boolean processJson(JSONObject jsonObject) {
                return true;
            }
        }, action, error, "http://developer.goodway.io/api/v1/me/location/update").execute(new AbstractMap.SimpleEntry<String, String>("mail", mail), new AbstractMap.SimpleEntry<String, String>("pass", password), new AbstractMap.SimpleEntry<String, String>("a_name", address.getA_name()), new AbstractMap.SimpleEntry<String, String>("s_name", address.getName()),
                new AbstractMap.SimpleEntry<String, String>("loc_id", Integer.toString(address.getId())), new AbstractMap.SimpleEntry<String, String>("shared", Boolean.toString(address.shared())), new AbstractMap.SimpleEntry<String, String>("lat", Double.toString(address.getLatitude())), new AbstractMap.SimpleEntry<String, String>("lng", Double.toString(address.getLongitude())));
    }

    public static AsyncTask deleteLocation(final Context c, Action<Boolean> action, ErrorAction error, String mail, String password, UserLocation address){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<Boolean>() {
            @Override
            public Boolean processJson(JSONObject jsonObject) {
                return true;
            }
        }, action, error, "https://api.goodway.io/delete_user_location.php").execute(new AbstractMap.SimpleEntry<String, String>("mail", mail), new AbstractMap.SimpleEntry<String, String>("pass", password), new AbstractMap.SimpleEntry<String, String>("loc", Integer.toString(address.getId())));
    }

    public static AsyncTask authenticate(Context c, Action<String> action, ErrorAction error, final String mail, String password){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<String>() {
            @Override
            public String processJson(JSONObject jsonObject) throws JSONException {
                boolean success = jsonObject.getBoolean("success");
                if(success) {
                    return jsonObject.optString("token");
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
    public static AsyncTask getFriends(Context c, Action<User> action, ErrorAction error, String token){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<User>() {
            @Override
            public User processJson(JSONObject jsonObject) {
                Integer id = jsonObject.optInt("id");
                String fname = jsonObject.optString("fname");
                String lname = jsonObject.optString("lname");
                String avatar = jsonObject.optString("avatar");
                int title = jsonObject.optInt("title");
                String city = jsonObject.optString("city");
                if(city=="null"){city=null;}
                return new User(id, fname, lname, avatar, title, city, true);
            }
        }, action, error, "http://developer.goodway.io/api/v1/me/friends").execute(new AbstractMap.SimpleEntry<String, String>("token", token));
    }

    public static AsyncTask getFriendRequests(Context c, Action<User> action, ErrorAction error, FinishCallback finish, String token){
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
    public static AsyncTask getEvents(Context c, Action<GroupEvent> action, ErrorAction error, FinishCallback finish, String mail, String password){
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
        }, action, error,finish, "https://api.goodway.io/event.php").execute(new AbstractMap.SimpleEntry<String, String>("mail", mail), new AbstractMap.SimpleEntry<String, String>("pass", password), new AbstractMap.SimpleEntry<String, String>("city", "1"));
    }

    public static AsyncTask getGroups(Context c, Action<Group> action, ErrorAction error, String mail, String password){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<Group>() {
            @Override
            public Group processJson(JSONObject jsonObject) {
                Integer id = jsonObject.optInt("id");
                String name = jsonObject.optString("name");
                String description = jsonObject.optString("description");
                String avatar = jsonObject.optString("avatar");
                return new Group(id, name, description, avatar);
            }
        }, action, error, "https://api.goodway.io/user_group.php").execute(new AbstractMap.SimpleEntry<String, String>("mail", mail), new AbstractMap.SimpleEntry<String, String>("pass", password));
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

    public static AsyncTask setSharing(Context c, Action<Boolean> action, ErrorAction error, String mail, String password, int id, boolean state){
        return new GoodwayHttpClientPost<>(c, new ProcessJson<Boolean>() {
            @Override
            public Boolean processJson(JSONObject jsonObject) {
                return true;
            }
        }, action, error, "https://api.goodway.io/update_sharing.php").execute(
                new AbstractMap.SimpleEntry<String, String>("mail", mail), new AbstractMap.SimpleEntry<String, String>("Password", password),
                new AbstractMap.SimpleEntry<String, String>("id", Integer.toString(id)), new AbstractMap.SimpleEntry<String, String>("state", Boolean.toString(state)));
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

    public static AsyncTask getGroups(Context c, Action<Group> action, ErrorAction error, String mail, String password, String name) {
        if (name != null) {
            return new GoodwayHttpClientPost<>(c, new ProcessJson<Group>() {
                @Override
                public Group processJson(JSONObject jsonObject) {
                    Integer id = jsonObject.optInt("id");
                    String name = jsonObject.optString("name");
                    String description = jsonObject.optString("description");
                    String avatar = jsonObject.optString("avatar");
                    return new Group(id, name, description, avatar);
                }
            }, action, error, "https://api.goodway.io/groups.php").execute(
                    new AbstractMap.SimpleEntry<String, String>("name", name),
                    new AbstractMap.SimpleEntry<String, String>("mail", mail), new AbstractMap.SimpleEntry<String, String>("pass", password));
        }
        return null;
    }
    public static AsyncTask joinGroup(Context c, Action<Void> action, ErrorAction error, String mail, String password, Group group) {
            return new GoodwayHttpClientPost<>(c, new ProcessJson<Void>() {
                @Override
                public Void processJson(JSONObject jsonObject) {
                    return null;
                }
            }, action, error, "https:/api.goodway.io/join_group.php").execute(
                    new AbstractMap.SimpleEntry<String, String>("g", Integer.toString(group.getId())),
                    new AbstractMap.SimpleEntry<String, String>("mail", mail), new AbstractMap.SimpleEntry<String, String>("pass", password));
    }

    public static AsyncTask quitGroup(Context c, Action<Void> action, ErrorAction error, String mail, String password, Group group) {
        return new GoodwayHttpClientPost<>(c, new ProcessJson<Void>() {
            @Override
            public Void processJson(JSONObject jsonObject) {
                return null;
            }
        }, action, error, "https://api.goodway.io/quit_group.php").execute(
                new AbstractMap.SimpleEntry<String, String>("g", Integer.toString(group.getId())),
                new AbstractMap.SimpleEntry<String, String>("mail", mail), new AbstractMap.SimpleEntry<String, String>("pass", password));
    }

    public static AsyncTask getUpcomingEvents(Context c, Action<GroupEvent> action, ErrorAction error, String mail, String password, Group group) {
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
        }, action, error, "https://api.goodway.io/upcoming_events.php").execute(
                new AbstractMap.SimpleEntry<String, String>("group", Integer.toString(group.getId())),
                new AbstractMap.SimpleEntry<String, String>("mail", mail), new AbstractMap.SimpleEntry<String, String>("pass", password));
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
