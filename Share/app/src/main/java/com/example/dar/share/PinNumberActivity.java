package com.example.dar.share;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.goodiebag.pinview.Pinview;

public class PinNumberActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewPin;
    private Pinview pinView;
    private Button buttonProceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_number);

        textViewPin = (TextView) findViewById(R.id.textViewPin);
        pinView = (Pinview) findViewById(R.id.pinView);
        buttonProceed = (Button) findViewById(R.id.buttonProceed);

        buttonProceed.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

    }
}
