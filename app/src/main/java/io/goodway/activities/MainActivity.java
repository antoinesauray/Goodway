package io.goodway.activities;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import io.goodway.R;
import io.goodway.activities.account.FacebookAccountActivity;
import io.goodway.activities.account.GoodwayAccountActivity;
import io.goodway.activities.gtfs.SubscribeActivity;
import io.goodway.model.User;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.sync.gcm.GCMService;
import io.goodway.sync.gcm.QuickstartPreferences;
import io.goodway.sync.gcm.RegistrationIntentService;
import io.goodway.view.fragment.MainFragment;
import io.goodway.view.fragment.SearchFragment;


/**
 * Created by sauray on 14/03/15.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private Toolbar toolbar;
    public static final int DEPARTURE=1, DESTINATION=2;
    private int nbFriendRequests;

    private String token;
    private User user;
    private FragmentManager fragmentManager;

    private Address departure, destination;

    private static final int ACCESS_FINE_LOCATION=1;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            /*
            Message message = intent.getParcelableExtra("message");
            int channel_id = intent.getIntExtra(Constants.CHANNEL, -1);
            if(message.getSender_id()!= Debug.SENDER_ID){
                Log.d("displaying type", "type="+message.getAttachment_type());
                Log.d("displaying image", "url="+message.getAttachment());
            }
            if(activeChannel==null || (channel_id!=-1 && activeChannel.getId()!=channel_id)){
                adapter.incrementChannelPendingMessages(channel_id);
            }
            */
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        token = getIntent().getExtras().getString("token");
        user = getIntent().getExtras().getParcelable("user");
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setupGCM();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(GCMService.MESSAGE_RECEIVED));

        fragmentManager = getFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(0, R.animator.exit, R.animator.enter_pop, R.animator.exit_pop);
        fragmentTransaction.replace(R.id.fragment, MainFragment.newInstance(getIntent().getExtras()));
        fragmentTransaction.commit();

        GoodwayHttpClientPost.countFriendRequests(this, new Action<Integer>() {
            @Override
            public void action(Integer e) {
                nbFriendRequests = e;
                if (e > 0) {

                }
            }
        }, new ErrorAction() {
            @Override
            public void action(int length) {

            }
        }, token);


    }

    public void changeLocation(View v){
        int request=0;
        switch (v.getId()) {
            case R.id.from:
                request = DEPARTURE;
                break;
            case R.id.to:
                request = DESTINATION;
                break;
        }
        changeLocation(request);
    }
    public void changeLocation(int request){
        Bundle b = new Bundle();
        b.putInt("REQUEST", request);
        b.putString("token", token);
        switchToFragment(SearchFragment.newInstance(b));
    }

    private void switchToFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.enter, R.animator.exit, R.animator.enter_pop, R.animator.exit_pop);
        fragmentTransaction.addToBackStack(fragment.getTag());
        fragmentTransaction.replace(R.id.fragment, fragment);
        fragmentTransaction.commit();
    }
    private void switchToFragmentWithExitAnim(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.enter_pop, R.animator.exit_pop, R.animator.enter_pop, R.animator.exit_pop);
        fragmentTransaction.addToBackStack(fragment.getTag());
        fragmentTransaction.replace(R.id.fragment, fragment);
        fragmentTransaction.commit();
    }

    public void setFrom(Address address) {
        this.departure = address;
        Bundle b= new Bundle();
        b.putParcelable("departure", departure);
        b.putParcelable("destination", destination);
        b.putAll(getIntent().getExtras());
        switchToFragmentWithExitAnim(MainFragment.newInstance(b));
    }

    public void setTo(Address address) {
        this.destination = address;
        Bundle b= new Bundle();
        b.putParcelable("departure", departure);
        b.putParcelable("destination", destination);
        b.putAll(getIntent().getExtras());
        switchToFragmentWithExitAnim(MainFragment.newInstance(b));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                fragmentManager.popBackStack();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        fragmentManager.popBackStack();
    }

    /*
    public void fabClick(View v){
        if((departure==null || destination == null) && userLocation==null){
            if(departure!=null){
                Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.your_location_not_available, Snackbar.LENGTH_LONG)
                        .setAction(R.string.select, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                changeLocation(DEPARTURE);
                            }
                        }).show();
            }
            else{
                Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.no_to, Snackbar.LENGTH_LONG)
                        .setAction(R.string.select, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                changeLocation(DESTINATION);
                            }
                        }).show();
            }
        }
        else {
            if(departure==null){departure=new Address(getString(R.string.your_location), userLocation.getLatitude(), userLocation.getLongitude());}
            if(destination==null){destination=new Address(getString(R.string.your_location), userLocation.getLatitude(), userLocation.getLongitude());;}
            Intent i = new Intent(this, WayActivity.class);
            i.putExtra("user", user);
            i.putExtra("token", token);
            i.putExtra("departure", departure);
            i.putExtra("destination", destination);
            startActivity(i);
        }
    }
*/
    public void onClick(View v){
        switch (v.getId()){
            case R.id.contacts:
                Intent i = new Intent(this, FriendsActivity.class);
                i.putExtra("token", token);
                i.putExtra("user", user);
                i.putExtra("nbFriendRequests", nbFriendRequests);
                startActivity(i);
                break;
            case R.id.places:
                Intent i2 = new Intent(this, AddressActivity.class);
                i2.putExtra("token", token);
                i2.putExtra("user", user);
                i2.putExtra("nbFriendRequests", nbFriendRequests);
                startActivity(i2);
                break;
            case R.id.groups:
                Intent i3 = new Intent(this, UserGroupsActivity.class);
                i3.putExtra("token", token);
                i3.putExtra("user", user);
                startActivity(i3);
                break;
            case R.id.subscriptions:
                Intent i4 = new Intent(this, SubscribeActivity.class);
                i4.putExtra("token", token);
                i4.putExtra("user", user);
                startActivity(i4);
                break;
            case R.id.share:
                String message = "Téléchargez Goodway, l'application de déplacement moderne";
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(share, "Partager"));
                break;
            case R.id.facebook_account:
                Intent i5 = new Intent(this, FacebookAccountActivity.class);
                i5.putExtra("token", token);
                startActivity(i5);
                break;
            case R.id.goodway_account:
                Intent i6 = new Intent(this, GoodwayAccountActivity.class);
                i6.putExtra("token", token);
                i6.putExtra("user", user);
                startActivity(i6);
                break;
            case R.id.add_account:
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_add_account);
                dialog.setTitle(R.string.add_account);
                dialog.show();
                break;
            default:
                setDeparture(null);
                break;
        }
    }

    public void setDeparture(View v){
        switchToFragment(SearchFragment.newInstance(getIntent().getExtras()));
    }

    /**
     * Setup the GCM service to listen for incoming notifications
     */
    private void setupGCM(){
        Log.d(TAG, "setupGCM");
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.d(TAG, "sentToken");
                } else {
                    Log.d(TAG, "Token error");
                }
            }
        };
        // Registering BroadcastReceiver
        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            intent.putExtra("token", token);
            startService(intent);
        }
    }


    @Override
    public void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    /**
     * Register the receiver for incoming GCM notifications
     */
    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


}
