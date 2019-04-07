package com.example.dar.share;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
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

public class ProfileFragment extends Fragment {

    private View rootView;

    private TextView textViewName, textViewEmail, textViewRating;
    private Button buttonSettings;
    private ImageView imageView;
    private GridLayout gridLayout;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference, reference;
    private StorageReference storageReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid().toString());
        reference = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference("profile/" + user.getUid().toString() + ".jpg");

        textViewName = (TextView) rootView.findViewById(R.id.textViewName);
        textViewEmail = (TextView) rootView.findViewById(R.id.textViewEmail);
        textViewRating = (TextView) rootView.findViewById(R.id.rating);
        buttonSettings = (Button) rootView.findViewById(R.id.buttonSettings);
        imageView = (ImageView) rootView.findViewById(R.id.imageView);
        gridLayout = (GridLayout) rootView.findViewById(R.id.gridLayout);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(getActivity(), "Please finish registration", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getActivity(), RegistrationActivity.class));
                } else {
                    String Fname = dataSnapshot.child("Fname").getValue().toString();
                    String Lname = dataSnapshot.child("Lname").getValue().toString();
                    textViewName.setText(Fname + " " + Lname);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("Rating").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int x = 0;
                float temp = 0;
                for (DataSnapshot data: dataSnapshot.getChildren()){
                    temp = temp + Float.parseFloat(data.child("Rating").getValue().toString());
                    x++;

                    if (data.hasChild("Comment")){
                        CardView cardView = new CardView(NavBarActivity.sContext);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        cardView.setLayoutParams(layoutParams);
                        cardView.setUseCompatPadding(true);

                        LinearLayout linearLayout = new LinearLayout(NavBarActivity.sContext);
                        layoutParams.setMargins(0, dp(10), 0, 0);
                        linearLayout.setLayoutParams(layoutParams);

                        ImageView imageView = new ImageView(NavBarActivity.sContext);
                        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(dp(36), dp(36));
                        layoutParams1.setMargins(0, dp(10),0,0);
                        imageView.setLayoutParams(layoutParams1);
                        imageView.setImageResource(R.drawable.person);

                        LinearLayout linearLayout1 = new LinearLayout(NavBarActivity.sContext);
                        linearLayout1.setLayoutParams(layoutParams);
                        linearLayout1.setPadding(dp(20), 0, 0, 0);
                        linearLayout1.setOrientation(LinearLayout.VERTICAL);

                        TextView textView = new TextView(NavBarActivity.sContext);
                        textView.setLayoutParams(layoutParams);
                        textView.setText(data.child("Comment").getValue().toString());

                        TextView textView1 = new TextView(NavBarActivity.sContext);
                        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams2.setMargins(0, dp(10), 0, 0);
                        reference.child(data.child("User").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                textView1.setText(dataSnapshot.child("Fname").getValue().toString()+" "+dataSnapshot.child("Lname").getValue().toString());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        linearLayout1.addView(textView);
                        linearLayout1.addView(textView1);
                        linearLayout.addView(imageView);
                        linearLayout.addView(linearLayout1);
                        cardView.addView(linearLayout);
                        gridLayout.addView(cardView);
                    }

                }
                temp = temp/x;
                if (x!=0){
                    textViewRating.setText(Float.toString(temp));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        textViewEmail.setText(user.getEmail());

        final long ONE_MEGABYTE = 1024 * 1024 * 5;
        storageReference.getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        float aspectRatio = bm.getWidth() /
                                (float) bm.getHeight();

                        int width = 90;
                        int height = Math.round(width / aspectRatio);

                        bm = Bitmap.createScaledBitmap(
                                bm, width, height, false);

                        imageView.setImageBitmap(bm);
                    }
                });

        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new SettingsFragment();
                replaceFragment(fragment);
            }
        });

        return rootView;
    }

    public void replaceFragment(Fragment someFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, someFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public int dp(int number){
        DisplayMetrics displayMetrics = NavBarActivity.sContext.getResources().getDisplayMetrics();
        return (int)((number * displayMetrics.density) + 0.5);
    }

}
