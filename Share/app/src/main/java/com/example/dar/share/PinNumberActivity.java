package com.example.dar.share;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.goodiebag.pinview.Pinview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PinNumberActivity extends AppCompatActivity implements View.OnClickListener {

    private int i=0;
    private String pin1, pin2;

    private TextView textViewPin;
    private Pinview pinView;
    private Button buttonProceed;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_number);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        textViewPin = (TextView) findViewById(R.id.textViewPin);
        pinView = (Pinview) findViewById(R.id.pinView);
        buttonProceed = (Button) findViewById(R.id.buttonProceed);

        buttonProceed.setOnClickListener(this);
    }

    public void addPin(){
        if (!pin1.equals(pin2)){
            Toast.makeText(this, "Pin number doesn't match. Please try again", Toast.LENGTH_LONG).show();
            textViewPin.setText("Enter Pin");
            clearPinViewChild();
            i=0;
            return;
        }

        Integer pin = Integer.valueOf(pin1);

        FirebaseUser user = firebaseAuth.getCurrentUser();

        databaseReference.child("users").child(user.getUid().toString()).child("Pin").setValue(pin);
        startActivity(new Intent(getApplicationContext(), NavBarActivity.class));
    }

    private void clearPinViewChild() {
        for (int i = 0; i < pinView.getChildCount() ; i++) {
            EditText child = (EditText) pinView.getChildAt(i);
            child.setText("");

        }
    }

    @Override
    public void onClick(View v) {
        if (i==0){
            pin1 = pinView.getValue().toString();
            textViewPin.setText("Please re-enter pin number");
            clearPinViewChild();
            i=1;
        }else{
            pin2 = pinView.getValue().toString();
            addPin();
        }
    }
}
