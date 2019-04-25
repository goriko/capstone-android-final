package com.example.dar.share;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressLint("ValidFragment")
public class InsideRoomFragment extends Fragment implements View.OnClickListener{

    private View rootView;

    private DatabaseReference databaseReference, ref;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private ImageView imageLeader;
    private TextView textViewLeader;
    private LinearLayout linearLayoutUsers, linearLayoutGuests;
    private CardView cardViewGuest, cardViewMessage, cardViewDetails, cardViewTravel;

    private String origin, destination, stringTime, travelTime, fare;
    private String[] ID = new String[4];
    private Integer leader = 0, x = 0, removed = 0, kick = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_inside_room, container, false);

        cardViewGuest = (CardView) rootView.findViewById(R.id.cardViewGuest);
        cardViewDetails = (CardView) rootView.findViewById(R.id.cardViewDetails);
        cardViewMessage = (CardView) rootView.findViewById(R.id.cardViewMessage);
        cardViewTravel = (CardView) rootView.findViewById(R.id.cardViewTravel);
        imageLeader = (ImageView) rootView.findViewById(R.id.imageLeader);
        textViewLeader = (TextView) rootView.findViewById(R.id.textViewLeader);
        linearLayoutUsers = (LinearLayout) rootView.findViewById(R.id.linearLayoutUsers);
        linearLayoutGuests = (LinearLayout) rootView.findViewById(R.id.linearLayoutGuests);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("travel").child(NavBarActivity.roomId);
        ref = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference("profile/");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null){
                    if (dataSnapshot.child("NoOfUsers").getValue().toString().equals("4") || !dataSnapshot.child("Available").getValue().toString().equals("1")){
                        cardViewGuest.setVisibility(View.GONE);
                    }else{
                        cardViewGuest.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (NavBarActivity.roomStatus != null){
            if (NavBarActivity.roomStatus.equals("start")){
                databaseReference.child("users").child("Leader").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue().toString().equals(user.getUid().toString())){
                            cardViewTravel.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                stopAlarm();
            }else if (NavBarActivity.roomStatus.equals("on going")){
                cardViewTravel.setVisibility(View.GONE);
            }else if (NavBarActivity.roomStatus.equals("reached destination")){
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new RatingFragment(), "Rating").commitAllowingStateLoss();
            }
        }

        databaseReference.child("Available").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null){
                    if (dataSnapshot.getValue().toString().equals("0")){
                        NavBarActivity.roomStatus = "on going";
                        cardViewGuest.setVisibility(View.GONE);
                        cardViewTravel.setVisibility(View.GONE);
                        NavBarActivity navBarActivity = new NavBarActivity();
                        navBarActivity.tracker();
                    }else if (dataSnapshot.getValue().toString().equals("2")){
                        NavBarActivity.roomStatus = "reached destination";
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String time = dataSnapshot.child("DepartureTime").child("DepartureHour").getValue().toString() + ":" + dataSnapshot.child("DepartureTime").child("DepartureMinute").getValue().toString();
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
                fare = dataSnapshot.child("MinimumFare").getValue().toString() + " - " + dataSnapshot.child("MaximumFare").getValue().toString();

                alarm(Integer.valueOf(dataSnapshot.child("DepartureTime").child("DepartureHour").getValue().toString()), Integer.valueOf(dataSnapshot.child("DepartureTime").child("DepartureMinute").getValue().toString()));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                linearLayoutUsers.removeAllViews();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.getValue().toString().equals(user.getUid())){
                        kick++;
                    }
                    if (data.getKey().equals("Leader")) {
                        final long ONE_MEGABYTE = 1024 * 1024 * 5;
                        storageReference.child(data.getValue().toString()+".jpg").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(NavBarActivity.sContext.getResources(), bm);
                                roundDrawable.setCircular(true);

                                imageLeader.setImageDrawable(roundDrawable);
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
                                    textViewLeader.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Fragment fragment = new ViewProfileFragment(data.getValue().toString());
                                            replaceFragment(fragment);
                                        }
                                    });
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }else{
                        ref.child(data.getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                        RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(NavBarActivity.sContext.getResources(), bm);
                                        roundDrawable.setCircular(true);

                                        imageView.setImageDrawable(roundDrawable);
                                    }
                                });

                                TextView textView = new TextView(NavBarActivity.sContext);
                                LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                textView.setLayoutParams(layoutParams1);
                                textView.setTextColor(NavBarActivity.sContext.getResources().getColor(R.color.colorBlack));
                                textView.setPadding(dp(10), dp(10), 0, 0);
                                if (data.getValue().toString().equals(user.getUid())){
                                    textView.setText(dataSnapshot.child("Fname").getValue().toString() + " " +dataSnapshot.child("Lname").getValue().toString()+" (You)");
                                }else{
                                    textView.setText(dataSnapshot.child("Fname").getValue().toString() + " " +dataSnapshot.child("Lname").getValue().toString());
                                    textView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Fragment fragment = new ViewProfileFragment(data.getValue().toString());
                                            replaceFragment(fragment);
                                        }
                                    });
                                }

                                linearLayout.addView(imageView);
                                linearLayout.addView(textView);

                                if (leader == 1 && NavBarActivity.roomStatus == null){
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

                if (kick == 0){
                    promptKick();
                }
                kick = 0;
            }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        databaseReference.child("Guests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                linearLayoutGuests.removeAllViews();
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
                    textView.setTextColor(NavBarActivity.sContext.getResources().getColor(R.color.colorBlack));

                    TextView textView2 = new TextView(NavBarActivity.sContext);
                    textView2.setLayoutParams(layoutParams2);
                    textView2.setTextColor(NavBarActivity.sContext.getResources().getColor(R.color.colorBlack));

                    ref.child(data.child("CompanionId").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            textView2.setText("Guest (with: " + dataSnapshot.child("Fname").getValue().toString() + " " + dataSnapshot.child("Lname").getValue().toString() + ")");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                    linearLayout1.addView(textView);
                    linearLayout1.addView(textView2);

                    linearLayout.addView(imageView);
                    linearLayout.addView(linearLayout1);

                    linearLayoutGuests.addView(linearLayout);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

        cardViewGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addGuest();
            }
        });

        cardViewDetails.setOnClickListener(new View.OnClickListener() {
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

            }
        });

        cardViewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new RoomMessagesFragment();
                replaceFragment(fragment);
            }
        });

        cardViewTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new TakePicActivity();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragmentContainer, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
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

        ref.child(ID[num]).child("CurRoom").setValue("0");

        removed=0;
    }

    private void promptKick(){
        if(InsideRoomFragment.this.getContext() != null){
            NavBarActivity.roomId = NavBarActivity.roomStatus = null;
            AlertDialog.Builder builder1 = new AlertDialog.Builder(InsideRoomFragment.this.getContext());
            builder1.setMessage("Are you sure you want to close the application?");
            builder1.setCancelable(false);

            builder1.setPositiveButton(
                    "Okay",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            NavBarActivity.bottomNav.getMenu().getItem(0).setChecked(true);
                            NavBarActivity.bottomNav.setSelectedItemId(R.id.nav_travel);
                        }
                    });

            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
    }

    public void alarm(int departureHour, int departureMinute) {
        NavBarActivity.alarmManager_time = (AlarmManager) NavBarActivity.sContext.getSystemService(Context.ALARM_SERVICE);
        NavBarActivity.alarmManager_advance = (AlarmManager) NavBarActivity.sContext.getSystemService(Context.ALARM_SERVICE);

        Date date = new Date();

        Calendar alarm_time = Calendar.getInstance();
        Calendar alarm_advance = Calendar.getInstance();
        Calendar cal_now = Calendar.getInstance();

        alarm_time.setTime(date);
        alarm_advance.setTime(date);
        cal_now.setTime(date);

        alarm_time.set(Calendar.HOUR_OF_DAY, departureHour);
        alarm_time.set(Calendar.MINUTE, departureMinute);
        alarm_time.set(Calendar.SECOND, 0);

        if (departureMinute >= 3) {
            alarm_advance.set(Calendar.HOUR_OF_DAY, departureHour);
            alarm_advance.set(Calendar.MINUTE, departureMinute - 3);
            alarm_advance.set(Calendar.SECOND, 0);
        } else {
            int i = 3 - departureMinute;
            alarm_advance.set(Calendar.HOUR_OF_DAY, departureHour - 1);
            alarm_advance.set(Calendar.MINUTE, 60 - i);
            alarm_advance.set(Calendar.SECOND, 0);
        }

        if (alarm_time.before(cal_now)) {
            alarm_time.add(Calendar.DATE, 1);
        }

        Intent intent_time = new Intent(NavBarActivity.sContext, NotificationTime.class);
        intent_time.putExtra("id", NavBarActivity.roomId);
        PendingIntent pendingIntent_time = PendingIntent.getBroadcast(NavBarActivity.sContext, 1, intent_time, 0);
        ((NavBarActivity)this.getActivity()).alarmManager_time.set(AlarmManager.RTC_WAKEUP, alarm_time.getTimeInMillis(), pendingIntent_time);

        if (alarm_advance.before(cal_now)) {
            alarm_advance.add(Calendar.DATE, 1);
        }

        Intent intent_advance = new Intent(NavBarActivity.sContext, NotificationAdvance.class);
        intent_advance.putExtra("id", NavBarActivity.roomId);
        PendingIntent pendingIntent_advance = PendingIntent.getBroadcast(NavBarActivity.sContext, 24444, intent_advance, 0);
        ((NavBarActivity)this.getActivity()).alarmManager_advance.set(AlarmManager.RTC_WAKEUP, alarm_advance.getTimeInMillis(), pendingIntent_advance);
    }

    public void stopAlarm(){
        Intent intent_time = new Intent(NavBarActivity.sContext, NotificationTime.class);
        PendingIntent pendingIntent_time = PendingIntent.getBroadcast(NavBarActivity.sContext, 1, intent_time, PendingIntent.FLAG_CANCEL_CURRENT);
        NavBarActivity.alarmManager_time.cancel(pendingIntent_time);

        Intent intent_advance = new Intent(NavBarActivity.sContext, NotificationAdvance.class);
        PendingIntent pendingIntent_advance = PendingIntent.getBroadcast(NavBarActivity.sContext, 1, intent_advance, PendingIntent.FLAG_CANCEL_CURRENT);
        NavBarActivity.alarmManager_advance.cancel(pendingIntent_advance);
    }

}
