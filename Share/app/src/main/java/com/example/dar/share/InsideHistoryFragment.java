package com.example.dar.share;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCanceledListener;
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
import java.util.Date;

@SuppressLint("ValidFragment")
public class InsideHistoryFragment extends Fragment {

    private View rootView;

    private String Id;
    private Integer temp = 0;

    private TextView textViewOrigin, textViewDestination, textViewDeparture, textViewTime, textViewFare, textViewPlateNum, textViewNum, textViewOperator;
    private ImageView imageView;
    private LinearLayout linearLayout;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private ProgressDialog progressDialog;

    public InsideHistoryFragment(String id){
        this.Id = id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_inside_history, container, false);

        progressDialog = new ProgressDialog(this.getContext());
        progressDialog.setMessage("Loading ....");
        progressDialog.setCancelable(false);
        progressDialog.show();

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
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
                String time = dataSnapshot.child("DepartureTime").child("DepartureHour").getValue().toString()+":"+dataSnapshot.child("DepartureTime").child("DepartureMinute").getValue().toString();
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm");
                SimpleDateFormat dateFormat2 = new SimpleDateFormat("hh:mm a");
                Date date = new Date();
                try {
                    date = dateFormat1.parse(time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String stringTime = dateFormat2.format(date);
                textViewDeparture.setText(stringTime);
                textViewTime.setText(dataSnapshot.child("EstimatedTravelTime").getValue().toString() + " minute(s)");
                textViewFare.setText("PHP "+dataSnapshot.child("MinimumFare").getValue().toString()+"-"+dataSnapshot.child("MaximumFare").getValue().toString());
                textViewPlateNum.setText(dataSnapshot.child("taxi").child("PlateNumber").getValue().toString());
                textViewNum.setText(dataSnapshot.child("taxi").child("TaxiNumber").getValue().toString());
                textViewOperator.setText(dataSnapshot.child("taxi").child("Operator").getValue().toString());

                for(DataSnapshot data : dataSnapshot.child("users").getChildren()){
                    for (DataSnapshot d: dataSnapshot.child("rated").getChildren()){
                        if (d.getKey().toString().equals(data.getValue().toString())){
                            for (DataSnapshot da: dataSnapshot.child("rated").child(d.getKey().toString()).getChildren()){
                                if (da.child("UserId").getValue().toString().equals(user.getUid())){
                                    temp = 1;
                                }
                            }
                        }
                    }

                    LinearLayout linearLayout1 = new LinearLayout(NavBarActivity.sContext);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    linearLayout1.setLayoutParams(layoutParams);
                    linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
                    linearLayout1.setPadding(0, 0, 0, dp(10));

                    DatabaseReference ref;
                    ref = FirebaseDatabase.getInstance().getReference("users").child(data.getValue().toString());
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ImageView imageView = new ImageView(NavBarActivity.sContext);
                            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(dp(36), dp(36));
                            imageView.setLayoutParams(layoutParams2);
                            imageView.setPadding(dp(10), 0, 0, 0);

                            StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile/");
                            final long ONE_MEGABYTE = 1024 * 1024 * 5;
                            storageRef.child(data.getValue().toString()+".jpg").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
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
                            textView.setPadding(dp(10), dp(10), 0, 0);
                            textView.setTextColor(NavBarActivity.sContext.getResources().getColor(R.color.colorBlack));
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

                            linearLayout1.addView(imageView);
                            linearLayout1.addView(textView);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });

                    if (temp == 0){
                        if (!data.getValue().toString().equals(user.getUid())){
                            Button button = new Button(NavBarActivity.sContext);
                            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            button.setLayoutParams(layoutParams3);
                            button.setText("RATE");
                            //button.setId(x);
                            //ID[x] = data.getValue().toString();
                            //button.setOnClickListener(InsideRoomFragment.this);
                            linearLayout1.addView(button);
                            //x++;
                        }
                    }else{
                        temp = 0;
                    }

                    linearLayout.addView(linearLayout1);
                }

                for (DataSnapshot data : dataSnapshot.child("Guests").getChildren()) {
                    DatabaseReference ref;
                    ref = FirebaseDatabase.getInstance().getReference("users");
                    LinearLayout linearLayout2;
                    linearLayout2 = new LinearLayout(NavBarActivity.sContext);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    linearLayout2.setLayoutParams(layoutParams);
                    linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
                    linearLayout2.setPadding(0, 0,0, dp(10));

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

                    linearLayout2.addView(imageView);
                    linearLayout2.addView(linearLayout1);

                    linearLayout.addView(linearLayout2);
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
                        progressDialog.dismiss();
                    }
                });

        return rootView;
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

}
