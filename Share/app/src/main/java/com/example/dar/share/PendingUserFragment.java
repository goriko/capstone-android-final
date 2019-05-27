package com.example.dar.share;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PendingUserFragment extends Fragment {

    private DatabaseReference databaseReference;
    private FirebaseUser user;
    private int x = 0, i = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        databaseReference = FirebaseDatabase.getInstance().getReference("travel").child(NavBarActivity.roomId);
        user = FirebaseAuth.getInstance().getCurrentUser();

        databaseReference.child("pendingusers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot data: dataSnapshot.getChildren()){
                        if (data.child("UserId").getValue().toString().equals(user.getUid())){
                            x = 1;
                        }
                    }

                    if (x == 0){
                        check();
                    }
                }else{
                    check();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return inflater.inflate(R.layout.fragment_pending_user, container, false);
    }

    public void check(){

        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {

                databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot data: dataSnapshot.getChildren()){
                            if (data.getValue().toString().equals(user.getUid())){
                                i = 1;
                            }
                        }

                        if (i == 0){
                            NavBarActivity.roomId = NavBarActivity.roomStatus = null;
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                            ref.child("CurRoom").setValue("0");
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                            builder1.setMessage("The Leader decline your request");
                            builder1.setCancelable(false);
                            builder1.setPositiveButton(
                                    "Okay",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            NavBarActivity.bottomNav.getMenu().getItem(0).setChecked(true);
                                            NavBarActivity.bottomNav.setSelectedItemId(R.id.nav_travel);
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                        }else{
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                            builder1.setMessage("The Leader accepted your request");
                            builder1.setCancelable(false);
                            builder1.setPositiveButton(
                                    "Okay",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            NavBarActivity.bottomNav.getMenu().getItem(2).setChecked(true);
                                            NavBarActivity.bottomNav.setSelectedItemId(R.id.nav_room);
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }.start();


    }
}

