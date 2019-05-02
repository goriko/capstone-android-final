package com.example.dar.share;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordFragment extends Fragment {

    private View rootView;

    private Button buttonProceed, buttonCancel;
    private EditText editTextOld, editTextNew, editTextVerify;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    private Fragment fragment = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_change_password, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = firebaseAuth.getCurrentUser();

        editTextOld = (EditText) rootView.findViewById(R.id.editTextOld);
        editTextNew = (EditText) rootView.findViewById(R.id.editTextNew);
        editTextVerify = (EditText) rootView.findViewById(R.id.editTextConfirm);
        buttonProceed = (Button) rootView.findViewById(R.id.buttonProceed);
        buttonCancel = (Button) rootView.findViewById(R.id.buttonCancel);

        progressDialog = new ProgressDialog(this.getContext());

        buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Changing Password ....");
                progressDialog.setCancelable(false);
                progressDialog.show();

                String oldPass = editTextOld.getText().toString();
                String newPass = editTextNew.getText().toString();
                String confirmPass = editTextVerify.getText().toString();

                if(TextUtils.isEmpty(oldPass)){
                    Toast.makeText(NavBarActivity.sContext, "Please enter your password", Toast.LENGTH_SHORT).show();
                    progressDialog.cancel();
                    return;
                }else if(TextUtils.isEmpty(oldPass)){
                    Toast.makeText(NavBarActivity.sContext, "Please enter your new password", Toast.LENGTH_SHORT).show();
                    progressDialog.cancel();
                    return;
                }else if(TextUtils.isEmpty(oldPass)){
                    Toast.makeText(NavBarActivity.sContext, "Please enter your new password again for confirmation", Toast.LENGTH_SHORT).show();
                    progressDialog.cancel();
                    return;
                }

                final String email = user.getEmail();
                AuthCredential credential = EmailAuthProvider.getCredential(email,oldPass);

                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            if(newPass.equals(confirmPass)){
                                user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(getActivity(), "Successfully changed password", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                            fragment = new ProfileFragment();
                                            replaceFragment(fragment);
                                        }else{
                                            Toast.makeText(getActivity(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                                            progressDialog.cancel();
                                        }
                                    }
                                });
                            }else {
                                Toast.makeText(getActivity(), "Please Enter the same new password", Toast.LENGTH_SHORT).show();
                                editTextOld.setText("");
                                editTextNew.setText("");
                                editTextVerify.setText("");
                                progressDialog.dismiss();
                            }

                        }else{
                            Toast.makeText(getActivity(), "Please Enter the correct Password", Toast.LENGTH_SHORT).show();
                            editTextOld.setText("");
                            editTextNew.setText("");
                            editTextVerify.setText("");
                            progressDialog.dismiss();
                        }
                    }
                });

            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        return rootView;
    }

    public void replaceFragment(Fragment someFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, someFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
