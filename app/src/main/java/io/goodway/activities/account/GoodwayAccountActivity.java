package io.goodway.activities.account;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;

import io.goodway.R;
import io.goodway.model.User;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class GoodwayAccountActivity extends AppCompatActivity{

    // ----------------------------------- Model

    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goodway);

        String token = getIntent().getExtras().getString("token");
        User user = getIntent().getExtras().getParcelable("user");
        ((TextView)findViewById(R.id.name)).setText(user.getName());

        ((TextView)findViewById(R.id.mail)).setText(user.getMail());
        ((TextView)findViewById(R.id.phone)).setText(R.string.not_set);

        Window window = getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(R.color.facebook));
        }
    }

}
