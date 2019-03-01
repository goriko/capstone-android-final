package com.example.dar.share;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.Calendar;

@SuppressLint("ValidFragment")
public class SearchRoomFragment extends Fragment {

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
            if(departureHour > 12){
                textViewTime.setText(departureHour-12+":"+departureMinute+" pm");
            }else{
                textViewTime.setText(departureHour+":"+departureMinute+" am");
            }
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

    private void setTime(){
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR);
        int minutes = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                departureHour = hourOfDay;
                departureMinute = minute;
                if(departureHour > 12){
                    textViewTime.setText(departureHour-12+":"+departureMinute+" pm");
                }else{
                    textViewTime.setText(departureHour+":"+departureMinute+" am");
                }
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
