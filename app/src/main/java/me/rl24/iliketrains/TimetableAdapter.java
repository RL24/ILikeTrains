package me.rl24.iliketrains;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import me.rl24.iliketrains.beans.Stop;
import me.rl24.iliketrains.beans.Trip;
import me.rl24.iliketrains.beans.TripStop;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private final List<Trip> localDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFromLabel, tvFromTime, tvToLabel, tvToTime;

        public ViewHolder(View view) {
            super(view);

            tvFromLabel = view.findViewById(R.id.tvFromLabel);
            tvFromTime = view.findViewById(R.id.tvFromTime);
            tvToLabel = view.findViewById(R.id.tvToLabel);
            tvToTime = view.findViewById(R.id.tvToTime);
        }

        public TextView getTvFromLabel() {
            return tvFromLabel;
        }

        public TextView getTvFromTime() {
            return tvFromTime;
        }

        public TextView getTvToLabel() {
            return tvToLabel;
        }

        public TextView getTvToTime() {
            return tvToTime;
        }
    }

    public TimetableAdapter(List<Trip> dataSet) {
        localDataSet = dataSet;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        Trip trip = localDataSet.get(position);
        Stop[] boundStops = trip.boundStops;
        TripStop[] stops = trip.stops;

        TripStop tripStop = null;
        for (TripStop s : stops)
            if (s.stop.contentEquals(StorageService.cityId))
                tripStop = s;

        String fromId;
        String toId;
        String depart;
        String arrive;
        if (stops[0].stop.equals("WELL")) {
            fromId = stops[0].stop; // wellington
            toId = tripStop != null ? tripStop.stop : stops[stops.length - 2].stop; // selected city or default paraparaumu
            depart = stops[0].depart;
            arrive = tripStop != null ? tripStop.arrive : stops[stops.length - 2].arrive;
        } else {
            fromId = tripStop != null ? tripStop.stop : stops[1].stop; // selected city or default paraparaumu
            toId = stops[stops.length - 1].stop; // wellington
            depart = tripStop != null ? tripStop.depart : stops[1].depart;
            arrive = stops[stops.length - 1].arrive;
        }

        String fromName = "";
        String toName = "";

        for (Stop boundStop : boundStops) {
            if (boundStop.id.equals(fromId))
                fromName = boundStop.name.replace("Station", "").trim();
            if (boundStop.id.equals(toId))
                toName = boundStop.name.replace("Station", "").trim();
        }

        viewHolder.getTvFromLabel().setText(fromName);
        viewHolder.getTvFromTime().setText(DateConverter.convert(depart));
        viewHolder.getTvToLabel().setText(toName);
        viewHolder.getTvToTime().setText(DateConverter.convert(arrive));
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}