package com.example.dar.share;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.goodiebag.pinview.Pinview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChangePinFragment extends Fragment implements View.OnClickListener{

    private View rootView;

    private Integer click = 1;
    private String truePin, checkPin, newPin, verifyPin;

    private Button buttonProceed, buttonCancel;
    private Pinview pinview;
    private TextView textView;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_change_pin, container, false);
        
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid().toString());

        buttonProceed = (Button) rootView.findViewById(R.id.buttonProceed);
        buttonCancel = (Button) rootView.findViewById(R.id.buttonCancel);
        pinview = (Pinview) rootView.findViewById(R.id.pinView);
        textView = (TextView) rootView.findViewById(R.id.textView);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                truePin = dataSnapshot.child("Pin").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        buttonProceed.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);

        return rootView;
    }

    public void replaceFragment(Fragment someFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, someFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void firstClick(){
        checkPin = pinview.getValue().toString();
        if (TextUtils.isEmpty(checkPin)){
            Toast.makeText(NavBarActivity.sContext, "Please enter your pin number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!checkPin.equals(truePin)){
            Toast.makeText(getActivity(), "Please enter the right pin number", Toast.LENGTH_SHORT).show();
            clearPinViewChild();
            return;
        }

        click++;
        textView.setText("Please enter new Pin Number");
        clearPinViewChild();
    }

    private void secondClick(){
        newPin = pinview.getValue().toString();
        if (TextUtils.isEmpty(newPin)){
            Toast.makeText(NavBarActivity.sContext, "Please enter your pin number", Toast.LENGTH_SHORT).show();
            return;
        }
        click++;
        textView.setText("Please enter new Pin Number again to verify");
        clearPinViewChild();
    }

    private void thirdClick(){
        verifyPin = pinview.getValue().toString();
        if (TextUtils.isEmpty(verifyPin)){
            Toast.makeText(NavBarActivity.sContext, "Please enter your pin number", Toast.LENGTH_SHORT).show();
            return;
        }else if(!newPin.equals(verifyPin)){
            Toast.makeText(getActivity(), "Please enter the right Pin Number", Toast.LENGTH_SHORT).show();
            textView.setText("Please enter new Pin Number");
            clearPinViewChild();
            click--;
            return;
        }

        Integer pin = Integer.valueOf(verifyPin);

        databaseReference.child("Pin").setValue(pin);
        Toast.makeText(getActivity(), "Successfully Changed Pin", Toast.LENGTH_SHORT).show();
        Fragment fragment = new ProfileFragment();
        replaceFragment(fragment);
    }

    private void clearPinViewChild() {
        for (int i = 0; i < pinview.getChildCount() ; i++) {
            EditText child = (EditText) pinview.getChildAt(i);
            child.setText("");
        }
    }

    @Override
    public void onClick(View v) {
        if (v == buttonProceed){
            switch (click){
                case 1: firstClick();
                    break;
                case 2: secondClick();
                    break;
                case 3: thirdClick();
                    break;
            }
        }else{
            getActivity().onBackPressed();
        }
    }
}
