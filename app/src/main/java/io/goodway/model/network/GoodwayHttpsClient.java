package io.goodway.model.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import io.goodway.R;
import io.goodway.model.Event;
import io.goodway.model.User;
import io.goodway.model.callback.FinishCallback;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.Pair;
import io.goodway.navitia_android.UserLocation;
import io.goodway.navitia_android.Way;

/**
 * Created by antoine on 10/25/15.
 */


public class GoodwayHttpsClient<T> extends AsyncTask<Pair, T, Integer>{

    private Context c;
    private Action<T> action;
    private ErrorAction error;
    private FinishCallback finish;
    private ProcessJson<T> processJson;
    private String url;

    private GoodwayHttpsClient(Context c, ProcessJson<T> processJson, Action<T> action, ErrorAction error, final String URL, Pair... pairs){
        this.c = c;
        this.action = action;
        this.error = error;
        this.processJson = processJson;
        this.url = URL;
    }
    private GoodwayHttpsClient(Context c, ProcessJson<T> processJson, Action<T> action, ErrorAction error, FinishCallback finish, final String URL, Pair... pairs){
        this.c = c;
        this.action = action;
        this.error = error;
        this.finish = finish;
        this.processJson = processJson;
        this.url = URL;
    }

    public static AsyncTask getUsers(Context c, Action<User> action, String mail, String password){
        return new GoodwayHttpsClient<>(c, new ProcessJson<User>() {
            @Override
            public User processJson(JSONObject jsonObject) {
                String fname = jsonObject.optString("fname");
                String lname = jsonObject.optString("lname");
                Log.d(fname+" "+lname, "found someone");
                return new User(fname, lname, false);
            }
        }, action, null, "https://api.goodway.io/users.php").execute(new Pair("mail", mail), new Pair("pass", password));
    }

    public static AsyncTask getSelfLocations(final Context c, Action<UserLocation> action, ErrorAction error, FinishCallback finish, String mail, String password, final String fname){
        return new GoodwayHttpsClient<>(c, new ProcessJson<UserLocation>() {
            @Override
            public UserLocation processJson(JSONObject jsonObject) {
                int id = jsonObject.optInt("id");
                String a_name = jsonObject.optString("a_name");
                String s_name = jsonObject.optString("s_name");
                String lat = jsonObject.optString("st_y");
                String lng = jsonObject.optString("st_x");
                boolean shared = jsonObject.optBoolean("shared");
                try{
                    return new UserLocation(id, s_name, a_name, fname, Double.parseDouble(lat), Double.parseDouble(lng), shared);
                }
                catch (NumberFormatException e){
                    return null;
                }
            }
        }, action, error, finish, "https://api.goodway.io/user_locations.php").execute(new Pair("mail", mail), new Pair("pass", password));
    }

    public static AsyncTask getUserLocations(final Context c, Action<UserLocation> action, ErrorAction error, FinishCallback finish, String mail, String password, final String fname, int id){
        return new GoodwayHttpsClient<>(c, new ProcessJson<UserLocation>() {
            @Override
            public UserLocation processJson(JSONObject jsonObject) {
                String a_name = jsonObject.optString("a_name");
                String s_name = jsonObject.optString("s_name");
                String lat = jsonObject.optString("st_y");
                String lng = jsonObject.optString("st_x");
                try{
                    return new UserLocation(s_name, a_name, fname, Double.parseDouble(lat), Double.parseDouble(lng), true);
                }
                catch (NumberFormatException e){
                    return null;
                }
            }
        }, action, error, finish, "https://api.goodway.io/user_locations.php").execute(new Pair("mail", mail), new Pair("pass", password), new Pair("id", Integer.toString(id)));
    }

    public static AsyncTask addLocation(final Context c, Action<Boolean> action, ErrorAction error, String mail, String password, UserLocation address){
        return new GoodwayHttpsClient<>(c, new ProcessJson<Boolean>() {
            @Override
            public Boolean processJson(JSONObject jsonObject) {
                return true;
            }
        }, action, error, "https://api.goodway.io/add_location.php").execute(new Pair("mail", mail), new Pair("pass", password), new Pair("a_name", address.getA_name()), new Pair("s_name", address.getName()),
                new Pair("shared", Boolean.toString(address.shared())), new Pair("lat", Double.toString(address.getLatitude())), new Pair("lng", Double.toString(address.getLongitude())));
    }

    public static AsyncTask updateLocation(final Context c, Action<Boolean> action, ErrorAction error, String mail, String password, UserLocation address){
        return new GoodwayHttpsClient<>(c, new ProcessJson<Boolean>() {
            @Override
            public Boolean processJson(JSONObject jsonObject) {
                return true;
            }
        }, action, error, "https://api.goodway.io/update_location.php").execute(new Pair("mail", mail), new Pair("pass", password), new Pair("a_name", address.getA_name()), new Pair("s_name", address.getName()),
                new Pair("loc_id", Integer.toString(address.getId())), new Pair("shared", Boolean.toString(address.shared())), new Pair("lat", Double.toString(address.getLatitude())), new Pair("lng", Double.toString(address.getLongitude())));
    }

    public static AsyncTask checkMailAvailability(Context c, Action<Integer> action, ErrorAction error, String mail){
        return new GoodwayHttpsClient<>(c, new ProcessJson<Integer>() {
            @Override
            public Integer processJson(JSONObject jsonObject) {
                Integer id = jsonObject.optInt("id");
                return id;
            }
        }, action, error, "https://api.goodway.io/check_availability.php").execute(new Pair("mail", mail));
    }
    public static AsyncTask authenticate(Context c, Action<User> action, ErrorAction error, final String mail, String password){
        return new GoodwayHttpsClient<>(c, new ProcessJson<User>() {
            @Override
            public User processJson(JSONObject jsonObject) {
                int id = jsonObject.optInt("id");
                String firstName = jsonObject.optString("fname");
                String lastName = jsonObject.optString("lname");
                return new User(id, firstName, lastName, mail, false);
            }
        }, action, error, "https://api.goodway.io/login.php").execute(new Pair("mail", mail), new Pair("pass", password));
    }
    public static AsyncTask register(Context c, Action<User> action, ErrorAction error, final String mail, String password, final String fname, final String lname, String birthday){
        return new GoodwayHttpsClient<>(c, new ProcessJson<User>() {
            @Override
            public User processJson(JSONObject jsonObject) {
                int id = jsonObject.optInt("Id");
                return new User(id, fname, lname, mail, false);
            }
        }, action, error, "https://api.goodway.io/register.php").execute(new Pair("mail", mail), new Pair("pass", password)
        , new Pair("fname", fname), new Pair("lname", lname), new Pair("bday", birthday));
    }
    public static AsyncTask getFriends(Context c, Action<User> action, ErrorAction error, String mail, String password){
        return new GoodwayHttpsClient<>(c, new ProcessJson<User>() {
            @Override
            public User processJson(JSONObject jsonObject) {
                Integer id = jsonObject.optInt("id");
                String fname = jsonObject.optString("fname");
                String lname = jsonObject.optString("lname");
                return new User(id, fname, lname, true);
            }
        }, action, error, "https://api.goodway.io/friends.php").execute(new Pair("mail", mail), new Pair("pass", password), new Pair("pending", "false"));
    }
    public static AsyncTask getFriendsPending(Context c, Action<User> action, String mail, String password){
        return new GoodwayHttpsClient<>(c, new ProcessJson<User>() {
            @Override
            public User processJson(JSONObject jsonObject) {
                Integer id = jsonObject.optInt("id");
                String fname = jsonObject.optString("fname");
                String lname = jsonObject.optString("lname");
                return new User(id, fname, lname, false);
            }
        }, action, null, "https://api.goodway.io/friends_pending.php").execute(new Pair("mail", mail), new Pair("pass", password));
    }
    public static AsyncTask getFriendsRequest(Context c, Action<User> action, ErrorAction error, String mail, String password){
        return new GoodwayHttpsClient<>(c, new ProcessJson<User>() {
            @Override
            public User processJson(JSONObject jsonObject) {
                Integer id = jsonObject.optInt("id");
                String fname = jsonObject.optString("fname");
                String lname = jsonObject.optString("lname");
                return new User(id, fname, lname, false);
            }
        }, action, error, "https://api.goodway.io/friends_request.php").execute(new Pair("mail", mail), new Pair("pass", password));
    }
    public static AsyncTask getEvents(Context c, Action<Event> action, ErrorAction error, String mail, String password){
        return new GoodwayHttpsClient<>(c, new ProcessJson<Event>() {
            @Override
            public Event processJson(JSONObject jsonObject) {
                Integer id = jsonObject.optInt("id");
                String name = jsonObject.optString("name");
                String s_time = jsonObject.optString("s_time");
                String e_time = jsonObject.optString("e_time");
                double lat = jsonObject.optDouble("st_x");
                double lng = jsonObject.optDouble("st_y");
                String html = jsonObject.optString("html");
                return new Event(id, name, html, s_time, e_time, lat, lng);
            }
        }, action, error, "https://api.goodway.io/event.php").execute(new Pair("mail", mail), new Pair("pass", password), new Pair("city", "1"));
    }

    public static AsyncTask acceptFriend(Context c, Action<Integer> action, ErrorAction error, String mail, String password, int id){
            return new GoodwayHttpsClient<>(c, new ProcessJson<Integer>() {
                @Override
                public Integer processJson(JSONObject jsonObject) {
                    Integer id = jsonObject.optInt("Id");
                    return id;
                }
            }, action, error, "https://api.goodway.io/accept_friend.php").execute(
                    new Pair("mail", mail), new Pair("pass", password),
                    new Pair("f_id", Integer.toString(id)));
    }
    public static AsyncTask requestFriend(Context c, Action<Boolean> action, ErrorAction error, String mail, String password, int id){
            return new GoodwayHttpsClient<>(c, new ProcessJson<Boolean>() {
                @Override
                public Boolean processJson(JSONObject jsonObject) {
                    return true;
                }
            }, action, error, "https://api.goodway.io/request_friend.php").execute(
                    new Pair("uid", Integer.toString(id)),
                    new Pair("mail", mail), new Pair("pass", password));
    }

    public static AsyncTask setSharing(Context c, Action<Boolean> action, ErrorAction error, String mail, String password, int id, boolean state){
        return new GoodwayHttpsClient<>(c, new ProcessJson<Boolean>() {
            @Override
            public Boolean processJson(JSONObject jsonObject) {
                return true;
            }
        }, action, error, "https://api.goodway.io/update_sharing.php").execute(
                new Pair("mail", mail), new Pair("Password", password),
                new Pair("id", Integer.toString(id)), new Pair("state", Boolean.toString(state)));
    }

    public static AsyncTask getUsersFromName(Context c, Action<User> action, ErrorAction error, String mail, String password, String fname, String lname){
        if(fname != null ){
            if(lname!=null){
                return new GoodwayHttpsClient<>(c, new ProcessJson<User>() {
                    @Override
                    public User processJson(JSONObject jsonObject) {
                        Integer id = jsonObject.optInt("id");
                        String fname = jsonObject.optString("fname");
                        String lname = jsonObject.optString("lname");
                        return new User(id, fname, lname, false);
                    }
                }, action, error, "https://api.goodway.io/users.php").execute(
                        new Pair("u1", fname), new Pair("u2", lname),
                        new Pair("mail", mail), new Pair("pass", password));
            }
            else{
                return new GoodwayHttpsClient<>(c, new ProcessJson<User>() {
                    @Override
                    public User processJson(JSONObject jsonObject) {
                        Integer id = jsonObject.optInt("id");
                        String fname = jsonObject.optString("fname");
                        String lname = jsonObject.optString("lname");
                        return new User(id, fname, lname, false);
                    }
                }, action, error, "https://api.goodway.io/users.php").execute(
                        new Pair("u1", fname),
                        new Pair("mail", mail), new Pair("pass", password));
            }
        }
        return null;
    }



    public static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static HttpsURLConnection setUpHttpsConnection(Context c, String urlString) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            // My CRT file that I put in the assets folder
            // I got this file by following these steps:
            // * Go to https://littlesvr.ca using Firefox
            // * Click the padlock/More/Security/View Certificate/Details/Export
            // * Saved the file as littlesvr.crt (type X.509 Certificate (PEM))
            // The MainActivity.context is declared as:
            // public static Context context;
            // And initialized in MainActivity.onCreate() as:
            // MainActivity.context = getApplicationContext();
            //InputStream caInput = new BufferedInputStream(c.getResources().openRawResource(R.raw.self_ssl));
            //Certificate ca = cf.generateCertificate(caInput);
            //System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = "BKS";
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            String mdp = ")qiJ7Y^BrX2nEGX2Tte2";
            keyStore.load(new BufferedInputStream(c.getResources().openRawResource(R.raw.goodway_keystore)), mdp.toCharArray());
            Certificate ca = keyStore.getCertificate("goodway");
            if (ca!=null){
                Log.d("ca=" + ((X509Certificate) ca).getSubjectDN(), "ca found");
            }
            Log.d(keyStore.toString(), "keystore");

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
            Log.d(tmf.toString(), "tmf");

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            // Tell the URLConnection to use a SocketFactory from our SSLContext
            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false); // Don't use a Cached Copy
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****");
            return urlConnection;
    }


    @Override
    protected Integer doInBackground(Pair... pairs) {
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int length=0;
        try {
            HttpsURLConnection urlConnection = setUpHttpsConnection(c, url);
            dos = new DataOutputStream(urlConnection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            //Adding Parameter name
            for(Pair p : pairs){
                dos.writeBytes("Content-Disposition: form-data; name=\""+p.first+"\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(p.second); // mobile_no is String variable
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                Log.d("writing : "+p.first+"="+p.second, "writing to post");
            }
            int serverResponseCode = urlConnection.getResponseCode();
            String serverResponseMessage = urlConnection.getResponseMessage();

            Log.d(serverResponseMessage, "Response message");
            String jsonResult;
            if (serverResponseCode == 201 || serverResponseCode == 200) {
                Log.d(urlConnection.getResponseCode() + "", "response code");
                InputStream response = urlConnection.getInputStream();
                jsonResult = convertStreamToString(response);
                Log.d("response:", jsonResult.toString());
                try {
                    JSONArray jsonArray= new JSONArray(jsonResult.toString());
                    length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        publishProgress(processJson.processJson(jsonObject));
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Log.d("error", "json exception");
                    e.printStackTrace();
                }
                dos.flush();
                dos.close();
            }
        } catch (IOException e) {
            length=-1;
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
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
            finish.action();
        }
    }

    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static boolean isConnected(Context c) {
        return getConnectivityStatus(c) != TYPE_NOT_CONNECTED;
    }


}