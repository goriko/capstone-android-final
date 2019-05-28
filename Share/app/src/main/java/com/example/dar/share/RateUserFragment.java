package com.example.dar.share;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

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

import java.util.UUID;

@SuppressLint("ValidFragment")
public class RateUserFragment extends Fragment {

    private View rootView;

    private String uId, roomId;

    private TextView textViewName, textViewGender, textViewContact;
    private ImageView imageView;
    private EditText editTextComment;
    private RatingBar ratingBar;
    private Button buttonProceed;

    private DatabaseReference databaseReference, reference;
    private StorageReference storageReference;
    private FirebaseUser user;


    public RateUserFragment(String UId, String RoomId){
        this.uId = UId;
        this.roomId = RoomId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_rate_user, container, false);

        textViewName = (TextView) rootView.findViewById(R.id.textViewName);
        textViewGender = (TextView) rootView.findViewById(R.id.textViewGender);
        textViewContact = (TextView) rootView.findViewById(R.id.textViewContact);
        imageView = (ImageView) rootView.findViewById(R.id.imageView);
        editTextComment = (EditText) rootView.findViewById(R.id.editTextComment);
        ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);
        buttonProceed = (Button) rootView.findViewById(R.id.buttonProceed);

        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uId);
        reference = FirebaseDatabase.getInstance().getReference("travel").child(roomId).child("rated");
        storageReference = FirebaseStorage.getInstance().getReference("profile/" + uId + ".jpg");
        user = FirebaseAuth.getInstance().getCurrentUser();

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

        buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mKey = UUID.randomUUID().toString();
                String temp = editTextComment.getText().toString();
                if (temp.equals("")){
                    temp = null;
                }
                AddRating addRating = new AddRating(user.getUid(), ratingBar.getRating(), temp);
                databaseReference.child("Rating").child(mKey).setValue(addRating);
                Rate rate = new Rate(user.getUid());
                reference.child(uId).push().setValue(rate);
                Fragment fragment = new InsideHistoryFragment(roomId);
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

}
