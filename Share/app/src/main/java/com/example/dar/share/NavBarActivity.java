package com.example.dar.share;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NavBarActivity extends AppCompatActivity implements LocationListener {

    public static String roomId = null, roomStatus = null;
    public static Context sContext;
    public static BottomNavigationView bottomNav;
    public static AlarmManager alarmManager_time;
    public static AlarmManager alarmManager_advance;

    public Integer leader = 0, removed = 0, x, ctr = 0;
    private DatabaseReference databaseReference;
    private DatabaseReference reference;
    private FirebaseAuth firebaseAuth;
    public String userid;

    private Location userLocation, destinationLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_PERMISSION_FINE_LOCATION_RESULT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_bar);
        sContext = getApplicationContext();

        firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        userid = user.getUid();

        bottomNav = findViewById(R.id.bottom_navigation);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                reference.child("Location").child("Latitude").setValue(location.getLatitude());
                reference.child("Location").child("Longitude").setValue(location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);

        reference = FirebaseDatabase.getInstance().getReference("users").child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child("CurRoom").getValue().toString().equals("0")){
                    roomId = dataSnapshot.child("CurRoom").getValue().toString();
                    bottomNav.getMenu().getItem(1).setChecked(true);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new InsideRoomFragment(), "InsideRoom").commitAllowingStateLoss();
                }else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new TravelFragment(), "Travel").commitAllowingStateLoss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment selectedFragment = null;
                String tag = "";

                switch (menuItem.getItemId()){
                    case R.id.nav_travel:
                        selectedFragment = new TravelFragment();
                        tag = "Travel";
                        break;
                    case R.id.nav_room:
                        if (NavBarActivity.roomId == null){
                            selectedFragment = new NoRoomFragment();
                            tag = "";
                        }else{
                            selectedFragment = new InsideRoomFragment();
                            tag = "InsideRoom";
                        }
                        break;
                    case R.id.nav_history:
                        selectedFragment = new HistoryFragment();
                        tag = "History";
                        break;
                    case R.id.nav_Profile:
                        selectedFragment = new ProfileFragment();
                        tag = "Profile";
                        break;
                }

                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                        selectedFragment, tag).commitAllowingStateLoss();

                return true;
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentByTag("InsideRoom") != null) {
            if (roomId != null) {
                if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
                    getFragmentManager().popBackStackImmediate();
                    getSupportFragmentManager().popBackStack(getSupportFragmentManager().getBackStackEntryCount() - 1, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else if (roomStatus != null && roomStatus.equals("on going")) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                    builder1.setMessage("Unable to exit room while travel is on going");
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
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                    builder1.setMessage("Are you sure you want to exit this room?");
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag("InsideRoom")).commit();
                                    delete();
                                    roomId = roomStatus = null;
                                    bottomNav.getMenu().getItem(0).setChecked(true);
                                    bottomNav.setSelectedItemId(R.id.nav_travel);
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
            } else {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("Are you sure you want to close the application?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finishAffinity();
                                System.exit(0);
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

        }else if (getSupportFragmentManager().findFragmentByTag("Rating") != null){
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
        }else if(getSupportFragmentManager().getBackStackEntryCount() == 0) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("Are you sure you want to close the application?");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finishAffinity();
                            System.exit(0);
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
        }else{
            getFragmentManager().popBackStackImmediate();
            getSupportFragmentManager().popBackStack(getSupportFragmentManager().getBackStackEntryCount()-1, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public void replaceFragment(Fragment someFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, someFragment);
        transaction.commitAllowingStateLoss();
    }

    //remove user from room in db
    public void delete(){
        databaseReference = FirebaseDatabase.getInstance().getReference("travel").child(roomId);

        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String value = data.getValue().toString();
                    String key = data.getKey().toString();
                    if (value.equals(userid)) {
                        if (key.equals("Leader")) {
                            leader++;
                        }
                        databaseReference.child("users").child(key).removeValue();
                        removed++;
                    }
                }
                if(leader == 1){
                    databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot data: dataSnapshot.getChildren()){
                                if (leader==1){
                                    databaseReference.child("users").child("Leader").setValue(data.getValue().toString());
                                    databaseReference.child("users").child(data.getKey()).removeValue();
                                    leader=0;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
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
                    if(data.child("CompanionId").getValue().toString().equals(userid)){
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
                if(x == 0){
                    databaseReference.removeValue();
                }else{
                    databaseReference.child("Available").setValue(1);
                    databaseReference.child("NoOfUsers").setValue(x);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reference.child("CurRoom").setValue("0");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Intent intent_time = new Intent(sContext, NotificationTime.class);
        PendingIntent pendingIntent_time = PendingIntent.getBroadcast(sContext, 1, intent_time, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager_time.cancel(pendingIntent_time);

        Intent intent_advance = new Intent(sContext, NotificationAdvance.class);
        PendingIntent pendingIntent_advance = PendingIntent.getBroadcast(sContext, 1, intent_advance, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager_advance.cancel(pendingIntent_advance);

        x=removed=0;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getExtras().get("status").toString().equals("start")){
            roomStatus = "start";
        }

        NavBarActivity.bottomNav.getMenu().getItem(1).setChecked(true);
        NavBarActivity.bottomNav.setSelectedItemId(R.id.nav_room);
    }

    public void tracker(){
        destinationLocation = new Location("");

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("travel").child(NavBarActivity.roomId);
        databaseRef.child("Destination").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                destinationLocation.setLongitude(Double.parseDouble(dataSnapshot.child("longitude").getValue().toString()));
                destinationLocation.setLatitude(Double.parseDouble(dataSnapshot.child("latitude").getValue().toString()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final Handler handler = new Handler();
        final int delay = 5000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(sContext.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION )==
                            PackageManager.PERMISSION_GRANTED){
                        getLocation();
                    }else{
                        if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
                            Toast.makeText(sContext.getApplicationContext(), "Application required to access location", Toast.LENGTH_SHORT).show();
                        }
                        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_FINE_LOCATION_RESULT);
                    }
                }else{
                    getLocation();
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    void getLocation(){
        try{
            locationManager = (LocationManager)sContext.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        userLocation = location;

        if(userLocation.distanceTo(destinationLocation) <= 4000){
            if(ctr == 0){
                Vibrator vibrator = (Vibrator) sContext.getSystemService(sContext.VIBRATOR_SERVICE);
                vibrator.vibrate(2000);

                PowerManager powerManager = (PowerManager) sContext.getSystemService(Context.POWER_SERVICE);
                @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Tag");
                wakeLock.acquire();
                wakeLock.release();

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(NavBarActivity.sContext, "notify_001");
                Intent ii = new Intent(sContext.getApplicationContext(), TravelPinActivity.class);
                ii.putExtra("id", roomId);
                ii.putExtra("status", "reached destination");
                ii.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(sContext, 1, ii, PendingIntent.FLAG_CANCEL_CURRENT);

                mBuilder.setContentIntent(pendingIntent);
                mBuilder.setSmallIcon(R.mipmap.ic_launcher);
                mBuilder.setContentTitle("Almost at the destination");
                mBuilder.setContentText("Finish your travel now");
                mBuilder.setPriority(Notification.PRIORITY_MAX);
                mBuilder.setAutoCancel(true);

                NotificationManager mNotificationManager =
                        (NotificationManager) sContext.getSystemService(Context.NOTIFICATION_SERVICE);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("notify_001",
                            "Channel human readable title",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    mNotificationManager.createNotificationChannel(channel);
                }

                mNotificationManager.notify(0, mBuilder.build());
                ctr++;
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(NavBarActivity.sContext, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_FINE_LOCATION_RESULT){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(), "Application will not run without location permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
