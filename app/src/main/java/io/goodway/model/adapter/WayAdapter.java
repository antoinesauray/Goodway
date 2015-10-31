package io.goodway.model.adapter;


import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.goodway.DetailedWayActivity;
import io.goodway.R;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.Way;


/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class WayAdapter extends RecyclerView.Adapter<WayAdapter.ViewHolder> {

    private List<Way> mDataset;
    private Activity activity;

    private static final String TAG="LINE_ADAPTER";

    // Provide a suitable constructor (depends on the kind of dataset)
    public WayAdapter(Activity activity) {
        this.activity = activity;
        mDataset = new ArrayList<Way>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public WayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_way, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(activity, v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Way way = mDataset.get(position);
        holder.setItem(way);
        Way t = mDataset.get(position);
        //holder.icon.setImageDrawable(activity.getResources().getDrawable(t.getImage(), activity.getTheme()));
        String[] departure = Address.toHumanTime(way.getDepartureDateTime());
        String[] arrival = Address.toHumanTime(way.getArrivalDateTime());
        holder.departure.setText("Départ à "+departure[0]+"h"+departure[1]);
        holder.arrival.setText("Arrivée à "+arrival[0]+"h"+arrival[1]);
        holder.destination.setText(way.getLabel());
    }

    public void add(Way item) {
        int position = mDataset.size();
        mDataset.add(position, item);
        notifyItemInserted(position);
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
        TextView departure, arrival, destination;
        ImageView icon;
        Way item;
        Activity activity;

        public ViewHolder(Activity activity, View lyt_main) {
            super(lyt_main);
            lyt_main.setOnClickListener(this);
            this.activity = activity;
            departure = (TextView) lyt_main.findViewById(R.id.departure);
            arrival = (TextView) lyt_main.findViewById(R.id.arrival);
            icon = (ImageView) lyt_main.findViewById(R.id.icon);
            destination = (TextView) lyt_main.findViewById(R.id.destination);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(activity, DetailedWayActivity.class);
            intent.putExtra("WAY", item);
            activity.startActivity(intent);
        }

        public void setItem(Way item) {
            this.item = item;
        }
    }
}
