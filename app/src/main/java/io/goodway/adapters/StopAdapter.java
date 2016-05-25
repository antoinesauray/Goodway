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
import io.goodway.model.gtfs.Route;
import io.goodway.model.gtfs.Stop;
import io.goodway.utils.RouteCallback;
import io.goodway.utils.StopCallback;


/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class StopAdapter extends RecyclerView.Adapter<StopAdapter.ViewHolder> {

    private List<Stop> mDataset;
    private StopCallback callback;
    private static final String TAG="LINE_ADAPTER";

    // Provide a suitable constructor (depends on the kind of dataset)
    public StopAdapter(StopCallback callback) {
        this.callback = callback;
        mDataset = new ArrayList<Stop>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public StopAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_stop, parent, false);
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
        Stop a = mDataset.get(position);
        holder.name.setText(a.getStop_name());
    }

    public void addAll(List<Stop> items) {
        int start = mDataset.size();
        mDataset.addAll(items);
        notifyItemRangeInserted(start, mDataset.size());
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
        Stop item;

        public ViewHolder(View lyt_main) {
            super(lyt_main);
            lyt_main.setOnClickListener(this);
            name = (TextView) lyt_main.findViewById(R.id.name);
        }

        @Override
        public void onClick(View v) {
            callback.callback(item);
        }

        public void setItem(Stop item) {
            this.item = item;
        }
    }
}
