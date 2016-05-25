package io.goodway.activities.gtfs;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import io.goodway.R;
import io.goodway.activities.fragment.gtfs.RouteFragment;
import io.goodway.activities.fragment.gtfs.SchemaFragment;
import io.goodway.activities.fragment.gtfs.ServicesFragment;
import io.goodway.activities.fragment.gtfs.StopFragment;
import io.goodway.model.gtfs.Route;
import io.goodway.model.gtfs.Schema;
import io.goodway.model.gtfs.Service;
import io.goodway.model.gtfs.Stop;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.sync.gcm.gtfs.HttpRequest;
import io.goodway.utils.RouteCallback;
import io.goodway.utils.SchemaCallback;
import io.goodway.utils.ServiceCallback;
import io.goodway.utils.StopCallback;

/**
 * Created by Antoine Sauray on 5/25/2016.
 */
public class SubscribeActivity extends AppCompatActivity implements SchemaCallback, RouteCallback, ServiceCallback, StopCallback {

    private FragmentManager fragmentManager;

    private String token;
    private Schema schema;
    private Route route;
    private Service service;
    private Stop stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

        token = getIntent().getExtras().getString("token");

        fragmentManager = getSupportFragmentManager();

        SchemaFragment schemaFragment = SchemaFragment.newInstance(getIntent().getExtras());

        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        fragmentTransaction.addToBackStack(schemaFragment.getTag());
        fragmentTransaction.replace(R.id.fragment, schemaFragment);
        fragmentTransaction.commit();

    }

    @Override
    public void callback(Schema schema) {
        this.schema = schema;
        Bundle b = new Bundle();
        b.putParcelable("schema", schema);
        RouteFragment routeFragment = RouteFragment.newInstance(b);
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        fragmentTransaction.addToBackStack(routeFragment.getTag());
        fragmentTransaction.replace(R.id.fragment, routeFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void callback(Route route) {
        this.route = route;
        Bundle b = new Bundle();
        b.putParcelable("schema", schema);
        b.putParcelable("route", route);
        ServicesFragment routeFragment = ServicesFragment.newInstance(b);
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        fragmentTransaction.addToBackStack(routeFragment.getTag());
        fragmentTransaction.replace(R.id.fragment, routeFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void callback(Service service) {
        this.service = service;
        Bundle b = new Bundle();
        b.putParcelable("schema", schema);
        b.putParcelable("service", service);
        StopFragment routeFragment = StopFragment.newInstance(b);
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        fragmentTransaction.addToBackStack(routeFragment.getTag());
        fragmentTransaction.replace(R.id.fragment, routeFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void callback(Stop t) {
        this.stop = stop;
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Enregistrement");
        pd.setMessage("Nous sommes en train de vos ajouter à notre liste d'envoi, veuillez patienter");
        pd.setIndeterminate(true);
        pd.show();
        HttpRequest.subscribe(new HttpRequest.Action<Void>() {
            @Override
            public void success(Void aVoid) {
                pd.hide();
            }

            @Override
            public void error() {
                pd.hide();
                Toast.makeText(SubscribeActivity.this, "Echec de l'ajout à la liste d'envoi", Toast.LENGTH_SHORT).show();
            }
        }, token, schema, route, stop);
    }
}
