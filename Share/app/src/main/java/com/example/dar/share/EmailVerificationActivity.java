package com.example.dar.share;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonProceed;
    private TextView textViewVerified;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        user.sendEmailVerification();

        textViewVerified = (TextView) findViewById(R.id.textViewVerified);
        buttonProceed = (Button) findViewById(R.id.buttonProceed);

        buttonProceed.setOnClickListener(this);

        Handler handler = new Handler();
        int delay = 1000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                user.reload();
                Log.d("myTag", "eyyy");
                if(user.isEmailVerified() == true){
                    change();
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    public void change(){
        textViewVerified.setText("Email has been verified");
        buttonProceed.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, NavBarActivity.class));
    }
}
