package com.example.dar.share;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@SuppressLint("ValidFragment")
public class InsideRoomFragment extends Fragment implements View.OnClickListener {

    private View rootView;

    private DatabaseReference databaseReference, ref;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private Button buttonGuest, buttonDetails, buttonMessages;
    private ImageView imageLeader;
    private TextView textViewLeader;
    private LinearLayout linearLayoutUsers;

    private String origin, destination, stringTime, travelTime, fare;
    private String[] ID = new String[4];
    private Integer leader = 0, x = 0, removed = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_inside_room, container, false);

        TextView textView = (TextView) rootView.findViewById(R.id.textView);
        buttonGuest = (Button) rootView.findViewById(R.id.buttonGuest);
        buttonDetails = (Button) rootView.findViewById(R.id.buttonDetails);
        buttonMessages = (Button) rootView.findViewById(R.id.buttonMessages);
        imageLeader = (ImageView) rootView.findViewById(R.id.imageLeader);
        textViewLeader = (TextView) rootView.findViewById(R.id.textViewLeader);
        linearLayoutUsers = (LinearLayout) rootView.findViewById(R.id.linearLayoutUsers);

        if (NavBarActivity.roomId == null){
            textView.setText("OUTSIDE");
            buttonGuest.setVisibility(View.GONE);
            buttonMessages.setVisibility(View.GONE);
            buttonDetails.setVisibility(View.GONE);
            imageLeader.setVisibility(View.GONE);
            textViewLeader.setVisibility(View.GONE);
            linearLayoutUsers.setVisibility(View.GONE);
        }else {
            textView.setText(NavBarActivity.roomId);
            firebaseAuth = FirebaseAuth.getInstance();
            user = firebaseAuth.getCurrentUser();
            databaseReference = FirebaseDatabase.getInstance().getReference("travel").child(NavBarActivity.roomId);
            ref = FirebaseDatabase.getInstance().getReference("users");
            storageReference = FirebaseStorage.getInstance().getReference("profile/");

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

            databaseReference.child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    linearLayoutUsers.removeAllViews();
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.getKey().equals("Leader")) {
                            final long ONE_MEGABYTE = 1024 * 1024 * 5;
                            storageReference.child(data.getValue().toString()+".jpg").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    float aspectRatio = bm.getWidth() / (float) bm.getHeight();

                                    int width = 90;
                                    int height = Math.round(width / aspectRatio);

                                    bm = Bitmap.createScaledBitmap(bm, width, height, false);

                                    imageLeader.setImageBitmap(bm);
                                }
                            });
                            ref.child(data.getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (data.getValue().toString().equals(user.getUid())){
                                        leader = 1;
                                        textViewLeader.setText(dataSnapshot.child("Fname").getValue().toString() + " " +dataSnapshot.child("Lname").getValue().toString()+ " (You)");
                                    }else{
                                        textViewLeader.setText(dataSnapshot.child("Fname").getValue().toString() + " " +dataSnapshot.child("Lname").getValue().toString());
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });
                        }else{
                            ref.child(data.getValue().toString()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    LinearLayout linearLayout = new LinearLayout(NavBarActivity.sContext);
                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    linearLayout.setLayoutParams(layoutParams);
                                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                                    linearLayout.setPadding(0, 0, 0, dp(10));

                                    ImageView imageView = new ImageView(NavBarActivity.sContext);
                                    LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(dp(36), dp(36));
                                    imageView.setLayoutParams(layoutParams2);
                                    imageView.setPadding(dp(10), 0, 0, 0);

                                    final long ONE_MEGABYTE = 1024 * 1024 * 5;
                                    storageReference.child(data.getValue().toString()+".jpg").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            float aspectRatio = bm.getWidth() /(float) bm.getHeight();

                                            int width = 90;
                                            int height = Math.round(width / aspectRatio);

                                            bm = Bitmap.createScaledBitmap(bm, width, height, false);

                                            imageView.setImageBitmap(bm);
                                        }
                                    });

                                    TextView textView = new TextView(NavBarActivity.sContext);
                                    LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    textView.setLayoutParams(layoutParams1);
                                    textView.setPadding(dp(10), dp(10), 0, 0);
                                    if (data.getValue().toString().equals(user.getUid())){
                                        textView.setText(dataSnapshot.child("Fname").getValue().toString() + " " +dataSnapshot.child("Lname").getValue().toString()+" (You)");
                                    }else{
                                        textView.setText(dataSnapshot.child("Fname").getValue().toString() + " " +dataSnapshot.child("Lname").getValue().toString());
                                    }

                                    linearLayout.addView(imageView);
                                    linearLayout.addView(textView);

                                    if (leader == 1){
                                        Button button = new Button(NavBarActivity.sContext);
                                        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                        button.setLayoutParams(layoutParams3);
                                        button.setText("KICK");
                                        button.setId(x);
                                        ID[x] = data.getValue().toString();
                                        button.setOnClickListener(InsideRoomFragment.this);
                                        linearLayout.addView(button);
                                        x++;
                                    }

                                    linearLayoutUsers.addView(linearLayout);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            x = 0;
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
                    for (DataSnapshot data : dataSnapshot.getChildren()){
                        LinearLayout linearLayout;
                        linearLayout = new LinearLayout(NavBarActivity.sContext);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        linearLayout.setLayoutParams(layoutParams);
                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                        linearLayout.setPadding(0, 0,0, dp(10));

                        ImageView imageView = new ImageView(NavBarActivity.sContext);
                        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(dp(36), LinearLayout.LayoutParams.MATCH_PARENT);
                        imageView.setLayoutParams(layoutParams1);
                        imageView.setPadding(dp(10), 0, 0, 0);
                        imageView.setImageDrawable(NavBarActivity.sContext.getResources().getDrawable(R.drawable.ic_user_icon));

                        LinearLayout linearLayout1 = new LinearLayout(NavBarActivity.sContext);
                        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        linearLayout1.setLayoutParams(layoutParams3);
                        linearLayout1.setPadding(dp(10), 0, 0, 0);
                        linearLayout1.setOrientation(LinearLayout.VERTICAL);

                        TextView textView = new TextView(NavBarActivity.sContext);
                        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        textView.setLayoutParams(layoutParams2);
                        textView.setText(data.child("Name").getValue().toString());

                        TextView textView2 = new TextView(NavBarActivity.sContext);
                        textView2.setLayoutParams(layoutParams2);

                        ref.child(data.child("CompanionId").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                textView2.setText("Guest (with: "+dataSnapshot.child("Fname").getValue().toString() + " " +dataSnapshot.child("Lname").getValue().toString()+")");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        linearLayout1.addView(textView);
                        linearLayout1.addView(textView2);

                        linearLayout.addView(imageView);
                        linearLayout.addView(linearLayout1);

                        linearLayoutUsers.addView(linearLayout);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        buttonGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addGuest();
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

        buttonMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new RoomMessagesFragment();
                replaceFragment(fragment);
            }
        });

        return rootView;
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

    public int dp(int number){
        DisplayMetrics displayMetrics = NavBarActivity.sContext.getResources().getDisplayMetrics();
        return (int)((number * displayMetrics.density) + 0.5);
    }

    public void replaceFragment(Fragment someFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, someFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        int num = v.getId();
        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String value = data.getValue().toString();
                    String key = data.getKey().toString();
                    if (value.equals(ID[num])) {
                        databaseReference.child("users").child(key).removeValue();
                        removed++;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){

            }
        });

        databaseReference.child("Guests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    if(data.child("CompanionId").getValue().toString().equals(ID[num])){
                        databaseReference.child("Guests").child(data.getKey().toString()).removeValue();
                        removed++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("NoOfUsers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                x = Integer.valueOf(dataSnapshot.getValue().toString()) - removed;
                databaseReference.child("Available").setValue(1);
                databaseReference.child("NoOfUsers").setValue(x);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        removed=0;
    }
}
