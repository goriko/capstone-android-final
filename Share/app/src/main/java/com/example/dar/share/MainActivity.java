package com.example.dar.share;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button buttonRegister, buttonLogin;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);

        firebaseAuth = FirebaseAuth.getInstance();

        //check if someone is logged in
        if(firebaseAuth.getCurrentUser() != null){
            //checks if user verified email
            if(firebaseAuth.getCurrentUser().isEmailVerified()){
                finish();
                //startActivity(new Intent(getApplicationContext(), NavBarActivity.class));
            }else{
                finish();
                startActivity(new Intent(getApplicationContext(), EmailVerificationActivity.class));
            }
        }

        buttonRegister.setOnClickListener(this);
        buttonLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == buttonLogin){
            startActivity(new Intent(this, LoginActivity.class));
        }else{
            startActivity(new Intent(this, RegistrationActivity.class));
        }
    }
}
