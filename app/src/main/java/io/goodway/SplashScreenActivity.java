package io.goodway;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.goodway.model.GroupEvent;
import io.goodway.model.User;
import io.goodway.model.network.GoodwayHttpClientGet;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.model.network.GoodwayProtocol;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.ErrorAction;


/**
 * The main activity of the program
 * @author Antoine Sauray
 * @version 2.0
 */
public class SplashScreenActivity extends AppCompatActivity {


    private AsyncTask client;

    private int state;
    private static final int CHOOSE = 1, LOGIN = 2, REGISTER = 3;

    private FrameLayout fragmentLayout;
    private Fragment splash, register, current;
    private LoginFragment login;
    private FragmentManager fragmentManager;


    // ----------------------------------- Constants
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        fragmentLayout = (FrameLayout) findViewById(R.id.fragment);

        splash = new SplashFragment();
        login = new LoginFragment();
        register = new RegisterFragment();

        FacebookSdk.sdkInitialize(getApplicationContext());
        fragmentManager = getSupportFragmentManager();
        splash();
        showHashKey(this);
    }

    public static void showHashKey(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    "io.goodway", PackageManager.GET_SIGNATURES); //Your package name here
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.v("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    void splash() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        fragmentTransaction.replace(R.id.fragment, splash);
        fragmentTransaction.addToBackStack("splash");
        fragmentTransaction.commit();
        current = splash;
    }
    void login() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        fragmentTransaction.replace(R.id.fragment, login);
        fragmentTransaction.addToBackStack("login");
        fragmentTransaction.commit();
        current = login;
    }
    void register() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        fragmentTransaction.replace(R.id.fragment, register);
        fragmentTransaction.addToBackStack("register");
        fragmentTransaction.commit();
        current = register;
    }

    public static void closeKeyboard(Activity context, View editText){
        if(editText!=null) {
            editText.requestFocus();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void showKeyboard(Activity context, View editText){
        editText.requestFocus();
        InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    public void login(View v){login();}

    public void register(View v){register();}

    public static class SplashFragment extends Fragment{
        View root;
        ImageView logo;
        TextView connectedAs;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.fragment_splashscreen, container, false);
            SharedPreferences shared_preferences = getActivity().getSharedPreferences(getString(R.string.goodway_preferences),
                    MODE_PRIVATE);

            connectedAs = (TextView) root.findViewById(R.id.connectedAs);

            String mail = shared_preferences.getString("mail", null);
            String password = shared_preferences.getString("password", null);
            int id = shared_preferences.getInt("id", -1);
            String fname = shared_preferences.getString("fname", null);
            String lname = shared_preferences.getString("lname", null);
            int title = shared_preferences.getInt("title", 2);

            logo = (ImageView) root.findViewById(R.id.logo);
            if (mail != null && password != null) {

                if (GoodwayProtocol.isConnected(getActivity())) {
                    GoodwayHttpClientPost.authenticate(getActivity(), new Action<User>() {
                        @Override
                        public void action(User user) {
                            if(user!=null) {
                                start(user);
                            }
                            else{
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(R.string.connexion_error)
                                        .setMessage("Impossible de se connecter à internet. Goodway ne pourra vous proposer aucun contenu en mode hors-ligne")
                                        .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // continue with delete
                                            }
                                        })
                                        .show();
                            }

                        }
                    }, null, mail, password);
                } else {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.connexion_error)
                            .setMessage("Impossible de se connecter à internet. Goodway ne pourra vous proposer aucun contenu en mode hors-ligne")
                            .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .show();
                }

            } else {
                root.findViewById(R.id.not_connected).setVisibility(View.VISIBLE);
            }
            return root;
        }
        private void start(final User user){
            ObjectAnimator alpha = ObjectAnimator.ofFloat(connectedAs, "alpha", 0f, 1f);
            alpha.setStartDelay(400);
            alpha.setDuration(600);
            alpha.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    Intent i = new Intent(getActivity(), MainActivity.class);
                    i.putExtra("user", user);
                    i.putExtra("token", user.getToken());
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
    }
    public static class LoginFragment extends Fragment implements View.OnClickListener {
        View root;
        EditText mail, password;
        TextView login;
        ProgressBar progressBar;
        LoginButton loginButton;
        CallbackManager callbackManager;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.fragment_login, container, false);
            mail = (EditText) root.findViewById(R.id.email);
            password = (EditText) root.findViewById(R.id.password);
            login = (TextView) root.findViewById(R.id.login);
            progressBar = (ProgressBar) root.findViewById(R.id.progressBar);
            login.setOnClickListener(this);
            mail.requestFocus();

            loginButton = (LoginButton) root.findViewById(R.id.login_button);
            loginButton.setReadPermissions("user_friends");
            // If using in a fragment
            loginButton.setFragment(this);
            // Other app specific specialization
            callbackManager = CallbackManager.Factory.create();
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

                @Override
                public void onSuccess(LoginResult loginResult) {

                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onError(FacebookException error) {

                }
            });
            loginButton.registerCallback(callbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            //handlePendingAction();
                            GraphRequest request = GraphRequest.newMeRequest(
                                    loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(JSONObject object, GraphResponse response) {
                                            Log.v("LoginActivity", response.toString());
                                        }
                                    });
                            request.executeAsync();
                        }

                        @Override
                        public void onCancel() {
                            Log.d("LoginActivity", "error");
                        }

                        @Override
                        public void onError(FacebookException exception) {

                        }


                    });
            ProfileTracker profileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(
                        Profile oldProfile,
                        Profile currentProfile) {
                    //GoodwayHttpClientPost.currentProfile.getProfilePictureUri(200, 200)
                    // Envoyer la nouvelle photo de profil
                    // App code
                }
            };

            return root;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        public void onResume(){
            super.onResume();
            SplashScreenActivity.showKeyboard(getActivity(), mail);
        }

        @Override
        public void onClick(View v) {
            closeKeyboard(getActivity(), root.findFocus());
            if(GoodwayProtocol.isConnected(getActivity())) {
                progressBar.setVisibility(View.VISIBLE);
                GoodwayHttpClientPost.authenticate(getActivity(), new Action<User>() {
                    @Override
                    public void action(User user) {
                        if(user!=null) {
                            SharedPreferences shared_preferences = getActivity().getSharedPreferences(getString(R.string.goodway_preferences),
                                    MODE_PRIVATE);
                            SharedPreferences.Editor editor = shared_preferences.edit();
                            editor.putString("mail", mail.getText().toString());
                            editor.putString("password", password.getText().toString());
                            editor.commit();
                            Intent i = new Intent(getActivity(), MainActivity.class);
                            i.putExtra("user", user);
                            i.putExtra("token", user.getToken());
                            startActivity(i);
                        }
                        else{
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getActivity(), getString(R.string.wrong_id), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, null, mail.getText().toString(), password.getText().toString());
            }
            else{
                Toast.makeText(getActivity(), getString(R.string.connexion_error), Toast.LENGTH_SHORT).show();
            }
        }
    }
    public static class RegisterFragment extends Fragment implements View.OnClickListener {
        View root;
        EditText fname, lname, mail, password, confirm;
        TextView register, birthday, mailAvailibility;
        Calendar date;
        boolean mailAvailable;
        AsyncTask client;
        static final Pattern VALID_EMAIL_ADDRESS_REGEX =
                Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.fragment_register, container, false);
            fname = (EditText) root.findViewById(R.id.fname);
            lname = (EditText) root.findViewById(R.id.lname);
            mail = (EditText) root.findViewById(R.id.email);
            password = (EditText) root.findViewById(R.id.password);
            confirm = (EditText) root.findViewById(R.id.passwordConfirm);
            birthday = (TextView) root.findViewById(R.id.birthday);
            register = (TextView) root.findViewById(R.id.register);
            mailAvailibility = (TextView) root.findViewById(R.id.mailAvailibility);

            fname.setNextFocusDownId(R.id.lname);
            lname.setNextFocusDownId(R.id.email);
            birthday.setOnClickListener(this);
            register.setOnClickListener(this);

            mail.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(validate(s.toString())) {
                        if (client != null) {
                            client.cancel(true);
                        }
                        client = GoodwayHttpClientGet.checkMailAvailability(getActivity(), new Action<Boolean>() {
                            @Override
                            public void action(Boolean e) {
                                mailAvailable = false;
                                mailAvailibility.setText(R.string.notAvailable);
                                mailAvailibility.setTextColor(getResources().getColor(R.color.red));
                            }
                        }, new ErrorAction() {
                            @Override
                            public void action(int length) {
                                mailAvailable = true;
                                mailAvailibility.setText(R.string.available);
                                mailAvailibility.setTextColor(getResources().getColor(R.color.green));
                            }
                        }, mail.getText().toString());
                    }
                    else{
                        mailAvailibility.setText(R.string.not_valid);
                        mailAvailibility.setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_dark));
                        mailAvailable = false;
                    }
                }
            });
            return root;
        }
        public void onResume(){
            super.onResume();
            SplashScreenActivity.showKeyboard(getActivity(), fname);
        }
        static boolean validate(String emailStr) {
            Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
            return matcher.find();
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.birthday:
                    new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            date = Calendar.getInstance();
                            date.set(year, monthOfYear, dayOfMonth);
                            birthday.setText(dayOfMonth+" "+ GroupEvent.formatMonth(Integer.toString(monthOfYear + 1))+" "+year);
                        }
                    }, 2000, 1, 1).show();
                    break;
                case R.id.register:
                    if(!password.getText().toString().equals(confirm.getText().toString())) {
                        Toast.makeText(getActivity(), getString(R.string.passwordsDiffer), Toast.LENGTH_SHORT).show();
                    }
                    else if(mail.getText().toString()==""){
                        Toast.makeText(getActivity(), R.string.enter_mail, Toast.LENGTH_SHORT).show();
                    }
                    else if(password.getText().toString()==""){
                        Toast.makeText(getActivity(), R.string.enter_password, Toast.LENGTH_SHORT).show();
                    }
                    else if(fname.getText().toString()==""){
                        Toast.makeText(getActivity(), R.string.enter_fname, Toast.LENGTH_SHORT).show();
                    }
                    else if(lname.getText().toString()==""){
                        Toast.makeText(getActivity(), R.string.enter_lname, Toast.LENGTH_SHORT).show();
                    }
                    else if(date==null){
                        Toast.makeText(getActivity(), R.string.enter_bday, Toast.LENGTH_SHORT).show();
                    }
                    else if(!mailAvailable){
                        Toast.makeText(getActivity(), "Mail "+getString(R.string.unavailable), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        final ProgressDialog dialog = new ProgressDialog(getActivity());
                        dialog.setMessage(getString(R.string.updating_share_options));
                        dialog.setProgressStyle(dialog.STYLE_SPINNER);
                        dialog.show();
                        GoodwayHttpClientPost.register(getActivity(), new Action<User>() {
                            @Override
                            public void action(User u) {
                                SharedPreferences shared_preferences = getActivity().getSharedPreferences(getString(R.string.goodway_preferences),
                                        MODE_PRIVATE);
                                SharedPreferences.Editor editor = shared_preferences.edit();
                                editor.putString("mail", mail.getText().toString());
                                editor.putString("password", password.getText().toString());
                                editor.commit();
                                Intent i = new Intent(getActivity(), MainActivity.class);
                                i.putExtra("user", u);
                                dialog.dismiss();
                                startActivity(i);
                            }
                        }, new ErrorAction() {
                            @Override
                            public void action(int length) {
                                dialog.dismiss();
                                Toast.makeText(getActivity(), R.string.failure, Toast.LENGTH_SHORT).show();
                            }
                        }, mail.getText().toString(), password.getText().toString(), fname.getText().toString(), lname.getText().toString(), new SimpleDateFormat("yyyy-MM-dd").format(date.getTime()));
                        break;
                    }
            }
            /*
            GoodwayHttpClientPost.
            SharedPreferences shared_preferences = getActivity().getSharedPreferences("shared_preferences_test",
                    MODE_PRIVATE);
            SharedPreferences.Editor editor = shared_preferences.edit();
            editor.putString("mail", mail.getText().toString());
            editor.putString("password", password.getText().toString());
            editor.putInt("id", e.getId());
            editor.putString("firstname", e.getFirstName());
            editor.putString("lastname", e.getLastName());
            editor.commit();
            Intent i = new Intent(getActivity(), MainActivity.class);
            i.putExtra("USER", e);
            startActivity(i);
            */
        }
    }

}
