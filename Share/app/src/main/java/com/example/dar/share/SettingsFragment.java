package com.example.dar.share;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private View rootView;

    private Fragment fragment = null;

    private Button buttonProfile, buttonPassword, buttonPin, buttonLogout;

    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        buttonProfile = (Button) rootView.findViewById(R.id.buttonProfile);
        buttonPassword = (Button) rootView.findViewById(R.id.buttonPassword);
        buttonPin = (Button) rootView.findViewById(R.id.buttonPin);
        buttonLogout = (Button) rootView.findViewById(R.id.buttonLogout);

        buttonProfile.setOnClickListener(this);
        buttonPassword.setOnClickListener(this);
        buttonPin.setOnClickListener(this);
        buttonLogout.setOnClickListener(this);

        return rootView;
    }

    void logout(){
        firebaseAuth.signOut();
        startActivity(new Intent(getActivity(), MainActivity.class));
    }

    public void replaceFragment(Fragment someFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, someFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        if (v == buttonProfile){
            fragment = new EditProfileFragment();
            replaceFragment(fragment);
        }else if (v == buttonPassword){
            fragment = new ChangePasswordFragment();
            replaceFragment(fragment);
        }else if (v == buttonPin){
            fragment = new ChangePinFragment();
            replaceFragment(fragment);
        }else if (v == buttonLogout){
            logout();
        }
    }

}
