package com.example.dar.share;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
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
public class ViewProfileFragment extends Fragment {

    private String Id;
    private View rootView;

    private TextView textViewName, textViewGender, textViewContact, textViewRating;
    private ImageView imageView;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    public ViewProfileFragment(String x){
        this.Id = x;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_view_profile, container, false);

        textViewName = (TextView) rootView.findViewById(R.id.textViewName);
        textViewGender = (TextView) rootView.findViewById(R.id.textViewGender);
        textViewContact = (TextView) rootView.findViewById(R.id.textViewContact);
        textViewRating = (TextView) rootView.findViewById(R.id.textViewRating);
        imageView = (ImageView) rootView.findViewById(R.id.imageView);

        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(Id);
        storageReference = FirebaseStorage.getInstance().getReference("profile/" + Id + ".jpg");

        databaseReference.child("Rating").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int x = 0;
                float temp = 0;
                for (DataSnapshot data: dataSnapshot.getChildren()){
                    temp = temp + Float.parseFloat(data.child("Rating").getValue().toString());
                    x++;
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

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                textViewName.setText(dataSnapshot.child("Fname").getValue().toString()+" "+dataSnapshot.child("Lname").getValue().toString());
                textViewGender.setText(dataSnapshot.child("Gender").getValue().toString());
                textViewContact.setText(dataSnapshot.child("ContactNumber").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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

        return rootView;
    }

}
