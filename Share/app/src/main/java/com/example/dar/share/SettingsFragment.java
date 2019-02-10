package com.example.dar.share;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private View rootView;

    private Fragment fragment = null;

    private CardView cardViewProfile, cardViewLogout;
    private LinearLayout linearLayoutPassword, linearLayoutPin;

    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        cardViewProfile = (CardView) rootView.findViewById(R.id.cardViewProfile);
        linearLayoutPassword = (LinearLayout) rootView.findViewById(R.id.linearLayoutPassword);
        linearLayoutPin = (LinearLayout) rootView.findViewById(R.id.linearLayoutPin);
        cardViewLogout = (CardView) rootView.findViewById(R.id.cardViewLogout);

        cardViewProfile.setOnClickListener(this);
        linearLayoutPassword.setOnClickListener(this);
        linearLayoutPin.setOnClickListener(this);
        cardViewLogout.setOnClickListener(this);

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
        if (v == cardViewProfile){
            fragment = new EditProfileFragment();
            replaceFragment(fragment);
        }else if (v == linearLayoutPassword){
            fragment = new ChangePasswordFragment();
            replaceFragment(fragment);
        }else if (v == linearLayoutPin){
            fragment = new ChangePinFragment();
            replaceFragment(fragment);
        }else if (v == cardViewLogout){
            logout();
        }
    }

}
