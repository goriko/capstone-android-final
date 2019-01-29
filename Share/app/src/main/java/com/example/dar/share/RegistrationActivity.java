package com.example.dar.share;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextEmail, editTextPassword, editTextPassword2, editTextFName, editTextLName, editTextContactNumber, editTextGContactNumber;
    private Spinner spinnerGender;
    private Button buttonFile, buttonProceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextPassword2 = (EditText) findViewById(R.id.editTextPassword2);
        editTextFName = (EditText) findViewById(R.id.editTextFName);
        editTextLName = (EditText) findViewById(R.id.editTextLName);
        spinnerGender = (Spinner) findViewById(R.id.spinnerGender);
        editTextContactNumber = (EditText) findViewById(R.id.editTextNumber);
        editTextGContactNumber = (EditText) findViewById(R.id.editTextGuardianNumber);
        buttonFile = (Button)findViewById(R.id.buttonFile);
        buttonProceed = (Button) findViewById(R.id.buttonProceed);

        String[] items = new String[]{"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinnerGender.setAdapter(adapter);

        buttonFile.setOnClickListener(this);
        buttonProceed.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == buttonFile){

        }else{
            startActivity(new Intent(this, EmailVerificationActivity.class));
        }
    }
}
