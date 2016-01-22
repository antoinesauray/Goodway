package io.goodway.model.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.goodway.R;
import io.goodway.model.User;
import io.goodway.model.callback.UserCallback;
import io.goodway.view.ImageTrans_CircleTransform;


/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> mDataset;
    private UserCallback callback;
    private Context context;

    private static final String TAG="LINE_ADAPTER";

    // Provide a suitable constructor (depends on the kind of dataset)
    public UserAdapter(Context context, UserCallback callback) {
        this.callback = callback;
        mDataset = new ArrayList<User>();
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_user, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.setItem(mDataset.get(position));
        User a = mDataset.get(position);
        holder.name.setText(a.getFirstName()+" "+a.getLastName());
        if(a.getCity()!=null){holder.title.setText(a.getCity());}
        else{holder.title.setText(a.getTitle(context));}
        /*
        Picasso.with(context)
                .load(a.getAvatar())
                .error(R.mipmap.ic_person_black_36dp)
                .resize(100, 100)
                .centerCrop()
                .transform(new ImageTrans_CircleTransform())
                .into(holder.avatar);
                */
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
        TextView name, title;
        ImageView avatar;
        User item;

        public ViewHolder(View lyt_main) {
            super(lyt_main);
            lyt_main.setOnClickListener(this);
            name = (TextView) lyt_main.findViewById(R.id.name);
            title = (TextView) lyt_main.findViewById(R.id.title);
            avatar = (ImageView) lyt_main.findViewById(R.id.avatar);
        }

        @Override
        public void onClick(View v) {
            callback.action(item);
        }

        public void setItem(User item) {
            this.item = item;
        }
    }
}
