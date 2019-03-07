package com.example.dar.share;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RoomDetailsFragment extends Fragment {

    private View rootView;

    private TextView textViewTime, textViewOrigin, textViewDestination, textViewTravelTime, textViewFare;

    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_room_details, container, false);

        textViewTime = (TextView) rootView.findViewById(R.id.textViewTime);
        textViewOrigin = (TextView) rootView.findViewById(R.id.textViewOrigin);
        textViewDestination = (TextView) rootView.findViewById(R.id.textViewDestination);
        textViewTravelTime = (TextView) rootView.findViewById(R.id.textViewTravelTime);
        textViewFare = (TextView) rootView.findViewById(R.id.textViewFare);

        databaseReference = FirebaseDatabase.getInstance().getReference("travel").child(NavBarActivity.roomId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String sign = "am";
                int hour, minute;
                hour = Integer.parseInt(dataSnapshot.child("DepartureTime").child("DepartureHour").getValue().toString());
                minute = Integer.parseInt(dataSnapshot.child("DepartureTime").child("DepartureMinute").getValue().toString());
                if (hour > 12){
                    hour = hour - 12;
                    sign = "pm";
                }
                textViewTime.setText(hour +":"+ minute + " " +sign);
                textViewOrigin.setText(dataSnapshot.child("OriginString").getValue().toString());
                textViewDestination.setText(dataSnapshot.child("DestinationString").getValue().toString());
                textViewTravelTime.setText(dataSnapshot.child("EstimatedTravelTime").getValue().toString() + " minute(s)");
                textViewFare.setText(dataSnapshot.child("MinimumFare").getValue().toString() + " - " + dataSnapshot.child("MaximumFare").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return rootView;
    }

}
