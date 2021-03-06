package com.example.dar.share;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.UUID;

public class CreateTravel {

    public DatabaseReference databaseReference;
    public DatabaseReference ref;
    public FirebaseUser user;
    public String mKey;

    public void create(LatLng origin,
                         LatLng destination,
                         String originString,
                         String destinationString,
                         Integer fareFrom,
                         Integer fareTo,
                         Integer departureHour,
                         Integer departureMinute,
                         Integer estimatedTravelTime){
        databaseReference = FirebaseDatabase.getInstance().getReference();
        ref = FirebaseDatabase.getInstance().getReference("users");
        user = FirebaseAuth.getInstance().getCurrentUser();

        AddTravelInformation addTravelInformation = new AddTravelInformation(origin, destination, originString, destinationString, 1, 1, fareFrom, fareTo, estimatedTravelTime);
        DatabaseReference reference = databaseReference.child("travel").push();
        mKey = reference.getKey();

        databaseReference.child("travel").child(mKey).setValue(addTravelInformation);

        Time time = new Time(departureHour, departureMinute);
        databaseReference.child("travel").child(mKey).child("DepartureTime").setValue(time);

        AddLeaderID addLeaderID = new AddLeaderID(user.getUid().toString());
        databaseReference.child("travel").child(mKey).child("users").setValue(addLeaderID);

        ref.child(user.getUid()).child("CurRoom").setValue(mKey);

        NavBarActivity.roomId =  mKey;
        NavBarActivity.roomStatus = "no";
        NavBarActivity.bottomNav.setSelectedItemId(R.id.nav_room);
    }

}