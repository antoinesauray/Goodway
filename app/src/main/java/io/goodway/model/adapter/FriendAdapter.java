package io.goodway.model.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.goodway.ProfileActivity;
import io.goodway.R;
import io.goodway.model.User;
import io.goodway.model.network.GoodwayHttpsClient;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.view.ImageTrans_CircleTransform;


/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    private List<User> mDataset;
    private Activity activity;
    private int viewId;
    private String mail, password;

    private static final String TAG="LINE_ADAPTER";

    // Provide a suitable constructor (depends on the kind of dataset)
    public FriendAdapter(Activity activity, int viewId, String mail, String password) {
        this.activity = activity;
        mDataset = new ArrayList<User>();
        this.viewId = viewId;
        this.mail = mail;
        this.password = password;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FriendAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(viewId, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(activity, v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.setItem(mDataset.get(position));
        User a = mDataset.get(position);
        holder.name.setText(a.getFirstName() + " " + a.getLastName());
        Picasso.with(activity)
                .load(a.getAvatar())
                .error(R.mipmap.ic_person_white_48dp)
                .resize(100, 100)
                .transform(new ImageTrans_CircleTransform())
                .into(holder.avatar);
    }

    public void add(User item) {
        int position = mDataset.size();
        mDataset.add(position, item);
        notifyItemInserted(position);
        //Collections.sort(mDataset);
        notifyItemRangeChanged(0, mDataset.size());
    }

    public void clear(){
        int size = mDataset.size();
        mDataset.clear();
        notifyItemRangeRemoved(0, size);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        TextView name;
        ImageView avatar;
        User item;
        Activity activity;

        public ViewHolder(Activity activity, View lyt_main) {
            super(lyt_main);
            lyt_main.setOnClickListener(this);
            if(viewId==R.layout.view_friend_request){
                lyt_main.findViewById(R.id.accept_friend_request).setOnClickListener(this);
            }
            else if(viewId==R.layout.view_friend_add){
                lyt_main.findViewById(R.id.request_friend).setOnClickListener(this);
            }
            this.activity = activity;
            name = (TextView) lyt_main.findViewById(R.id.name);
            avatar = (ImageView) lyt_main.findViewById(R.id.avatar);
        }

        @Override
        public void onClick(View v) {
            Log.d("click", "id=" + v.getId());
            switch (v.getId()){
                case R.id.accept_friend_request:
                    GoodwayHttpsClient.acceptFriend(activity, new Action<Integer>() {
                        @Override
                        public void action(Integer e) {
                            int pos = getAdapterPosition();
                            mDataset.remove(pos);
                            notifyItemRemoved(pos);
                        }
                    }, null, mail, password, item.getId());
                    break;
                case R.id.request_friend:
                    Log.d("requesting", "requesting friend with id="+item.getId());
                    GoodwayHttpsClient.requestFriend(activity, new Action<Boolean>() {
                        @Override
                        public void action(Boolean e) {
                            clear();
                        }
                    }, new ErrorAction() {
                        @Override
                        public void action(int length) {
                            clear();
                        }
                    }, mail, password, item.getId());
                    break;
                default:
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.putExtra("USER", item);
                    activity.startActivity(intent);
            }

        }

        public void setItem(User item) {
            this.item = item;
        }
    }
}
