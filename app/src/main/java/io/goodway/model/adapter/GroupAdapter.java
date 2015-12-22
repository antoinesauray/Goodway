package io.goodway.model.adapter;

import android.content.Context;
import android.graphics.Outline;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.goodway.R;
import io.goodway.model.Event;
import io.goodway.model.Group;
import io.goodway.model.callback.AddressSelected;
import io.goodway.model.callback.GroupCallback;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.UserLocation;
import io.goodway.view.ImageTrans_CircleTransform;


/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.AddressViewHolder> {

    private List<Group> mDataset;
    private GroupCallback callback;

    private static final String TAG="LINE_ADAPTER";
    private Context context;
    // Provide a suitable constructor (depends on the kind of dataset)
    public GroupAdapter(Context context, GroupCallback callback) {
        mDataset = new ArrayList<Group>();
        this.callback = callback;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AddressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_group, parent, false);
        AddressViewHolder vh = new AddressViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(AddressViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Group g = mDataset.get(position);
        holder.setItem(mDataset.get(position));
        holder.name.setText(g.getName());
        holder.description.setText(g.getDescription());

        Picasso.with(context)
                .load("http://www.sauray.com/img/group/" + g.getId() + ".png")
                .resize(100, 100)
                .transform(new ImageTrans_CircleTransform())
                .into(holder.avatar);
    }

    public void add(Group item) {
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

    public class AddressViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        ImageView avatar;
        TextView name, description;
        Group item;

        public AddressViewHolder(View lyt_main) {
            super(lyt_main);
            lyt_main.setOnClickListener(this);
            avatar = (ImageView) lyt_main.findViewById(R.id.avatar);
            name = (TextView) lyt_main.findViewById(R.id.name);
            description = (TextView) lyt_main.findViewById(R.id.description);
        }

        @Override
        public void onClick(View v) {
            callback.action(item);
        }

        public void setItem(Group item) {
            this.item = item;
        }
    }
}
