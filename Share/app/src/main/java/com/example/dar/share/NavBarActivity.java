package com.example.dar.share;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NavBarActivity extends AppCompatActivity {

    public static String roomId = null, roomStatus = null;
    public static Context sContext;
    public static BottomNavigationView bottomNav;

    public Integer leader = 0, removed = 0, x;
    private DatabaseReference databaseReference;
    private DatabaseReference reference;
    private FirebaseAuth firebaseAuth;
    public String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_bar);
        sContext = getApplicationContext();

        firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        userid = user.getUid();

        bottomNav = findViewById(R.id.bottom_navigation);

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
        if (getSupportFragmentManager().findFragmentByTag("InsideRoom") != null){
            if (roomId != null){
                if (getSupportFragmentManager().getBackStackEntryCount() != 0){
                    getFragmentManager().popBackStackImmediate();
                    getSupportFragmentManager().popBackStack(getSupportFragmentManager().getBackStackEntryCount()-1, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }else{
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                    builder1.setMessage("Are you sure you want to exit this room?");
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
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
            }else{
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
        } else{
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

        /*Intent intent_time = new Intent(sContext, NotificationTime.class);
        PendingIntent pendingIntent_time = PendingIntent.getBroadcast(sContext, 1, intent_time, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager_time.cancel(pendingIntent_time);

        Intent intent_advance = new Intent(sContext, NotificationAdvance.class);
        PendingIntent pendingIntent_advance = PendingIntent.getBroadcast(sContext, 1, intent_advance, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager_advance.cancel(pendingIntent_advance);*/

        x=removed=0;
    }
}
