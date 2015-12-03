package io.goodway.model.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.Callback;

import io.goodway.R;
import io.goodway.model.callback.AddressSelected;
import io.goodway.navitia_android.Address;


/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class AdressSearchAdapter extends RecyclerView.Adapter<AdressSearchAdapter.ViewHolder> {

    private List<Address> mDataset;
    private Context activity;
    private AddressSelected callback;

    private static final String TAG="LINE_ADAPTER";

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdressSearchAdapter(AddressSelected callback) {
        mDataset = new ArrayList<Address>();
        this.callback = callback;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AdressSearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_adress, parent, false);
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
        Address a = mDataset.get(position);
        holder.name.setText(a.getName(activity));
        //holder.icon.setImageDrawable(activity.getResources().getDrawable(a.getIcon(), activity.getTheme()));
    }

    public void add(Address item) {
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
        ImageView icon;
        Address item;

        public ViewHolder(View lyt_main) {
            super(lyt_main);
            lyt_main.setOnClickListener(this);
            name = (TextView) lyt_main.findViewById(R.id.name);
            icon = (ImageView) lyt_main.findViewById(R.id.icon);
        }

        @Override
        public void onClick(View v) {
            callback.action(item);
        }

        public void setItem(Address item) {
            this.item = item;
        }
    }
}
