package io.goodway.model.network;

/**
 * Created by antoine on 24/12/15.
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import io.goodway.R;
import io.goodway.view.ImageTrans_CircleTransform;

/**
 * Created by sauray on 23/03/15.
 */
public class UploadDocument extends AsyncTask<Void, String, Integer>{

    private Bitmap bitmap;
    private Context c;
    private String filename, mail, pass;
    private ImageView imageView;

    public UploadDocument(Context c, Bitmap bitmap, ImageView imageView, String filename, String mail, String pass){
        this.bitmap = bitmap;
        this.c = c;
        this.filename = filename;
        this.mail = mail;
        this.pass = pass;
        this.imageView = imageView;
    }

    public static String encodeTobase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.NO_WRAP);

        Log.e("LOOK", imageEncoded);
        return imageEncoded;
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Integer ret = -1;

        if(mail != null && pass != null) {
            try {

                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                int maxBufferSize = 1 * 1024 * 1024;


                HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://www.goodway.io/upload_img.php").openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setUseCaches(false); // Don't use a Cached Copy
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Connection", "Keep-Alive");
                urlConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
                urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);


                dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);


//Adding Parameter name

                dos.writeBytes("Content-Disposition: form-data; name=\"mail\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(mail); // mobile_no is String variable
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"pass\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(pass); // mobile_no is String variable
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                //Adding Parameter filepath


//Adding Parameter media file(audio,video and image)

                dos.writeBytes("Content-Disposition: form-data; name=\"picture\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(filename); // mobile_no is String variable
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);


                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                byte[] bitmapdata = bos.toByteArray();

                dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""+ filename + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.write(bitmapdata);
                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                int serverResponseCode = urlConnection.getResponseCode();
                String serverResponseMessage = urlConnection.getResponseMessage();

                Log.d(serverResponseCode+"", "Response code");
                Log.d(serverResponseMessage, "Response message");

                //urlConnection.connect();

                String jsonResult;
                if (serverResponseCode == 201 || serverResponseCode == 200) {
                    Log.d(urlConnection.getResponseCode() + "", "response code");
                    InputStream response = urlConnection.getInputStream();
                    jsonResult = convertStreamToString(response);
                    Log.d("response:", jsonResult.toString());
                    try {
                        JSONObject jsonObject= new JSONObject(jsonResult.toString());
                        publishProgress(jsonObject.optString("avatar"));
                        ret = 1;
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        Log.d("error", "json exception");
                        e.printStackTrace();
                    }
                    dos.flush();
                    dos.close();
                }

            } catch (MalformedURLException e) {
                Log.d(" error"+e.getMessage(), "MalformedURLException");
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                Log.d(" error"+e.getMessage(), "UnsupportedEncodingException");
                e.printStackTrace();
            } catch (ProtocolException e) {
                Log.d(" error"+e.getMessage(), "ProtocolException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(" error"+e.getMessage(), "IOException");
                e.printStackTrace();
            }
        }
        else{

        }

        return ret;
    }

    @Override
    public void onProgressUpdate(String...avatar){
        Log.d("avatar"+avatar[0], avatar[0]);
        Picasso.with(c)
                .load(avatar[0])
                .error(R.mipmap.ic_person_white_48dp)
                .resize(200, 200)
                .transform(new ImageTrans_CircleTransform())
                .into(imageView);
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

    @Override
    protected void onPostExecute(Integer result){

        if(result!=1){
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setTitle(R.string.failure);
            builder.setMessage(R.string.could_not_send);
            builder.setNeutralButton(android.R.string.ok, null);
            builder.show();
        }
    }
}

