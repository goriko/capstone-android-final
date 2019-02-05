package com.example.dar.share;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

@SuppressLint("ValidFragment")
public class InsideHistoryFragment extends Fragment {

    private View rootView;

    private String Id;

    private TextView textViewOrigin, textViewDestination, textViewDeparture, textViewTime, textViewFare, textViewPlateNum, textViewNum, textViewOperator;
    private ImageView imageView;
    private LinearLayout linearLayout;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    public InsideHistoryFragment(String id){
        this.Id = id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_inside_history, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference("travel").child(Id);
        storageReference = FirebaseStorage.getInstance().getReference("travel/" + Id + ".jpg");

        textViewOrigin = (TextView) rootView.findViewById(R.id.textViewOrigin);
        textViewDestination = (TextView) rootView.findViewById(R.id.textViewDestination);
        textViewDeparture = (TextView) rootView.findViewById(R.id.textViewDeparture);
        textViewTime = (TextView) rootView.findViewById(R.id.textViewTime);
        textViewFare = (TextView) rootView.findViewById(R.id.textViewFare);
        textViewPlateNum = (TextView) rootView.findViewById(R.id.textViewPlateNum);
        textViewNum = (TextView) rootView.findViewById(R.id.textViewNum);
        textViewOperator = (TextView) rootView.findViewById(R.id.textViewOperator);
        linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayoutMembers);
        imageView =(ImageView) rootView.findViewById(R.id.imageView);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                textViewOrigin.setText(dataSnapshot.child("OriginString").getValue().toString());
                textViewDestination.setText(dataSnapshot.child("DestinationString").getValue().toString());
                String hourString = dataSnapshot.child("DepartureTime").child("DepartureHour").getValue().toString();
                int hour = Integer.parseInt(hourString);
                String p;
                if(hour > 12){
                    p = "pm";
                    hour = hour - 12;
                }else{
                    p = "am";
                }
                textViewDeparture.setText(hour+":"+dataSnapshot.child("DepartureTime").child("DepartureMinute").getValue().toString() +" "+p);
                textViewTime.setText(dataSnapshot.child("EstimatedTravelTime").getValue().toString() + " minute(s)");
                textViewFare.setText("PHP "+dataSnapshot.child("MinimumFare").getValue().toString()+"-"+dataSnapshot.child("MaximumFare").getValue().toString());
                textViewPlateNum.setText(dataSnapshot.child("taxi").child("PlateNumber").getValue().toString());
                textViewNum.setText(dataSnapshot.child("taxi").child("TaxiNumber").getValue().toString());
                textViewOperator.setText(dataSnapshot.child("taxi").child("Operator").getValue().toString());

                for(DataSnapshot data : dataSnapshot.child("users").getChildren()){
                    DatabaseReference ref;
                    ref = FirebaseDatabase.getInstance().getReference("users").child(data.getValue().toString());
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String Fname = dataSnapshot.child("Fname").getValue().toString();
                            String Lname = dataSnapshot.child("Lname").getValue().toString();
                            TextView textView1 = new TextView(getActivity().getApplicationContext());
                            textView1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            textView1.setText(Fname + " " + Lname);
                            linearLayout.addView(textView1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }

                for (DataSnapshot data : dataSnapshot.child("Guests").getChildren()) {
                    DatabaseReference ref;
                    ref = FirebaseDatabase.getInstance().getReference("users").child(data.child("UserId").getValue().toString());
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String Fname = dataSnapshot.child("Fname").getValue().toString();
                            String Lname = dataSnapshot.child("Lname").getValue().toString();
                            TextView textView1 = new TextView(getActivity().getApplicationContext());
                            textView1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            textView1.setText(data.child("Name").getValue().toString() + " (With: " + Fname + " " + Lname + ")");
                            linearLayout.addView(textView1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final long ONE_MEGABYTE = 1024 * 1024;
        storageReference.getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        DisplayMetrics dm = new DisplayMetrics();
                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

                        imageView.setImageBitmap(bm);
                    }
                });

        return rootView;
    }

}
