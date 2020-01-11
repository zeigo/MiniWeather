package com.example.zjj.miniweather;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {
    private List<String> places;
    private LayoutInflater inflater;
    private Context context;
    public PlaceAdapter(Context context, List<String> places) {
        this.places = places;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setData(List<String> newData) {
        places = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.place);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(places.get(position));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.simple_place,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LocationActivity)context).selected(((TextView)view).getText().toString());
            }
        });
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return places == null ? 0 : places.size();
    }
}
