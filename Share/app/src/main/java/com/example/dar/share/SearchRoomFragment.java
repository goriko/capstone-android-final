package com.example.dar.share;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@SuppressLint("ValidFragment")
public class SearchRoomFragment extends Fragment {

    private final int TIME_PICKER_INTERVAL = 5;

    private View rootView;

    private TextView textViewTime;
    private Button buttonTime, buttonFind;
    private AutoCompleteTextView editTextOrigin, editTextDestination;

    private Integer departureHour = null, departureMinute = null;
    private String originString, destinationString;
    private LatLng originLatLng, destinationLatLng;

    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private static final LatLngBounds latLngBounds = new LatLngBounds(new com.google.android.gms.maps.model.LatLng(-40, -168), new com.google.android.gms.maps.model.LatLng(71, 136));

    public SearchRoomFragment(String Origin, LatLng Orginlatlng, String Destination, LatLng DestinationLatLng){
        this.originString = Origin;
        this.originLatLng = Orginlatlng;
        this.destinationString = Destination;
        this.destinationLatLng = DestinationLatLng;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search_room, container, false);

        textViewTime = (TextView) rootView.findViewById(R.id.textViewTime);
        buttonTime = (Button) rootView.findViewById(R.id.buttonTime);
        buttonFind = (Button) rootView.findViewById(R.id.buttonFind);
        editTextOrigin = (AutoCompleteTextView) rootView.findViewById(R.id.editTextOrigin);
        editTextDestination = (AutoCompleteTextView) rootView.findViewById(R.id.editTextDestination);

        editTextOrigin.setText(originString);
        editTextDestination.setText(destinationString);

        if (departureHour != null){
            String time = departureHour+":"+departureMinute;
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("hh:mm a");
            Date date = new Date();
            try {
                date = dateFormat1.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            textViewTime.setText(dateFormat2.format(date));
        }

        GeoDataClient geoDataClient = Places.getGeoDataClient(NavBarActivity.sContext, null);
        AutocompleteFilter filter =
                new AutocompleteFilter.Builder().setCountry("PH").build();
        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(NavBarActivity.sContext, geoDataClient, latLngBounds, filter);
        editTextOrigin.setAdapter(placeAutocompleteAdapter);
        editTextDestination.setAdapter(placeAutocompleteAdapter);

        buttonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTime();
            }
        });

        buttonFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextOrigin.getText().toString().equals("") && !editTextDestination.getText().toString().equals("")){
                    Fragment fragment = new RoomFragment(originString, originLatLng, destinationString, destinationLatLng, departureHour, departureMinute);
                    replaceFragment(fragment);
                }else{
                    Toast.makeText(NavBarActivity.sContext, "Please enter origin and destination addresses", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setTime(){
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR);
        int minutes = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                departureHour = hourOfDay;
                departureMinute = minute;
                String time = departureHour+":"+departureMinute;
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm");
                SimpleDateFormat dateFormat2 = new SimpleDateFormat("hh:mm a");
                Date date = new Date();
                try {
                    date = dateFormat1.parse(time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                textViewTime.setText(dateFormat2.format(date));
            }
        }, hours, minutes, false);

        timePickerDialog.show();
    }

    public void replaceFragment(Fragment someFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, someFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
