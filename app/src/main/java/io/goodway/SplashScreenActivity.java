package io.goodway;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.goodway.model.User;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.ErrorAction;


/**
 * The main activity of the program
 * @author Antoine Sauray
 * @version 2.0
 */
public class SplashScreenActivity extends AppCompatActivity {

    private ImageView logo;
    private LinearLayout register;
    private EditText mail, fname, lname;
    private EditText loginMail, loginPassword;
    private Button registerNext;
    private ProgressBar progress;

    private TextView connectedAs;

    private LinearLayout not_connected;
    private LinearLayout loginLayout;

    private AsyncTask client;

    private int state;
    private static final int CHOOSE=1, LOGIN=2, REGISTER=3;


    // ----------------------------------- Constants
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        mail = (EditText) findViewById(R.id.mail);
        progress = (ProgressBar) findViewById(R.id.progress);
        fname = (EditText) findViewById(R.id.fname);
        lname = (EditText) findViewById(R.id.lname);
        registerNext = (Button) findViewById(R.id.registerNext);

        loginMail = (EditText) findViewById(R.id.mail_login);
        loginPassword = (EditText) findViewById(R.id.loginPassword);

        not_connected = (LinearLayout) findViewById(R.id.not_connected);
        loginLayout = (LinearLayout) findViewById(R.id.loginLayout);

        connectedAs = (TextView) findViewById(R.id.connectedAs);

        mail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                progress.setVisibility(View.VISIBLE);
                if(client!=null){client.cancel(true);}
                client = GoodwayHttpsClient.checkMailAvailability(SplashScreenActivity.this, new Action<Integer>() {
                    @Override
                    public void action(Integer e) {
                        Log.d("integer : " + e, "action");
                        progress.setVisibility(View.INVISIBLE);
                        mail.setTextColor(getResources().getColor(R.color.red));
                        registerNext.setEnabled(false);

                    }
                }, new ErrorAction() {
                    @Override
                    public void action() {
                        progress.setVisibility(View.INVISIBLE);
                        mail.setTextColor(getResources().getColor(R.color.green));
                        registerNext.setEnabled(true);
                    }
                }, v.getText().toString());
                return false;
            }
        });
        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                MODE_PRIVATE);
        String mail = shared_preferences.getString("mail", null);
        String password = shared_preferences.getString("password", null);
        int id = shared_preferences.getInt("id", -1);
        String fname = shared_preferences.getString("firstname", null);
        String lname = shared_preferences.getString("lastname", null);

        logo = (ImageView) findViewById(R.id.logo);
        register = (LinearLayout) findViewById(R.id.register);
        if (mail != null && password != null && id != -1 && fname!=null && lname != null) {
            state=0;
            if(GoodwayHttpsClient.isConnected(this)){
                GoodwayHttpsClient.authenticate(this, new Action<User>() {
                    @Override
                    public void action(User e) {
                        start(e);
                        connectedAs.setText(e.getMail());
                    }
                }, new ErrorAction() {
                    @Override
                    public void action() {
                        not_connected.setVisibility(View.VISIBLE);
                        state=CHOOSE;
                    }
                }, mail, password);
            }
            else{
                start(new User(id, fname, lname, mail));
                connectedAs.setText(mail);
            }


        } else {
            not_connected.setVisibility(View.VISIBLE);
            state=CHOOSE;
        }

    }

    private void animate(final View view, float from, float to, int colorAnimId, final boolean visible){
        ObjectAnimator translationTop = ObjectAnimator.ofFloat(logo, "translationY",from, to);

        ObjectAnimator colorAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(this, colorAnimId);
        colorAnim.setTarget(findViewById(R.id.mainLayout));

        translationTop.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if(visible){
                    not_connected.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                if(!visible){
                    not_connected.setVisibility(View.VISIBLE);
                }
            }@Override
            public void onAnimationCancel(Animator animation) {}@Override
            public void onAnimationRepeat(Animator animation) {}
        });
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", from, to);
        alpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if(visible){
                    view.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                if(!visible){
                    view.setVisibility(View.INVISIBLE);}
                }
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        alpha.setDuration(300);
        alpha.start();
        translationTop.setDuration(800);
        translationTop.start();
        colorAnim.start();
    }
    private void login(){
        loginLayout.setVisibility(View.VISIBLE);
    }
    private void start(final User u){
        ObjectAnimator alpha = ObjectAnimator.ofFloat(connectedAs, "alpha", 0f, 1f);
        alpha.setStartDelay(400);
        alpha.setDuration(600);
        alpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
                i.putExtra("USER", u);
                startActivity(i);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        alpha.start();
    }

    public void buttonClick(View v){
        not_connected.setVisibility(View.INVISIBLE);
        switch(v.getId()){
            case R.id.loginButton:
                state=LOGIN;
                login();
                animate(loginLayout, 0, 600, R.animator.blue_to_white, true);
                break;
            case R.id.registerButton:
                state=REGISTER;
                animate(register, 0, 600, R.animator.blue_to_white, true);
                break;
        }
    }

    public void nextBack(View v) {
        switch (v.getId()){
            case R.id.registerBack:
                animate(register, 600, 0, R.animator.white_to_blue, false);
                break;
            case R.id.registerNext:


                break;
            case R.id.loginBack:
                animate(loginLayout, 600, 0, R.animator.white_to_blue, false);
                break;
            case R.id.loginNext:
                GoodwayHttpsClient.authenticate(this, new Action<User>() {
                    @Override
                    public void action(User e) {
                        SharedPreferences shared_preferences = getSharedPreferences("shared_preferences_test",
                                MODE_PRIVATE);
                        SharedPreferences.Editor editor = shared_preferences.edit();
                        editor.putString("mail", loginMail.getText().toString());
                        editor.putString("password", loginPassword.getText().toString());
                        editor.putInt("id", e.getId());
                        editor.putString("firstname", e.getFirstName());
                        editor.putString("lastname", e.getLastName());
                        editor.commit();
                        Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
                        i.putExtra("USER", e);
                        startActivity(i);
                    }
                }, new ErrorAction() {
                    @Override
                    public void action() {
                        Snackbar.make(loginLayout, R.string.connexion_error, Snackbar.LENGTH_LONG)
                                .setAction(R.string.change, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        loginMail.requestFocus();
                                    }
                                }).show();
                    }
                }, loginMail.getText().toString(), loginPassword.getText().toString());
                break;
        }
    }

    @Override
    public void onBackPressed(){
        switch (state){
            case 0:
                super.onBackPressed();
                break;
            case CHOOSE:
                super.onBackPressed();
                break;
            case LOGIN:
                animate(loginLayout, 600, 0, R.animator.white_to_blue, false);
                break;
            case REGISTER:
                animate(register, 600, 0, R.animator.white_to_blue, false);
                break;
            default:
                super.onBackPressed();
                break;
        }
    }

}
