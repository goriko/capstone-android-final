package com.example.dar.share;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static java.lang.Boolean.FALSE;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_IMAGE_REQUEST = 234;

    private EditText editTextEmail, editTextPassword, editTextPassword2, editTextFName, editTextLName, editTextContactNumber, editTextGContactNumber;
    private Spinner spinnerGender;
    private Button buttonFile, buttonProceed;

    private ProgressDialog progressDialog;

    private Uri filePath = null;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

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

        progressDialog = new ProgressDialog(this);

        String[] items = new String[]{"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinnerGender.setAdapter(adapter);

        buttonFile.setOnClickListener(this);
        buttonProceed.setOnClickListener(this);
    }

    public void register(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String password2 = editTextPassword2.getText().toString().trim();
        String FName = editTextFName.getText().toString().trim();
        String LName = editTextLName.getText().toString().trim();
        String Gender = spinnerGender.getSelectedItem().toString();
        String Num = editTextContactNumber.getText().toString().trim();
        String GuardianNum = editTextGContactNumber.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            //email is empty
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }else if(TextUtils.isEmpty(password)){
            //password is empty
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }else if(!password.equals(password2)){
            Toast.makeText(this, "Password don't match. Please enter the correct password", Toast.LENGTH_SHORT).show();
            return;
        }else if(android.util.Patterns.PHONE.matcher(Num).matches() == FALSE){
            Toast.makeText(this, "Please Enter a correct phone number", Toast.LENGTH_LONG).show();
            editTextContactNumber.setText("");
            return;
        }else if(android.util.Patterns.PHONE.matcher(GuardianNum).matches() == FALSE){
            Toast.makeText(this, "Please Enter a correct phone number", Toast.LENGTH_LONG).show();
            editTextGContactNumber.setText("");
            return;
        }else if(filePath == null){
            Toast.makeText(this, "Please upload a photo", Toast.LENGTH_LONG).show();
            return;
        }

        //if validations are passed
        progressDialog.setMessage("Registering user....");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //user is successfully registered
                            userInfo(FName, LName, Gender, Num, GuardianNum);
                        }else{
                            String message = task.getException().getMessage();
                            Toast.makeText(RegistrationActivity.this, "Error Occurred: "+message, Toast.LENGTH_SHORT).show();
                            editTextEmail.setText("");
                            editTextPassword.setText("");
                            progressDialog.cancel();
                        }
                    }
                });
    }

    public void userInfo(String FName, String LName, String Gender, String Num, String GuardianNum){
        user = firebaseAuth.getCurrentUser();

        uploadFile();

        AddUserInformation addUserInformation = new AddUserInformation(FName, LName, Gender, Num, GuardianNum);

        databaseReference.child("users").child(user.getUid()).setValue(addUserInformation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    progressDialog.cancel();
                    Toast.makeText(RegistrationActivity.this, "Information Saved...", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), EmailVerificationActivity.class));
                }
            }
        });
    }

    private void uploadFile(){
        StorageReference riversRef = storageReference.child("profile/"+user.getUid().toString()+".jpg");
        riversRef.putFile(filePath);
    }

    private void showFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            filePath = data.getData();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == buttonFile){
            showFileChooser();
        }else{
            register();
        }
    }
}
