package com.example.dar.share;

import android.Manifest;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
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
                    long key = Long.parseLong(data.getKey().toString());
                    String dateString = DateFormat.format("yyyy-MM-dd", new Date(key)).toString();
                    String timeString = DateFormat.format("hh:mm:ss a", new Date(key)).toString();
                    String message = data.child("MessageText").getValue().toString();
                    String muser = data.child("MessageUser").getValue().toString();

                    LinearLayout samp = new LinearLayout(NavBarActivity.sContext);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (muser.equals(user.getUid())){
                        layoutParams.setMargins(dp(30), 0, 0, dp(10));
                    }else{
                        layoutParams.setMargins(0, 0, dp(30), dp(10));
                    }
                    samp.setLayoutParams(layoutParams);
                    samp.setBackgroundResource(R.drawable.customborder);
                    samp.setPadding(dp(10), dp(10),dp(10),dp(10));
                    samp.setOrientation(LinearLayout.VERTICAL);

                    TextView textView4 = new TextView(NavBarActivity.sContext);
                    LinearLayout.LayoutParams textViewParams3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    textView4.setLayoutParams(textViewParams3);
                    textView4.setText(dateString);
                    textView4.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

                    LinearLayout linearLayout2 = new LinearLayout(NavBarActivity.sContext);
                    LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams2.setMargins(0, dp(5), 0, 0);
                    linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
                    linearLayout2.setLayoutParams(layoutParams2);

                    TextView textView1 = new TextView(NavBarActivity.sContext);
                    textView1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                    ref.child(muser).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            textView1.setText(dataSnapshot.child("Fname").getValue().toString() +" "+dataSnapshot.child("Lname").getValue().toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    TextView textView3 = new TextView(NavBarActivity.sContext);
                    LinearLayout.LayoutParams textViewParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    textView3.setLayoutParams(textViewParams1);
                    textView3.setText(timeString);
                    textView3.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

                    LinearLayout linearLayout1 = new LinearLayout(NavBarActivity.sContext);
                    LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams1.setMargins(0, dp(10), 0, 0);
                    linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
                    linearLayout1.setLayoutParams(layoutParams1);

                    TextView textView = new TextView(NavBarActivity.sContext);
                    LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    textView.setLayoutParams(textViewParams);
                    textView.setText("Message:");

                    TextView textView2 = new TextView(NavBarActivity.sContext);
                    LinearLayout.LayoutParams textViewParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    textViewParams2.setMargins(dp(20), 0, 0, 0);
                    textView2.setLayoutParams(textViewParams2);
                    textView2.setText(message);

                    linearLayout1.addView(textView);
                    linearLayout1.addView(textView2);

                    linearLayout2.addView(textView1);
                    linearLayout2.addView(textView3);

                    samp.addView(textView4);
                    samp.addView(linearLayout2);
                    samp.addView(linearLayout1);
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

    public int dp(int number){
        DisplayMetrics displayMetrics = NavBarActivity.sContext.getResources().getDisplayMetrics();
        return (int)((number * displayMetrics.density) + 0.5);
    }


}
