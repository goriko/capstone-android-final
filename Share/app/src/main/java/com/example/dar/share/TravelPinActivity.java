package com.example.dar.share;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.goodiebag.pinview.Pinview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TravelPinActivity extends AppCompatActivity {

    public int i=0, ndx;
    private String pin, message, name, origin, destination, latitude, longitude, to;
    public String[] users = new String[4];
    public String[] guest = new String[4];

    private TextView textViewPin;
    private Pinview pinView;
    private Button buttonProceed;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference, reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_pin);

        databaseReference = FirebaseDatabase.getInstance().getReference("travel").child(NavBarActivity.roomId);
        reference = FirebaseDatabase.getInstance().getReference("users");

        databaseReference.child("Available").setValue("2");

        NavBarActivity.roomStatus = "reached destination";

        firebaseAuth = FirebaseAuth.getInstance();

        reference.child(firebaseAuth.getUid().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("Fname").getValue().toString()+" "+dataSnapshot.child("Lname").getValue().toString();
                latitude = dataSnapshot.child("Location").child("Latitude").getValue().toString();
                longitude = dataSnapshot.child("Location").child("Longitude").getValue().toString();
                to = dataSnapshot.child("EmergencyContact").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                origin = dataSnapshot.child("OriginString").getValue().toString();
                destination = dataSnapshot.child("DestinationString").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ndx = 0;
                for (DataSnapshot data:dataSnapshot.getChildren()){
                    if (!data.getValue().toString().equals(firebaseAuth.getUid())){
                        getMember(ndx, data.getValue().toString());
                        ndx++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("Guests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ndx = 0;
                for (DataSnapshot data:dataSnapshot.getChildren()){
                    guest[ndx] = data.child("Name").getValue().toString();
                    getGuests(ndx, data.child("CompanionId").getValue().toString());
                    ndx++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        textViewPin = (TextView) findViewById(R.id.textViewPin);
        pinView = (Pinview) findViewById(R.id.pinView);
        buttonProceed = (Button) findViewById(R.id.buttonProceed);

        buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (i<3){
                    pin = pinView.getValue().toString();
                    reference.child(firebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.child("Pin").getValue().toString().equals(pin)){
                                startActivity(new Intent(getApplicationContext(), NavBarActivity.class));
                            }else{
                                textViewPin.setText("Invalid Pin number. Please try again");
                                i++;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }else{
                    sendSMS();
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(TravelPinActivity.this);
                    builder1.setMessage("Failed to enter the correct pin. A message has been sent to the guardian informing of the situation");
                    builder1.setCancelable(false);

                    builder1.setPositiveButton(
                            "Okay",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    startActivity(new Intent(getApplicationContext(), NavBarActivity.class));
                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Please finish this step to complete travel");
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
    }

    public void sendSMS(){
        message ="\nSHARE" +
                "\n"+name+" has entered an invalid Pin. Room details are as follows:" +
                "\nOrigin: " + origin+
                "\nDestination: " + destination+
                "\n" +
                "\nMEMBERS:";
        for (ndx = 0; ndx<4 && users[ndx]!=null; ndx++){
            message = message+"\n"+users[ndx];
        }
        for (ndx = 0; ndx<4 && guest[ndx]!=null; ndx++){
            message = message+"\n"+guest[ndx];
        }
        message = message+"\n" +
                "\nLast seen:" +
                "\nLatitude: " +latitude+
                "\nLongitude: " +longitude+
                "\n" +
                "\nPlease check on the user and make sure of his safety." +
                "\nDo not reply to this message.";

        Log.d("EYY", message);

        OkHttpClient client = new OkHttpClient();
        String url = "https://api.twilio.com/2010-04-01/Accounts/AC7f18475068710bc061de9206a00557c4/SMS/Messages";
        String base64EncodedCredentials = "Basic " + Base64.encodeToString(("AC7f18475068710bc061de9206a00557c4:4a1950c51a6157041df51c7c1578236d").getBytes(), Base64.NO_WRAP);



        RequestBody body = new FormBody.Builder()
                .add("From", "+14805089792")
                .add("To", to)
                .add("Body", message)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", base64EncodedCredentials)
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d("EYY", "sendSms: "+ response.body().string());
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void getMember(int i, String data){
        reference.child(data).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users[i] = dataSnapshot.child("Fname").getValue().toString()+" "+dataSnapshot.child("Lname").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getGuests(int i, String data){
        reference.child(data).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                guest[i] = guest[i]+" (with: "+dataSnapshot.child("Fname").getValue().toString()+" "+dataSnapshot.child("Lname").getValue().toString()+")";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
