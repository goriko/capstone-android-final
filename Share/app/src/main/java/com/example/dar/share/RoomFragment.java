package com.example.dar.share;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("ValidFragment")
public class RoomFragment extends Fragment implements View.OnClickListener {

    private Integer x = 0, departureHour, departureMinute, fareFrom, fareTo, estimatedTravelTime;
    private String[] str = new String[100];
    private String originString, destinationString;
    private LatLng originLatLng, destinationLatLng;

    private View rootView;
    private Button buttonCreate;

    private GridLayout gridView;
    private Fragment fragment = null;

    private DatabaseReference databaseReference;

    private ProgressDialog progressDialog;

    public RoomFragment(String origin, LatLng originLatLng, String destination, LatLng destinationLatLng, Integer departureHour, Integer departureMinute){
        this.originString = origin;
        this.originLatLng = originLatLng;
        this.destinationString = destination;
        this.destinationLatLng = destinationLatLng;
        this.departureHour = departureHour;
        this.departureMinute = departureMinute;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_room, container, false);

        progressDialog = new ProgressDialog(this.getContext());
        progressDialog.setMessage("Loading ....");
        progressDialog.setCancelable(false);
        progressDialog.show();

        gridView = (GridLayout) rootView.findViewById(R.id.layout);
        buttonCreate = (Button) rootView.findViewById(R.id.buttonCreate);

        databaseReference = FirebaseDatabase.getInstance().getReference("travel");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                gridView.removeAllViews();
                LatLng userOrigin = new LatLng(convert(originLatLng.getLatitude()), convert(originLatLng.getLongitude()));
                LatLng userDestination = new LatLng(convert(destinationLatLng.getLatitude()), convert(destinationLatLng.getLongitude()));
                for (DataSnapshot data: dataSnapshot.getChildren()){
                    if(data.child("Available").getValue().toString().equals("1")){
                        LatLng dataOrigin = new LatLng(convert(Double.parseDouble(data.child("Origin").child("latitude").getValue().toString())), convert(Double.parseDouble(data.child("Origin").child("longitude").getValue().toString())));
                        LatLng dataDestination = new LatLng(convert(Double.parseDouble(data.child("Destination").child("latitude").getValue().toString())), convert(Double.parseDouble(data.child("Destination").child("longitude").getValue().toString())));
                        if (dataOrigin.equals(userOrigin) && dataDestination.equals(userDestination)) {
                            if (departureHour == null) {
                                x++;
                                layout(x, data);
                            } else {
                                if (data.child("DepartureTime").child("DepartureHour").getValue() != null && data.child("DepartureTime").child("DepartureMinute").getValue() != null){
                                    Integer hour = Integer.parseInt(data.child("DepartureTime").child("DepartureHour").getValue().toString());
                                    Integer minute = Integer.parseInt(data.child("DepartureTime").child("DepartureMinute").getValue().toString());
                                    if (departureHour == hour && departureMinute == minute){
                                        x++;
                                        layout(x,data);
                                    }
                                }
                            }
                        }
                    }
                }
                if (x == 0){
                    Toast.makeText(NavBarActivity.sContext, "No rooms found", Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (departureHour == null){
                    setTime();
                }else{
                    geoLocate();
                }
            }
        });

        return rootView;
    }

    @SuppressLint("ResourceAsColor")
    public void layout(int x, DataSnapshot data){
        CardView cardView = new CardView(NavBarActivity.sContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardView.setLayoutParams(layoutParams);
        cardView.setUseCompatPadding(true);

        RelativeLayout relativeLayout1 = new RelativeLayout(NavBarActivity.sContext);
        RelativeLayout.LayoutParams relParams1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeLayout1.setLayoutParams(relParams1);
        relativeLayout1.setPadding(dp(20), 0, dp(20), 0);

        LinearLayout linearLayout1 = new LinearLayout(NavBarActivity.sContext);
        LinearLayout.LayoutParams linParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout1.setLayoutParams(linParams1);
        linearLayout1.setOrientation(LinearLayout.VERTICAL);
        linearLayout1.setPadding(0, dp(20), 0, 0);

        TextView textView1 = new TextView(NavBarActivity.sContext);
        LinearLayout.LayoutParams textParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams1.gravity = Gravity.RIGHT;
        textView1.setLayoutParams(textParams1);
        textView1.setText("Join Room");
        textView1.setTextColor(NavBarActivity.sContext.getResources().getColor(R.color.colorYellow));
        textView1.setTypeface(Typeface.DEFAULT_BOLD);
        textView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) 15.5);
        textView1.setId(x);

        LinearLayout linearLayout2 = new LinearLayout(NavBarActivity.sContext);
        LinearLayout.LayoutParams linParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout2.setLayoutParams(linParams2);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout2.setPadding(dp(20), dp(20), dp(20), dp(15));

        LinearLayout linearLayout3 = new LinearLayout(NavBarActivity.sContext);
        linearLayout3.setLayoutParams(linParams2);
        linearLayout3.setPadding(0, dp(20), 0, dp(20));

        TextView textView2 = new TextView(NavBarActivity.sContext);
        LinearLayout.LayoutParams textParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView2.setLayoutParams(textParams2);
        textView2.setText("Room Information");
        textView2.setTypeface(Typeface.DEFAULT_BOLD);

        RelativeLayout relativeLayout2 = new RelativeLayout(NavBarActivity.sContext);
        RelativeLayout.LayoutParams relParams2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dp(2));
        relativeLayout2.setLayoutParams(relParams2);
        relativeLayout2.setPadding(0, dp(18), 0, dp(15));
        relativeLayout2.setBackgroundColor(R.color.colorBlue);

        LinearLayout linearLayout4 = new LinearLayout(NavBarActivity.sContext);
        LinearLayout.LayoutParams linParams4 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout4.setLayoutParams(linParams4);
        linearLayout4.setPadding(0, dp(20), 0, 0);

        TextView textView3 = new TextView(NavBarActivity.sContext);
        LinearLayout.LayoutParams textParams3 = new LinearLayout.LayoutParams(dp(120), LinearLayout.LayoutParams.WRAP_CONTENT);
        textView3.setLayoutParams(textParams3);
        textView3.setText("Starting Address:");

        TextView textView4 = new TextView(NavBarActivity.sContext);
        LinearLayout.LayoutParams textParams4 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(30));
        textView4.setLayoutParams(textParams4);
        textView4.setPadding(dp(20), 0, dp(20), 0);
        textView4.setText(data.child("OriginString").getValue().toString());

        LinearLayout linearLayout5 = new LinearLayout(NavBarActivity.sContext);
        LinearLayout.LayoutParams linParams5 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout5.setLayoutParams(linParams5);

        TextView textView5 = new TextView(NavBarActivity.sContext);
        LinearLayout.LayoutParams textParams5 = new LinearLayout.LayoutParams(dp(120), LinearLayout.LayoutParams.WRAP_CONTENT);
        textView5.setLayoutParams(textParams5);
        textView5.setText("Destination:");

        TextView textView6 = new TextView(NavBarActivity.sContext);
        LinearLayout.LayoutParams textParams6 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(30));
        textView6.setLayoutParams(textParams6);
        textView6.setPadding(dp(20), 0, dp(20), 0);
        textView6.setText(data.child("DestinationString").getValue().toString());

        LinearLayout linearLayout6 = new LinearLayout(NavBarActivity.sContext);
        LinearLayout.LayoutParams linParams6 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout6.setLayoutParams(linParams6);

        TextView textView7 = new TextView(NavBarActivity.sContext);
        LinearLayout.LayoutParams textParams7 = new LinearLayout.LayoutParams(dp(120), LinearLayout.LayoutParams.WRAP_CONTENT);
        textView7.setLayoutParams(textParams7);
        textView7.setText("Departure Time");

        TextView textView8 = new TextView(NavBarActivity.sContext);
        LinearLayout.LayoutParams textParams8 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(30));
        textView8.setLayoutParams(textParams8);
        textView8.setPadding(dp(20), 0, dp(20), 0);
        String time = data.child("DepartureTime").child("DepartureHour").getValue().toString()+":"+data.child("DepartureTime").child("DepartureMinute").getValue().toString();
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("hh:mm a");
        Date date = new Date();
        try {
            date = dateFormat1.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        textView8.setText(dateFormat2.format(date));
        LinearLayout linearLayout7 = new LinearLayout(NavBarActivity.sContext);
        LinearLayout.LayoutParams linParams7 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout7.setLayoutParams(linParams7);

        TextView textView9 = new TextView(NavBarActivity.sContext);
        LinearLayout.LayoutParams textParams9 = new LinearLayout.LayoutParams(dp(120), LinearLayout.LayoutParams.WRAP_CONTENT);
        textView9.setLayoutParams(textParams9);
        textView9.setText("Population");

        TextView textView10 = new TextView(NavBarActivity.sContext);
        LinearLayout.LayoutParams textParams10 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(30));
        textView10.setLayoutParams(textParams10);
        textView10.setPadding(dp(20), 0, dp(20), 0);
        textView10.setText(data.child("NoOfUsers").getValue().toString()+"/4");

        LinearLayout linearLayout8 = new LinearLayout(NavBarActivity.sContext);
        LinearLayout.LayoutParams linParams8 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout8.setLayoutParams(linParams8);
        linearLayout8.setOrientation(LinearLayout.VERTICAL);
        linearLayout8.setPadding(0, dp(20), 0, 0);

        TextView textView11 = new TextView(NavBarActivity.sContext);
        LinearLayout.LayoutParams textParams11 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams11.gravity = Gravity.RIGHT;
        textView11.setLayoutParams(textParams11);
        if (data.child("MinimumFare").getValue() != null && data.child("MaximumFare").getValue() != null){
            textView11.setText("Php. "+data.child("MinimumFare").getValue().toString()+"-"+data.child("MaximumFare").getValue().toString());
        }
        textView11.setTypeface(Typeface.DEFAULT_BOLD);
        textView11.setTextColor(NavBarActivity.sContext.getResources().getColor(R.color.colorYellow));
        textView11.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) 15.5);

        linearLayout1.addView(textView1);
        relativeLayout1.addView(linearLayout1);

        linearLayout3.addView(textView2);

        linearLayout4.addView(textView3);
        linearLayout4.addView(textView4);

        linearLayout5.addView(textView5);
        linearLayout5.addView(textView6);

        linearLayout6.addView(textView7);
        linearLayout6.addView(textView8);

        linearLayout7.addView(textView9);
        linearLayout7.addView(textView10);

        linearLayout8.addView(textView11);

        linearLayout2.addView(linearLayout3);
        linearLayout2.addView(relativeLayout2);
        linearLayout2.addView(linearLayout4);
        linearLayout2.addView(linearLayout5);
        linearLayout2.addView(linearLayout6);
        linearLayout2.addView(linearLayout7);
        linearLayout2.addView(linearLayout8);

        cardView.addView(relativeLayout1);
        cardView.addView(linearLayout2);
        gridView.addView(cardView);

        str[x] = data.getKey().toString();
        textView1.setOnClickListener(RoomFragment.this);
    }

    public int dp(int number){
        DisplayMetrics displayMetrics = NavBarActivity.sContext.getResources().getDisplayMetrics();
        return (int)((number * displayMetrics.density) + 0.5);
    }

    @Override
    public void onClick(View v) {
        Integer i = v.getId();
        AddMember addMember = new AddMember();
        addMember.add(str[i], null);
        NavBarActivity.roomId = str[i];
        NavBarActivity.roomStatus = "no";
        NavBarActivity.bottomNav.getMenu().getItem(1).setChecked(true);
        NavBarActivity.bottomNav.setSelectedItemId(R.id.nav_room);
    }

    public Double convert(Double num){
        Double ret = (int) Math.round(num*100)/(double)100;
        return ret;
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
                geoLocate();
            }
        }, hours, minutes, false);
        timePickerDialog.show();
    }

    private void geoLocate(){
        progressDialog.show();
        Geocoder geocoder = new Geocoder(NavBarActivity.sContext);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(originString, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address originAddress = list.get(0);

        try{
            list = geocoder.getFromLocationName(destinationString, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address destinationAddress = list.get(0);

        getRoute(originAddress, destinationAddress);
    }

    private void getRoute(Address origin, Address destination){
        Point originPoint = Point.fromLngLat(origin.getLongitude(), origin.getLatitude());
        Point destinationPoint = Point.fromLngLat(destination.getLongitude(), destination.getLatitude());

        NavigationRoute.builder(NavBarActivity.sContext)
                .accessToken("pk.eyJ1IjoiZ29yaWtvIiwiYSI6ImNqbXhlMmU3cDFuc2wzcXM4MmV4aG5reHQifQ.JkqGov_XghkeZ_hmYEH8xg")
                .origin(originPoint)
                .destination(destinationPoint)
                .alternatives(true)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                        for (int x = 0; x < 1 && x < response.body().routes().size(); x++) {
                            DirectionsRoute currentRoute = response.body().routes().get(x);
                            Double distance = currentRoute.distance() / 1000;//in meters
                            Double duration = currentRoute.duration() / 60;//in seconds

                            Double fare = (distance * 13.50) + (duration * 2) + 40;
                            fareFrom = Integer.valueOf(fare.intValue()) - 20;
                            if (fareFrom <= 45) {
                                fareFrom = 45;
                            }
                            fareTo = Integer.valueOf(fare.intValue()) + 20;
                            estimatedTravelTime = Integer.valueOf(duration.intValue());
                        }
                        CreateTravel createTravel = new CreateTravel();
                        createTravel.create(originLatLng,
                                destinationLatLng, originString,
                                destinationString, fareFrom,
                                fareTo, departureHour,
                                departureMinute, estimatedTravelTime);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Toast.makeText(NavBarActivity.sContext, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
