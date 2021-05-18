package me.rl24.iliketrains;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.rl24.iliketrains.beans.Bound;
import me.rl24.iliketrains.beans.Stop;
import me.rl24.iliketrains.beans.TrainParams;
import me.rl24.iliketrains.beans.TrainTimetable;
import me.rl24.iliketrains.beans.Trip;
import me.rl24.iliketrains.beans.TripStop;

public class MainActivity extends AppCompatActivity {

    private static final Gson GSON = new Gson();
    private static final String METLINK_URL = "https://backend.metlink.org.nz/api/v1/timetable";

    private final List<Trip> trips = new ArrayList<>();
    private boolean directionInbound = true;
    private Bound inbound, outbound;
    private TimetableAdapter adapter;
    private ArrayAdapter<CharSequence> cityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new TimetableAdapter(trips);

        RecyclerView rvTrips = findViewById(R.id.rvTrips);
        rvTrips.setLayoutManager(new LinearLayoutManager(this));
        rvTrips.addItemDecoration(new DividerItemDecoration(rvTrips.getContext(), DividerItemDecoration.VERTICAL));
        rvTrips.setAdapter(adapter);

        FloatingActionButton fabCitySelect = findViewById(R.id.fabCitySelect);
        fabCitySelect.setOnClickListener((view) -> {
            PopupMenu popup = new PopupMenu(this, fabCitySelect);
            MenuInflater inflater = popup.getMenuInflater();
            Menu menu = popup.getMenu();
            inflater.inflate(R.menu.menu_blank, menu);

            for (Stop stop : inbound.stops) {
                MenuItem item = menu.add(stop.name);
                item.setOnMenuItemClickListener((i) -> {
                    StorageService.cityId = stop.id;
                    loadTrains();
                    return true;
                });
            }

            popup.show();
        });

        requestTrains();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnFlip:
                flipTrains();
                return true;
            case R.id.btnRefresh:
                requestTrains();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void requestTrains() {
        RequestQueue queue = Volley.newRequestQueue(this);

        String formattedDate = DateConverter.getCurrent();
        Map<String, String> headers = new HashMap<>();
        TrainParams params = new TrainParams(formattedDate, formattedDate, "KPL");

        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");

        String body = GSON.toJson(params);

        GsonRequest<TrainTimetable> stringRequest = new GsonRequest<TrainTimetable>(
                Request.Method.POST, METLINK_URL, TrainTimetable.class, headers,
                (response) -> {
                    directionInbound = true;
                    inbound = response.inbound;
                    outbound = response.outbound;

                    List<String> stops = new ArrayList<>();
                    for (Stop stop : inbound.stops)
                        stops.add(stop.id);

                    loadTrains();
                    Toast.makeText(this, "Refreshed timetable", Toast.LENGTH_LONG).show();
                },
                (error) -> Toast.makeText(this, "Failed to refresh timetable: " + error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            public byte[] getBody() {
                return body.getBytes();
            }
        };

        queue.add(stringRequest);
    }

    private void flipTrains() {
        directionInbound = !directionInbound;
        loadTrains();
    }

    private void loadTrains() {
        Bound bound = directionInbound ? inbound : outbound;
        Stop[] boundStops = bound.stops;
        for (int i = 0; i < bound.trips.length; i++)
            bound.trips[i].boundStops = boundStops;

        List<Trip> filtered = new ArrayList<>();
        for (Trip trip : bound.trips) {
            boolean has = true;
            for (TripStop stop : trip.stops)
                if ((stop.arrive == null || stop.depart == null) && stop.stop.contentEquals(StorageService.cityId)) {
                    has = false;
                    break;
                }
            if (has)
                filtered.add(trip);
        }

        trips.clear();
        trips.addAll(filtered);
        adapter.notifyDataSetChanged();
    }

}