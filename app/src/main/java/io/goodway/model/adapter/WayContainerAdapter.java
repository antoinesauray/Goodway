package io.goodway.model.adapter;


import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.goodway.R;
import io.goodway.model.ContainerType;
import io.goodway.model.callback.WayCallback;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.ErrorAction;
import io.goodway.navitia_android.Line;
import io.goodway.navitia_android.Pair;
import io.goodway.navitia_android.Request;
import io.goodway.navitia_android.Way;


/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class WayContainerAdapter extends RecyclerView.Adapter<WayContainerAdapter.ViewHolder>{

    private List<ContainerType> mDataset;
    private Activity activity;
    private WayCallback callback;

    private Address from, to;

    private static final String TAG="LINE_ADAPTER";
    private String fromLatitude, fromLongitude, toLatitude, toLongitude;
    private String date;
    // Provide a suitable constructor (depends on the kind of dataset)
    public WayContainerAdapter(Activity activity, WayCallback callback, Address from, Address to, String fromLatitude, String fromLongitude, String toLatitude, String toLongitude, String date) {
        this.activity = activity;
        this.callback = callback;
        mDataset = new ArrayList<ContainerType>();
        this.from = from;
        this.to = to;
        this.fromLatitude = fromLatitude;
        this.fromLongitude = fromLongitude;
        this.toLatitude = toLatitude;
        this.toLongitude = toLongitude;
        this.date = date;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public WayContainerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_way_card, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(activity, v, callback);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ContainerType type = mDataset.get(position);
        ArrayList<Pair> pairs = new ArrayList<>();
        pairs.add(new Pair("from", fromLongitude + ";" + fromLatitude));
        pairs.add(new Pair("to", toLongitude + ";" + toLatitude));
        pairs.add(new Pair("datetime", date));
        holder.ways.removeAllViews();
        switch (type){
            case navitia:
                holder.provider.setText(R.string.navitia);
                Request.getWays(new Action<Way>() {
                    @Override
                    public void action(final Way e) {
                        View v = activity.getLayoutInflater().inflate(R.layout.view_way, null);
                        ((TextView)v.findViewById(R.id.description)).setText(e.getFrom().toString() + " - " + e.getTo().toString());
                        String[] departureTime = Address.splitIso8601(e.getDepartureDateTime());
                        String[] arrivalTime = Address.splitIso8601(e.getArrivalDateTime());
                        ((TextView) v.findViewById(R.id.departure)).setText(departureTime[3]+":"+departureTime[4]);
                        ((TextView)v.findViewById(R.id.arrival)).setText(arrivalTime[3]+":"+arrivalTime[4]);
                        ((TextView) v.findViewById(R.id.duration)).setText(activity.getString(R.string.duration)+" "+Address.secondToStr(activity, e.getDuration()));
                        holder.ways.addView(v);
                        //notifyItemChanged(0);
                        v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                callback.action(v, e);
                            }
                        });
                    }

                }, pairs, from, to, new ErrorAction() {
                    @Override
                    public void action(int length) {
                        switch (length) {
                            case 0:
                                View v = activity.getLayoutInflater().inflate(R.layout.view_way_not_found, null);
                                ((TextView)v.findViewById(R.id.message)).setText(R.string.noNavitia);
                                holder.ways.addView(v);
                                break;
                            case -1:
                                View notAvailable = activity.getLayoutInflater().inflate(R.layout.view_way_not_found, null);
                                ((TextView)notAvailable.findViewById(R.id.message)).setText(R.string.unavailable);
                                holder.ways.addView(notAvailable);
                                break;
                        }
                    }
                });
                break;
            case carSharing:
                holder.provider.setText(R.string.carSharing);
                View carSharing = activity.getLayoutInflater().inflate(R.layout.view_way_not_found, null);
                ((TextView)carSharing.findViewById(R.id.message)).setText(R.string.noCarSharing);
                holder.ways.addView(carSharing);
                break;
            case bike:
                holder.provider.setText(R.string.bike);
                View bike = activity.getLayoutInflater().inflate(R.layout.view_way_not_found, null);
                ((TextView)bike.findViewById(R.id.message)).setText(R.string.noBike);
                holder.ways.addView(bike);
                break;
            case uber:
                holder.provider.setText(R.string.uber);
                View uber = activity.getLayoutInflater().inflate(R.layout.view_way_not_found, null);
                ((TextView)uber.findViewById(R.id.message)).setText(R.string.noUber);
                holder.ways.addView(uber);
                break;
        }

    }

    public void add(ContainerType item) {
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

    public void setDepartureTime(String date){this.date = date;}

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        ContainerType type;
        TextView provider;
        LinearLayout ways;
        public ViewHolder(Activity activity, View lyt_main, WayCallback callback) {
            super(lyt_main);
            provider = (TextView) lyt_main.findViewById(R.id.provider);
            ways = (LinearLayout) lyt_main.findViewById(R.id.ways);
        }
    }
}
