package io.goodway.model.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.goodway.R;
import io.goodway.navitia_android.Address;
import io.goodway.navitia_android.BusTrip;
import io.goodway.navitia_android.Walking;
import io.goodway.navitia_android.WayPart;
import io.goodway.navitia_android.WayPartType;


/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class WayPartAdapter extends RecyclerView.Adapter<WayPartAdapter.ItemHolder> {

    private List<WayPart> mDataset;
    private Activity activity;

    private static final String TAG="LINE_ADAPTER";

    // Provide a suitable constructor (depends on the kind of dataset)
    public WayPartAdapter(Activity activity){
        this.activity = activity;
        mDataset = new ArrayList<WayPart>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        WayPartType type = WayPartType.values()[viewType];
        View view=null;
        switch (type){
            case BusTrip:
                view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.view_bustrip, parent, false);
                return new BusHolder(view);
            case Transfer:
                view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.view_transfer, parent, false);
                return new TransferHolder(view);
            case Waiting:
                view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.view_waiting, parent, false);
                return new WaitingHolder(view);
            case Walking:
                view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.view_walking, parent, false);
                return new WalkingHolder(view);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        WayPart wayPart = mDataset.get(position);
        switch (wayPart.getWayPartType()){
            case BusTrip:
                BusHolder busHolder = (BusHolder) holder;
                BusTrip trip = (BusTrip)wayPart;
                busHolder.line.setText(trip.getRoute().getLine().getName());
                String[] splitDeparture = Address.splitIso8601(wayPart.getDepartureDateTime());
                String[] splitArrival = Address.splitIso8601(wayPart.getArrivalDateTime());
                busHolder.departureTime.setText(splitDeparture[3]+":"+splitDeparture[4]);
                busHolder.arrivalTime.setText(splitArrival[3]+":"+splitArrival[4]);
                busHolder.departure.setText(wayPart.getFrom().toString());
                busHolder.destination.setText(wayPart.getTo().toString());
                try{
                    busHolder.line.setBackgroundColor(Color.parseColor("#" + trip.getRoute().getLine().getColor()));
                    busHolder.line.setTextColor(Color.WHITE);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
                            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void getOutline(View view, Outline outline) {
                                // Or read size directly from the view's width/height
                                //outline.setOval(0, 0, view.getWidth(), view.getWidth());
                                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (view.getWidth())/2);
                            }
                        };
                        busHolder.line.setOutlineProvider(viewOutlineProvider);
                        busHolder.line.setClipToOutline(true);
                    }

                }
                catch (IllegalArgumentException e){
                    busHolder.destination.setTextColor(activity.getResources().getColor(android.R.color.primary_text_light));
                }
                break;
            case Walking:
                WalkingHolder walkingHolder = (WalkingHolder) holder;
                walkingHolder.action.setText(wayPart.getAction(activity));
                walkingHolder.destination.setText(activity.getString(R.string.to)+" "+wayPart.getTo().getName());
                break;
            case Waiting:
                WaitingHolder waitingHolder = (WaitingHolder) holder;
                waitingHolder.action.setText(wayPart.getAction(activity));
                waitingHolder.time.setText(Address.secondToStr(activity, wayPart.getDuration()));
                break;
            case Transfer:
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return mDataset.get(position).getWayPartType().ordinal();
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

    public class ItemHolder<T> extends RecyclerView.ViewHolder{

        private T item;

        public ItemHolder(View itemView) {
            super(itemView);
        }
        public void setItem(T t){
            this.item = t;
        }
    }

    public class BusHolder extends ItemHolder{
        // each data item is just a string in this case
        TextView departure, departureTime, arrivalTime, line, destination;

        public BusHolder(View lyt_main) {
            super(lyt_main);
            departure = (TextView) lyt_main.findViewById(R.id.departure);
            departureTime = (TextView) lyt_main.findViewById(R.id.departureTime);
            arrivalTime = (TextView) lyt_main.findViewById(R.id.arrivalTime);
            line = (TextView) lyt_main.findViewById(R.id.line);
            destination = (TextView) lyt_main.findViewById(R.id.destination);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                line.setOutlineProvider(new ViewOutlineProvider() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setOval(view.getWidth(), view.getWidth(), view.getWidth(), view.getHeight());
                    }
                });
            }
        }
    }
    public class TransferHolder extends ItemHolder{
        // each data item is just a string in this case
        public TransferHolder(View lyt_main) {
            super(lyt_main);
        }
    }

    public class WaitingHolder extends ItemHolder{
        // each data item is just a string in this case
        TextView time, action;

        public WaitingHolder(View lyt_main) {
            super(lyt_main);
            time = (TextView) lyt_main.findViewById(R.id.time);
            action = (TextView) lyt_main.findViewById(R.id.action);
        }
    }

    public class WalkingHolder extends ItemHolder{
        // each data item is just a string in this case
        TextView action, destination;

        public WalkingHolder(View lyt_main) {
            super(lyt_main);
            action = (TextView) lyt_main.findViewById(R.id.action);
            destination = (TextView) lyt_main.findViewById(R.id.destination);
        }
    }
}
