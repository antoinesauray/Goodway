package io.goodway.view.fragment.nested;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import io.goodway.ProfileActivity;
import io.goodway.R;
import io.goodway.model.User;
import io.goodway.view.ImageTrans_CircleTransform;


/**
 * Created by sauray on 14/03/15.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {

    private View root;

    public static final String TAG="MAIN_FRAGMENT";

    public static ProfileFragment newInstance(Bundle args) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_main_profile, container, false);
        User user = getArguments().getParcelable("user");
        Picasso.with(getActivity())
                .load(user.getAvatar())
                .error(R.drawable.ic_account_circle_black_36dp)
                .resize(150, 150)
                .centerCrop()
                .transform(new ImageTrans_CircleTransform())
                .into((ImageView) root.findViewById(R.id.avatar));

        ((TextView)root.findViewById(R.id.name)).setText(user.getName());

        root.findViewById(R.id.profile).setOnClickListener(this);
        return root;
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(getActivity(), ProfileActivity.class);
        i.putExtra("user", getArguments().getParcelable("user"));
        i.putExtra("token", getArguments().getString("token"));
        i.putExtra("self", true);
        i.putExtra("preview", true);
        startActivity(i);
    }
}
