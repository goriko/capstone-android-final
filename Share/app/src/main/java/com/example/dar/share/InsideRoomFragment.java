package com.example.dar.share;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@SuppressLint("ValidFragment")
public class InsideRoomFragment extends Fragment {

    private View rootView;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private Button buttonGuest, buttonSend, buttonDetails;
    private EditText editTextMessage;

    private String origin, destination, stringTime, travelTime, fare;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_inside_room, container, false);

        TextView textView = (TextView) rootView.findViewById(R.id.textView);
        buttonGuest = (Button) rootView.findViewById(R.id.buttonGuest);
        buttonSend = (Button) rootView.findViewById(R.id.buttonSend);
        buttonDetails = (Button) rootView.findViewById(R.id.buttonDetails);
        editTextMessage = (EditText) rootView.findViewById(R.id.editTextMessage);

        if (NavBarActivity.roomId == null){
            textView.setText("OUTSIDE");
            buttonGuest.setVisibility(View.GONE);
            buttonSend.setVisibility(View.GONE);
            buttonDetails.setVisibility(View.GONE);
            editTextMessage.setVisibility(View.GONE);
        }else {
            textView.setText(NavBarActivity.roomId);
            databaseReference = FirebaseDatabase.getInstance().getReference("travel").child(NavBarActivity.roomId);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String time = dataSnapshot.child("DepartureTime").child("DepartureHour").getValue().toString()+":"+dataSnapshot.child("DepartureTime").child("DepartureMinute").getValue().toString();
                    SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm");
                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("hh:mm a");
                    Date date = new Date();
                    try {
                        date = dateFormat1.parse(time);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    stringTime = dateFormat2.format(date);
                    origin = dataSnapshot.child("OriginString").getValue().toString();
                    destination = dataSnapshot.child("DestinationString").getValue().toString();
                    travelTime = dataSnapshot.child("EstimatedTravelTime").getValue().toString();
                    fare = dataSnapshot.child("MinimumFare").getValue().toString() +" - "+ dataSnapshot.child("MaximumFare").getValue().toString();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            firebaseAuth = FirebaseAuth.getInstance();
            user = firebaseAuth.getCurrentUser();
        }

        buttonGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addGuest();
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMessage();
            }
        });

        buttonDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setTitle("Travel Details");
                builder1.setMessage("Departure time: "+stringTime+"\n"+
                                    "Origin: "+origin+"\n"+
                                    "Destination: "+destination+"\n"+
                                    "Travel Time: "+travelTime+" minute(s)\n"+
                                    "Fare: Php. "+fare);
                builder1.setCancelable(true);
                builder1.setPositiveButton(
                        "Okay",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();

                Log.d("EYY", origin);

            }
        });

        return rootView;
    }

    private void addMessage(){
        String str = editTextMessage.getText().toString();
        Message m = new Message(str, user.getUid().toString());
        Date currentTime = Calendar.getInstance().getTime();
        long key = currentTime.getTime();
        // convert to string
        // String dateString = DateFormat.format("yyyy-MM-dd hh:mm:ss a", new Date(key)).toString();
        databaseReference.child("messages").child(Long.toString(key)).setValue(m);
        editTextMessage.setText("");
        InputMethodManager imm = (InputMethodManager) NavBarActivity.sContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
    }

    private void addGuest(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setMessage("Enter guest name");

        EditText input = new EditText(NavBarActivity.sContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AddMember guest = new AddMember();
                guest.add(NavBarActivity.roomId, input.getText().toString());
            }
        });

        alertDialog.show();
    }

    public void replaceFragment(Fragment someFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, someFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
