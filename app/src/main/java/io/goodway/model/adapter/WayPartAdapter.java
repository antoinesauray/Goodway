package io.goodway.model.adapter;

import android.app.Activity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.goodway.R;
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
        int[] times = WayPart.splitToComponentTimes(way.getDuration());
        String timeStr="";
        if(times[0]!=0){
            if(times[1]!=0){
                timeStr+=times[0]+":";
            }
            else{
                timeStr+=times[1]+" "+activity.getString(R.string.hours);
            }
        }
        if(times[1]!=0){
            if(times[0]!=0){
                timeStr+=times[1];
            }
            else{
                timeStr+=times[1]+" "+activity.getString(R.string.minutes);
            }
        }
        holder.from.setText(activity.getString(R.string.from) + " " + way.getFrom().toString());
        Log.d("way.getLocaleType()", way.getType());
        holder.type.setText(activity.getString(getResource(way.getType())) +" "+ activity.getString(R.string.during)+" "+timeStr);
        holder.to.setText(activity.getString(R.string.to)+" "+way.getTo().toString());
    }

    private int getResource(String type){
        switch (type){
            case "Bus Trip":
                return R.string.bustrip;
            case "Walking":
                return R.string.walking;
            case "Waiting":
                return R.string.waiting;
            case "Transfer":
                return R.string.transfer;
            default:
                return R.string.unknown;
        }
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
        TextView time, from, to, type;
        WayPart item;
        Activity activity;

        public ViewHolder(Activity activity, View lyt_main) {
            super(lyt_main);
            this.activity = activity;
            time = (TextView) lyt_main.findViewById(R.id.time);
            from = (TextView) lyt_main.findViewById(R.id.from);
            to = (TextView) lyt_main.findViewById(R.id.to);
            type = (TextView) lyt_main.findViewById(R.id.type);
        }
        public void setItem(WayPart item) {
            this.item = item;
        }
    }
}
