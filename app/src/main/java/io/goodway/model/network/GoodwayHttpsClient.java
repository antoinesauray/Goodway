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
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.Pair;
import io.goodway.navitia_android.Way;

/**
 * Created by antoine on 10/25/15.
 */


public class GoodwayHttpsClient<T> extends AsyncTask<Pair, T, Integer>{

    private Context c;
    private Action<T> action;
    private ErrorAction error;
    private ProcessJson<T> processJson;
    private String url;

    private GoodwayHttpsClient(Context c, ProcessJson<T> processJson, Action<T> action, ErrorAction error, final String URL, Pair... pairs){
        this.c = c;
        this.action = action;
        this.error = error;
        this.processJson = processJson;
        this.url = URL;
    }

    public static AsyncTask getUsers(Context c, Action<User> action, String mail, String password){
        return new GoodwayHttpsClient<>(c, new ProcessJson<User>() {
            @Override
            public User processJson(JSONObject jsonObject) {
                String fname = jsonObject.optString("FirstName");
                String lname = jsonObject.optString("LastName");
                Log.d(fname+" "+lname, "found someone");
                return new User(fname, lname);
            }
        }, action, null, "https://sgorilla.goodway.io/users.php").execute(new Pair("Mail", mail), new Pair("Password", password));
    }
    public static AsyncTask checkMailAvailability(Context c, Action<Integer> action, ErrorAction error, String mail){
        return new GoodwayHttpsClient<>(c, new ProcessJson<Integer>() {
            @Override
            public Integer processJson(JSONObject jsonObject) {
                Integer id = jsonObject.optInt("Id");
                return id;
            }
        }, action, error, "https://sgorilla.goodway.io/check_availability.php").execute(new Pair("Mail", mail));
    }
    public static AsyncTask authenticate(Context c, Action<User> action, ErrorAction error, final String mail, String password){
        return new GoodwayHttpsClient<>(c, new ProcessJson<User>() {
            @Override
            public User processJson(JSONObject jsonObject) {
                int id = jsonObject.optInt("Id");
                String firstName = jsonObject.optString("FirstName");
                String lastName = jsonObject.optString("LastName");
                return new User(id, firstName, lastName, mail);
            }
        }, action, error, "https://sgorilla.goodway.io/login.php").execute(new Pair("Mail", mail), new Pair("Password", password));
    }
    public static AsyncTask getFriends(Context c, Action<User> action, ErrorAction error, String mail, String password){
        return new GoodwayHttpsClient<>(c, new ProcessJson<User>() {
            @Override
            public User processJson(JSONObject jsonObject) {
                Integer id = jsonObject.optInt("Id");
                String fname = jsonObject.optString("FirstName");
                String lname = jsonObject.optString("LastName");
                return new User(id, fname, lname);
            }
        }, action, error, "https://sgorilla.goodway.io/friends.php").execute(new Pair("Mail", mail), new Pair("Password", password));
    }
    public static AsyncTask getFriendsPending(Context c, Action<User> action, String mail, String password){
        return new GoodwayHttpsClient<>(c, new ProcessJson<User>() {
            @Override
            public User processJson(JSONObject jsonObject) {
                Integer id = jsonObject.optInt("Id");
                String fname = jsonObject.optString("FirstName");
                String lname = jsonObject.optString("LastName");
                return new User(id, fname, lname);
            }
        }, action, null, "https://sgorilla.goodway.io/friends_pending.php").execute(new Pair("Mail", mail), new Pair("Password", password));
    }
    public static AsyncTask getFriendsRequest(Context c, Action<User> action, ErrorAction error, String mail, String password){
        return new GoodwayHttpsClient<>(c, new ProcessJson<User>() {
            @Override
            public User processJson(JSONObject jsonObject) {
                Integer id = jsonObject.optInt("Id");
                String fname = jsonObject.optString("FirstName");
                String lname = jsonObject.optString("LastName");
                return new User(id, fname, lname);
            }
        }, action, error, "https://sgorilla.goodway.io/friends_request.php").execute(new Pair("Mail", mail), new Pair("Password", password));
    }
    public static AsyncTask getEvents(Context c, Action<Event> action, ErrorAction error, String mail, String password){
        return new GoodwayHttpsClient<>(c, new ProcessJson<Event>() {
            @Override
            public Event processJson(JSONObject jsonObject) {
                Integer id = jsonObject.optInt("Id");
                String name = jsonObject.optString("Name");
                String description = jsonObject.optString("Description");
                String date = jsonObject.optString("Date");
                double lat = jsonObject.optDouble("Latitude");
                double lng = jsonObject.optDouble("Longitude");
                return new Event(id, name, description, date, lat, lng);
            }
        }, action, error, "https://sgorilla.goodway.io/events.php").execute(new Pair("Mail", mail), new Pair("Password", password));
    }

    public static AsyncTask getTrips(Context c, Action<Way> action, ErrorAction error, String mail, String password, int id){
        return new GoodwayHttpsClient<>(c, new ProcessJson<Way>() {
            @Override
            public Way processJson(JSONObject jsonObject) {
                Integer id = jsonObject.optInt("Id");
                String labelStart = jsonObject.optString("LabelStart");
                String labelEnd = jsonObject.optString("LabelEnd");
                String departureDateTime = jsonObject.optString("DepartureTime");
                String arrivalDateTime = jsonObject.optString("ArrivalTime");
                double startLat = jsonObject.optDouble("LatitudeStart");
                double startLng = jsonObject.optDouble("LongitudeStart");
                double endLat = jsonObject.optDouble("LatitudeEnd");
                double endLng = jsonObject.optDouble("LongitudeEnd");
                double carbon = jsonObject.optDouble("Emission");
                int duration = jsonObject.optInt("Duration");
                return new Way(labelStart+" vers "+labelEnd, new Address(labelStart, startLat, startLng), new Address(labelEnd, endLat, endLng), carbon, departureDateTime, arrivalDateTime, duration, null);
            }
        }, action, error, "https://sgorilla.goodway.io/trips.php").execute(new Pair("Mail", mail), new Pair("Password", password), new Pair("Id", Integer.toString(id)));
    }

    public static AsyncTask sendRoute(Context c, Action<Integer> action, ErrorAction error, String mail, String password, List<Integer> users, Way way){
        try {
            return new GoodwayHttpsClient<>(c, new ProcessJson<Integer>() {
                @Override
                public Integer processJson(JSONObject jsonObject) {
                    Integer id = jsonObject.optInt("Id");
                    return id;
                }
            }, action, error, "https://sgorilla.goodway.io/send_route.php").execute(new Pair("Mail", mail), new Pair("Password", password),
                    new Pair("LabelStart", way.getFrom().getName(c)), new Pair("LabelEnd", way.getTo().getName(c)),
            new Pair("StartLat", Double.toString(way.getFrom().getLatitude())), new Pair("StartLng", Double.toString(way.getFrom().getLongitude())),
            new Pair("EndLat", Double.toString(way.getTo().getLatitude())), new Pair("EndLng", Double.toString(way.getTo().getLongitude())),new Pair("DepartureTime", way.getDepartureDateTime()),new Pair("ArrivalTime", way.getArrivalDateTime()),
                    new Pair("Duration", Integer.toString(way.getDuration())), new Pair("Emission", Double.toString(way.getCo2Emission())),
            new Pair("Users", new JSONArray(users).toString(1)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static AsyncTask acceptFriend(Context c, Action<Integer> action, ErrorAction error, String mail, String password, int id){
            return new GoodwayHttpsClient<>(c, new ProcessJson<Integer>() {
                @Override
                public Integer processJson(JSONObject jsonObject) {
                    Integer id = jsonObject.optInt("Id");
                    return id;
                }
            }, action, error, "https://sgorilla.goodway.io/accept_friend.php").execute(
                    new Pair("Mail", mail), new Pair("Password", password),
                    new Pair("FriendId", Integer.toString(id)));
    }
    public static AsyncTask requestFriend(Context c, Action<Boolean> action, ErrorAction error, String mail, String password, int id){
            return new GoodwayHttpsClient<>(c, new ProcessJson<Boolean>() {
                @Override
                public Boolean processJson(JSONObject jsonObject) {
                    return true;
                }
            }, action, error, "https://sgorilla.goodway.io/request_friend.php").execute(
                    new Pair("Friend", Integer.toString(id)),
                    new Pair("Mail", mail), new Pair("Password", password));
    }


    public static AsyncTask getUsersFromName(Context c, Action<User> action, ErrorAction error, String mail, String password, String fname, String lname){
        if(fname != null ){
            if(lname!=null){
                return new GoodwayHttpsClient<>(c, new ProcessJson<User>() {
                    @Override
                    public User processJson(JSONObject jsonObject) {
                        Integer id = jsonObject.optInt("Id");
                        String fname = jsonObject.optString("FirstName");
                        String lname = jsonObject.optString("LastName");
                        return new User(id, fname, lname);
                    }
                }, action, error, "https://sgorilla.goodway.io/users.php").execute(
                        new Pair("Name1", fname), new Pair("Name2", lname),
                        new Pair("Mail", mail), new Pair("Password", password));
            }
            else{
                return new GoodwayHttpsClient<>(c, new ProcessJson<User>() {
                    @Override
                    public User processJson(JSONObject jsonObject) {
                        Integer id = jsonObject.optInt("Id");
                        String fname = jsonObject.optString("FirstName");
                        String lname = jsonObject.optString("LastName");
                        return new User(id, fname, lname);
                    }
                }, action, error, "https://sgorilla.goodway.io/users.php").execute(
                        new Pair("Name1", fname),
                        new Pair("Mail", mail), new Pair("Password", password));
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
            keyStore.load(new BufferedInputStream(c.getResources().openRawResource(R.raw.keystore)), c.getString(R.string.store_pass).toCharArray());
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
                dos.close();;
            }
        } catch (IOException e) {
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
        if(length==0 && error!=null){
            error.action();
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