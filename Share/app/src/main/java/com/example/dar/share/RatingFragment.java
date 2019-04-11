package com.example.dar.share;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;


public class RatingFragment extends Fragment {

    private View rootView;

    private LinearLayout linearLayout;
    private Button buttonProceed;

    private DatabaseReference databaseReference, reference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private Integer x = 0;
    private String[] ID = new String[4];
    private String mKey;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_rating, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference("travel").child(NavBarActivity.roomId);
        reference = FirebaseDatabase.getInstance().getReference("users");
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
        buttonProceed = (Button) rootView.findViewById(R.id.buttonProceed);

        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (!data.getValue().toString().equals(user.getUid())) {
                        LinearLayout linearLayout1;
                        linearLayout1 = new LinearLayout(NavBarActivity.sContext);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        linearLayout1.setLayoutParams(layoutParams);
                        linearLayout1.setOrientation(LinearLayout.VERTICAL);
                        linearLayout1.setPadding(0, 0, 0, dp(10));

                        TextView textView = new TextView(NavBarActivity.sContext);
                        textView.setLayoutParams(layoutParams);
                        reference.child(data.getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                textView.setText(dataSnapshot.child("Fname").getValue().toString() +" "+ dataSnapshot.child("Lname").getValue().toString());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        RatingBar ratingBar = new RatingBar(NavBarActivity.sContext);
                        ratingBar.setId(x);
                        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        ratingBar.setLayoutParams(layoutParams1);
                        ratingBar.setNumStars(5);
                        ratingBar.setRating(3);

                        EditText editText = new EditText(NavBarActivity.sContext);
                        editText.setId(x+5);
                        editText.setLayoutParams(layoutParams);

                        linearLayout1.addView(textView);
                        linearLayout1.addView(ratingBar);
                        linearLayout1.addView(editText);
                        linearLayout.addView(linearLayout1);
                        ID[x] = data.getValue().toString();
                        x++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RatingBar ratingBar;
                EditText editText;
                for (int i = 0; i < x; i++){
                    ratingBar = (RatingBar) linearLayout.findViewById(i);
                    editText = (EditText) linearLayout.findViewById(i+5);
                    String temp = editText.getText().toString();
                    if (temp.equals("")){
                        temp = null;
                    }
                    mKey = UUID.randomUUID().toString();
                    AddRating addRating = new AddRating(user.getUid(), ratingBar.getRating(), temp);
                    reference.child(ID[i]).child("Rating").child(mKey).setValue(addRating);
                }
                reference.child(user.getUid()).child("CurRoom").setValue(0);

                Toast.makeText(NavBarActivity.sContext, "Finished Travel", Toast.LENGTH_LONG).show();

                NavBarActivity.roomId = NavBarActivity.roomStatus = null;
                NavBarActivity.bottomNav.getMenu().getItem(0).setChecked(true);
                NavBarActivity.bottomNav.setSelectedItemId(R.id.nav_travel);
            }
        });

        return rootView;
    }

    public int dp(int number){
        DisplayMetrics displayMetrics = NavBarActivity.sContext.getResources().getDisplayMetrics();
        return (int)((number * displayMetrics.density) + 0.5);
    }
}
