package io.goodway.activities.widgets;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.goodway.R;
import io.goodway.adapters.UserAdapter;
import io.goodway.model.User;
import io.goodway.model.callback.FinishCallback;
import io.goodway.model.callback.UserCallback;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.model.network.GoodwayProtocol;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.UserLocation;
import io.goodway.view.ImageTrans_CircleTransform;
import io.goodway.view.fragment.MainFragmentHome;
import io.goodway.widgets.FriendVisitWidgetProvider;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class FriendVisitConfigureActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    // ----------------------------------- Model

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private SwipeRefreshLayout swipeLayout;

    private Toolbar toolbar;

    private MainFragmentHome mainFragmentctivity;
    private TextView error;

    private int request;
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    private ArrayList<UserLocation> userLocations;

    private Address departure;
    private User activeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_contact);
        //request = getIntent().getExtras().getInt("REQUEST");
        recyclerView = (RecyclerView) findViewById(R.id.list);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        error = (TextView) findViewById(R.id.error);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new UserAdapter(this, new UserCallback() {
            @Override
            public void action(final User u) {
                final BottomSheet.Builder sheet = new BottomSheet.Builder(FriendVisitConfigureActivity.this).title(getString(R.string.addresses)+ " "+getString(R.string.linked_to)+" "+u.getFirstName()).listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(u, userLocations.get(which-1));
                    }
                });
                final ProgressDialog dialog = new ProgressDialog(FriendVisitConfigureActivity.this);
                dialog.setMessage(getString(R.string.request_places));
                dialog.setProgressStyle(dialog.STYLE_SPINNER);
                dialog.show();
                userLocations = new ArrayList<>();
                GoodwayHttpClientPost.getUserLocations(FriendVisitConfigureActivity.this, new Action<List<UserLocation>>() {
                    @Override
                    public void action(List<UserLocation> locations) {
                        for (UserLocation location : locations) {
                            userLocations.add(location);
                            sheet.sheet(userLocations.size(), location.getName());
                        }
                    }
                }, new ErrorAction() {
                    @Override
                    public void action(int length) {
                        dialog.dismiss();
                        if (length == -1) {
                            Toast.makeText(FriendVisitConfigureActivity.this, R.string.connexion_error, Toast.LENGTH_SHORT).show();
                        } else {
                            sheet.show();
                        }
                    }
                }, new FinishCallback() {
                    @Override
                    public void action(int length) {
                        dialog.dismiss();
                        sheet.show();
                    }
                }, activeUser.getToken(), u.getId());
            }
        });

        recyclerView.setAdapter(adapter);

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(R.color.accent);
        swipeLayout.setRefreshing(true);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        SharedPreferences shared_preferences = getSharedPreferences(getString(R.string.goodway_preferences),
                MODE_PRIVATE);
        String mail = shared_preferences.getString("mail", null);
        String password = shared_preferences.getString("password", null);
        int id = shared_preferences.getInt("id", -1);
        String fname = shared_preferences.getString("fname", null);
        String lname = shared_preferences.getString("lname", null);
        int title = shared_preferences.getInt("title", 2);

        if (mail != null && password != null) {

            if (GoodwayProtocol.isConnected(this)) {
                GoodwayHttpClientPost.authenticate(this, new Action<User>() {
                    @Override
                    public void action(User user) {
                        if(user!=null) {
                            start(user);
                        }
                        else{
                            new AlertDialog.Builder(FriendVisitConfigureActivity.this)
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
            }
            else {
                new AlertDialog.Builder(this)
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
            new AlertDialog.Builder(this)
                    .setTitle(R.string.connexion_error)
                    .setMessage("Vous n'êtes pas authentifié, veuillez lancer l'application Goodway et vous connecter avant d'ajouter un Widget")
                    .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .show();
        }
    }

    private void start(final User user){
        //departure = getIntent().getExtras().getParcelable("departure");
        Log.d("USER AUTHENTICATED", "SUCCESS");
        this.activeUser = user;
        GoodwayHttpClientPost.getFriends(this, new Action<List<User>>() {
            @Override
            public void action(List<User> e) {
                swipeLayout.setRefreshing(false);
                for (User u : e) {
                    Log.d("new user", u.toString());
                    adapter.add(u);
                }
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {
                switch (length) {
                    case 0:
                        swipeLayout.setRefreshing(false);
                        break;
                    case -1:
                        swipeLayout.setRefreshing(false);
                        error.setText(R.string.connexion_error);
                        error.setVisibility(View.VISIBLE);
                        break;
                }

            }
        }, user.getToken());
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

    @Override
    public void onRefresh(){
        swipeLayout.setRefreshing(true);
        adapter.clear();
        error.setVisibility(View.INVISIBLE);
        GoodwayHttpClientPost.getFriends(this, new Action<List<User>>() {
            @Override
            public void action(List<User> e) {
                for(User u : e) {
                    swipeLayout.setRefreshing(false);
                    Log.d("USER FOUND", e.toString());
                    adapter.add(u);
                }

            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {
                switch (length) {
                    case 0:
                        swipeLayout.setRefreshing(false);
                        error.setText(R.string.no_friends);
                        error.setVisibility(View.VISIBLE);
                        break;
                    case -1:
                        swipeLayout.setRefreshing(false);
                        error.setText(R.string.connexion_error);
                        error.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }, activeUser.getToken());
    }

    private void finish(User u, Address address){
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            int mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            Log.d("WIDGET_ID", "id="+mAppWidgetId);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            RemoteViews views = new RemoteViews(getPackageName(),
                    R.layout.friendvisit_appwidget);
            views.setTextViewText(R.id.name, u.getFirstName());
            views.setTextViewText(R.id.location, address.getName());



            Intent receiverIntent = new Intent(this, FriendVisitWidgetProvider.class);
            receiverIntent.setAction(FriendVisitWidgetProvider.ACTION_WIDGET_RECEIVER);
            receiverIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{mAppWidgetId});
            Log.d("address before intent", address.toString());
            receiverIntent.putExtra("address", address);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                    0, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);


            Picasso.with(this)
                    .load(u.getAvatar())
                    .resize(100,100)
                    .centerInside()
                    .transform(new ImageTrans_CircleTransform())
                    .into(views, R.id.avatar, new int[] {mAppWidgetId});
            appWidgetManager.updateAppWidget(mAppWidgetId, views);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat("latitude_"+mAppWidgetId, (float) address.getLatitude());
            editor.putFloat("latitude_"+mAppWidgetId, (float) address.getLatitude());
            editor.putString("user_name_"+mAppWidgetId, u.getFirstName());
            editor.putString("location_name_"+mAppWidgetId, address.getName());
            editor.commit();

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    }

}
