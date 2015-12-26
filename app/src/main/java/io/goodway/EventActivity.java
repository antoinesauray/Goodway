package io.goodway;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import io.goodway.model.GroupEvent;
import io.goodway.model.User;
import io.goodway.navitia_android.Address;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class EventActivity extends AppCompatActivity {

    // ----------------------------------- Model
    /**
     * Unique identifier for this activity
     */
    private static final String TAG = "STOP_ACTIVITY";

    // ----------------------------------- UI

    /**
     * Toolbar widget
     */
    private Toolbar toolbar;
    private GroupEvent groupEvent;
    private TextView date;
    private WebView webView;

    private User user;

    private int request;
    public static final int NEWACTIVITY=3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        Bundle extras = this.getIntent().getExtras();
        user = extras.getParcelable("user");
        groupEvent = extras.getParcelable("event");
        request = extras.getInt("request");
        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appBar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(groupEvent.getName());
        webView = (WebView) findViewById(R.id.webView);
        webView.setBackgroundColor(Color.argb(1, 0, 0, 0));
        webView.getSettings().setAllowFileAccess(true);

        Picasso.with(this).load(groupEvent.getUrl())
                    .error(R.mipmap.ic_event_black_36dp).into(new Target() {

                @Override
                public void onPrepareLoad(Drawable arg0) {


                }

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom arg1) {
                    // TODO Create your drawable from bitmap and append where you like.
                    appBarLayout.setBackground(new BitmapDrawable(getResources(), bitmap));

                    // Asynchronous
                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                        public void onGenerated(Palette p) {
                            // Use generated instance
                            int dark = p.getDarkMutedColor(getResources().getColor(R.color.primary));
                            //findViewById(R.id.scrollView).setDrawingCacheBackgroundColor(dark);
                            Window window = getWindow();
                            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                window.setStatusBarColor(dark);
                            }
                        }
                    });

                }

                @Override
                public void onBitmapFailed(Drawable arg0) {


                }
        });

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        date = (TextView) findViewById(R.id.date);
        String[] dateSplit = Address.splitIso8601(groupEvent.getS_time());
        date.setText(dateSplit[2] + " " + GroupEvent.formatMonth(dateSplit[1]) + " " + dateSplit[0] + " "+getString(R.string.at)+" " + dateSplit[3] + "h" + dateSplit[4]);

        webView.getSettings().setJavaScriptEnabled(true);

        final Activity activity = this;
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                activity.setProgress(progress * 1000);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });

        webView.loadUrl(groupEvent.getUrl());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void fabClick(View v){
        if(request==NEWACTIVITY){
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("user", user);
            i.putExtra("destination", new Address(groupEvent.getName(), groupEvent.getLatitude(), groupEvent.getLongitude()));
            startActivity(i);
        }
        else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("GROUPEVENT", groupEvent);
            returnIntent.putExtra("REQUEST", request);
            setResult(Activity.RESULT_OK, returnIntent);
            webView.onPause();
            finish();
        }
    }
}
