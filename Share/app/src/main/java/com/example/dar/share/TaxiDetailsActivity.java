package com.example.dar.share;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

@SuppressLint("ValidFragment")
public class TaxiDetailsActivity extends Fragment {

    private Button buttonProceed;
    private EditText edittextPlateNum;
    private EditText edittextNum;
    private EditText edittextOperator;
    private DatabaseReference databaseReference;
    private Fragment fragment = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_taxi_details, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("travel").child(NavBarActivity.roomId);

        buttonProceed = (Button) rootView.findViewById(R.id.buttonProceed);
        edittextPlateNum = (EditText) rootView.findViewById(R.id.editTextPlateNum);
        edittextNum = (EditText) rootView.findViewById(R.id.editTextNum);
        edittextOperator = (EditText) rootView.findViewById(R.id.editTextOperator);

        buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String plate = edittextPlateNum.getText().toString();
                String num = edittextNum.getText().toString();
                String operator = edittextOperator.getText().toString();
                Taxi taxi = new Taxi(plate, num, operator);
                databaseReference.child("taxi").setValue(taxi);

                NavBarActivity.roomStatus = "on going";

                NavBarActivity.bottomNav.getMenu().getItem(1).setChecked(true);
                NavBarActivity.bottomNav.setSelectedItemId(R.id.nav_room);

                databaseReference.child("Available").setValue(0);
            }
        });
        return rootView;
    }

}
