package io.goodway.adapters;


import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.goodway.R;
import io.goodway.model.callback.GroupLocationCallback;
import io.goodway.navitia_android.GroupLocation;


/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class GroupLocationAdapter extends RecyclerView.Adapter<GroupLocationAdapter.ViewHolder> {

    private List<GroupLocation> mDataset;
    private Activity activity;
    private GroupLocationCallback callback;

    private static final String TAG="LINE_ADAPTER";

    // Provide a suitable constructor (depends on the kind of dataset)
    public GroupLocationAdapter(Activity activity, GroupLocationCallback callback) {
        this.activity = activity;
        this.callback = callback;
        mDataset = new ArrayList<GroupLocation>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GroupLocationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_group_location, parent, false);
        return new ViewHolder(activity, v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.setItem(mDataset.get(position));
        GroupLocation a = (GroupLocation) mDataset.get(position);
        holder.name.setText(a.getName());
        holder.description.setText(a.getA_name());
    }

    public void add(GroupLocation item) {
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

    public GroupLocation get(int position){
        return mDataset.get(position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position).getType();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        TextView name, description;
        GroupLocation item;
        Activity activity;

        public ViewHolder(Activity activity, View lyt_main) {
            super(lyt_main);
            lyt_main.setOnClickListener(this);
            name = (TextView) lyt_main.findViewById(R.id.name);
            description = (TextView) lyt_main.findViewById(R.id.description);
            this.activity = activity;
        }

        @Override
        public void onClick(View v) {
            callback.action(item);
        }

        public void setItem(GroupLocation item) {
            this.item = item;
        }
    }

}
