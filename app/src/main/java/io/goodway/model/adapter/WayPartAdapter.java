package io.goodway.model.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
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
import io.goodway.navitia_android.WayPart;


/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class WayPartAdapter extends RecyclerView.Adapter<WayPartAdapter.ViewHolder> {

    private List<WayPart> mDataset;
    private Activity activity;

    private static final String TAG="LINE_ADAPTER";

    // Provide a suitable constructor (depends on the kind of dataset)
    public WayPartAdapter(Activity activity, GridLayoutManager layoutManager) {
        this.activity = activity;
        mDataset = new ArrayList<WayPart>();

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                //return mDataset.get(position).getSpans();
                return 3;
            }
        });

    }

    // Create new views (invoked by the layout manager)
    @Override
    public WayPartAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_waypart, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(activity, v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        WayPart way = mDataset.get(position);
        holder.setItem(way);
        String[] departure = Address.toHumanTime(way.getDepartureDateTime());
        String[] arrival = Address.toHumanTime(way.getArrivalDateTime());
        holder.departure.setText("Départ à "+departure[0]+"h"+departure[1]);
        holder.arrival.setText("Arrivée à "+arrival[0]+"h"+arrival[1]);
        holder.duration.setText(way.getDuration()+"");
        holder.type.setText(way.getType());
    }

    public void add(WayPart item) {
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

    public class ViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        TextView type, departure, arrival, duration;
        WayPart item;
        Activity activity;

        public ViewHolder(Activity activity, View lyt_main) {
            super(lyt_main);
            this.activity = activity;
            type = (TextView) lyt_main.findViewById(R.id.type);
            departure = (TextView) lyt_main.findViewById(R.id.departure);
            arrival = (TextView) lyt_main.findViewById(R.id.arrival);
            duration = (TextView) lyt_main.findViewById(R.id.duration);
        }


        public void setItem(WayPart item) {
            this.item = item;
        }
    }
}
