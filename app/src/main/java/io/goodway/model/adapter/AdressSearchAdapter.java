package io.goodway.model.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.goodway.R;
import io.goodway.model.callback.AddressSelected;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.UserLocation;


/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class AdressSearchAdapter extends RecyclerView.Adapter<AdressSearchAdapter.AddressViewHolder> {

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
    public AddressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_address, parent, false);
        AddressViewHolder vh = new AddressViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(AddressViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Address a = mDataset.get(position);
        switch (getItemViewType(position)){
            case Address.ADDRESS:
                holder.setItem(mDataset.get(position));
                holder.s_name.setText(a.getName());
                holder.a_name.setText(a.getSecondaryText());
                break;
            case Address.USERLOCATION:
                holder.setItem(mDataset.get(position));
                holder.s_name.setText(a.getName());
                holder.a_name.setText(((UserLocation)a).getA_name());
                break;
        }

    }

    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position).getType();
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

    public class AddressViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        TextView s_name, a_name;
        Address item;

        public AddressViewHolder(View lyt_main) {
            super(lyt_main);
            lyt_main.setOnClickListener(this);
            s_name = (TextView) lyt_main.findViewById(R.id.s_name);
            a_name = (TextView) lyt_main.findViewById(R.id.a_name);
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
