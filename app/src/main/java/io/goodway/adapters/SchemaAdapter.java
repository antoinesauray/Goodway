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
import io.goodway.model.gtfs.Schema;
import io.goodway.utils.SchemaCallback;


/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class SchemaAdapter extends RecyclerView.Adapter<SchemaAdapter.ViewHolder> {

    private List<Schema> mDataset;
    private SchemaCallback callback;
    private static final String TAG="LINE_ADAPTER";

    // Provide a suitable constructor (depends on the kind of dataset)
    public SchemaAdapter(SchemaCallback callback) {
        this.callback = callback;
        mDataset = new ArrayList<Schema>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SchemaAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_schema, parent, false);
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
        Schema a = mDataset.get(position);
        holder.name.setText(a.getName());
    }

    public void add(Schema item) {
        int position = mDataset.size();
        mDataset.add(position, item);
        notifyItemInserted(position);
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
        Schema item;
        Activity activity;

        public ViewHolder(View lyt_main) {
            super(lyt_main);
            lyt_main.setOnClickListener(this);
            name = (TextView) lyt_main.findViewById(R.id.name);
        }

        @Override
        public void onClick(View v) {
            callback.callback(item);
        }

        public void setItem(Schema item) {
            this.item = item;
        }
    }
}
