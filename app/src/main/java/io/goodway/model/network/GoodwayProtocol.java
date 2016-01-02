package io.goodway.model.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.AbstractMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import io.goodway.R;

/**
 * Created by antoine on 02/01/16.
 */
public class GoodwayProtocol {
    public static HttpURLConnection getHttpPostUrlConnection(String url, AbstractMap.SimpleEntry[] entries) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(15000);
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        OutputStream os = urlConnection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(postQuery(entries));
        writer.flush();
        writer.close();
        os.close();
        return urlConnection;
    }
    public static HttpsURLConnection getHttpsPostUrlConnection(String url, AbstractMap.SimpleEntry[] entries) throws IOException {
        HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(url).openConnection();
        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(15000);
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        OutputStream os = urlConnection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(postQuery(entries));
        writer.flush();
        writer.close();
        os.close();
        return urlConnection;
    }
    public static HttpURLConnection getHttpGetUrlConnection(String url, AbstractMap.SimpleEntry[] entries) throws IOException {
        return (HttpURLConnection) new URL(getQuery(url, entries)).openConnection();
    }
    public static HttpsURLConnection getHttpsGetUrlConnection(String url, AbstractMap.SimpleEntry[] entries) throws IOException {
        return (HttpsURLConnection) new URL(getQuery(url, entries)).openConnection();
    }

    private static String postQuery(AbstractMap.SimpleEntry[] params) throws UnsupportedEncodingException
    {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (AbstractMap.SimpleEntry pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getKey().toString(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue().toString(), "UTF-8"));
        }
        Log.d("post params", result.toString());
        return result.toString();
    }
    private static String getQuery(String url, AbstractMap.SimpleEntry[] params) throws UnsupportedEncodingException
    {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (AbstractMap.SimpleEntry pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode((String) pair.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode((String) pair.getValue(), "UTF-8"));
        }

        return url+result.toString();
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

}
