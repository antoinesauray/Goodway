package io.goodway.view.fragment.nested;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.goodway.AddressActivity;
import io.goodway.R;
import io.goodway.model.User;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.UserLocation;

/**
 * Created by antoine on 18/01/16.
 */
public class NewAddressFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, TextWatcher {
    public static final String TAG = "newaddress";
    String token;
    EditText name;
    CheckBox shared;
    Button set;
    boolean self;
    User user;
    Toolbar toolbar;
    UserLocation location;

    public static NewAddressFragment newInstance(Bundle args) {
        NewAddressFragment fragment = new NewAddressFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.fragment_new_address, container, false);
        Bundle extras = getArguments();
        token = extras.getString("token");
        self = extras.getBoolean("self");
        user = extras.getParcelable("user");

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        name = (EditText) rootView.findViewById(R.id.name);
        shared = (CheckBox) rootView.findViewById(R.id.shared);
        set = (Button) rootView.findViewById(R.id.set);
        toolbar.setTitle("");

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
        actionBar.setHomeButtonEnabled(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.user_location, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_delete:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_delete)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final ProgressDialog pd = new ProgressDialog(getActivity());
                                pd.setMessage(getString(R.string.deleting));
                                pd.setProgressStyle(pd.STYLE_SPINNER);
                                pd.show();

                                GoodwayHttpClientPost.deleteLocation(getActivity(), new Action<Boolean>() {
                                    @Override
                                    public void action(Boolean e) {
                                        pd.dismiss();
                                        getActivity().getFragmentManager().popBackStack();
                                    }
                                }, new ErrorAction() {
                                    @Override
                                    public void action(int length) {
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), R.string.failure, Toast.LENGTH_SHORT).show();
                                    }
                                }, token, location);

                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
                return true;
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
        }
        return false;
    }

    @Override
    public void onResume(){
        super.onResume();
        Bundle extras = getArguments();
        UserLocation tmp = extras.getParcelable("location");

        if(tmp!=null){
            location = new UserLocation(tmp);
            name.setText(location.getName());
            shared.setChecked(location.shared());
            setShared(location.shared());
            setName(location.getName());
            setLocation(location);
            set.setText(R.string.change_address);
            shared.setOnCheckedChangeListener(this);
            name.addTextChangedListener(this);
        }
        else if(location!=null){
            name.setText(location.getName());
            shared.setChecked(location.shared());
            setShared(location.shared());
            setName(location.getName());
            setLocation(location);
            set.setText(R.string.change_address);
            shared.setOnCheckedChangeListener(this);
            name.addTextChangedListener(this);
        }
        else{
            shared.setOnCheckedChangeListener(null);
            name.removeTextChangedListener(this);
            name.setText("");
            shared.setChecked(true);
            setShared(true);
            setName("");
            set.setText(R.string.add_address);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        location.setShared(isChecked);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        location.setName(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public void setShared(boolean shared){
        ((AddressActivity)getActivity()).setShared(shared);
    }

    public void setName(String name){
        ((AddressActivity) getActivity()).setName(name);
    }

    public void setLocation(UserLocation location){
        ((AddressActivity)getActivity()).setLocation(location);
    }

    public CheckBox getShared(){return shared;}

    public TextView getName(){return name;}

    public UserLocation getLocation(){return location;}

}