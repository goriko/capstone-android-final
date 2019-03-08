package com.example.dar.share;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

public class RoomMessagesFragment extends Fragment {

    private View rootView;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private EditText editTextMessage;
    private Button buttonSend;
    private LinearLayout linearLayout;
    private ScrollView scrollView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_room_messages, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference("travel").child(NavBarActivity.roomId);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        editTextMessage = (EditText) rootView.findViewById(R.id.editTextMessage);
        buttonSend = (Button) rootView.findViewById(R.id.buttonSend);
        linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
        scrollView = (ScrollView) rootView.findViewById(R.id.scrollView);

        databaseReference.child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                linearLayout.removeAllViews();
                for (DataSnapshot data: dataSnapshot.getChildren()){
                    LinearLayout samp = new LinearLayout(NavBarActivity.sContext);
                    samp.setBackgroundResource(R.drawable.customborder);

                    long key = Long.parseLong(data.getKey().toString());
                    String dateString = DateFormat.format("yyyy-MM-dd hh:mm:ss a", new Date(key)).toString();
                    String message = data.child("MessageText").getValue().toString();
                    String user = data.child("MessageUser").getValue().toString();

                    TextView textView1 = new TextView(NavBarActivity.sContext);
                    textView1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                    ref.child(user).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            textView1.setText("User: " + dataSnapshot.child("Fname").getValue().toString() +" "+dataSnapshot.child("Lname").getValue().toString()+
                                    "\nMessage: " + message +
                                    "\n" + dateString);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    samp.addView(textView1);
                    linearLayout.addView(samp);

                }
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMessage();
            }
        });

        return rootView;
    }

    private void addMessage(){
        String str = editTextMessage.getText().toString();
        if (!str.equals("")){
            Message m = new Message(str, user.getUid().toString());
            Date currentTime = Calendar.getInstance().getTime();
            long key = currentTime.getTime();
            databaseReference.child("messages").child(Long.toString(key)).setValue(m);
            editTextMessage.setText("");
            InputMethodManager imm = (InputMethodManager) NavBarActivity.sContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
        }
    }


}
