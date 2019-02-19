package com.example.dar.share;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;

public class NavBarActivity extends AppCompatActivity {

    public static String roomId = null, roomStatus = null;
    public static Context sContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_bar);
        sContext = getApplicationContext();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                new TravelFragment()).commitAllowingStateLoss();

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment selectedFragment = null;

                switch (menuItem.getItemId()){
                    case R.id.nav_travel:
                        selectedFragment = new TravelFragment();
                        break;
                    case R.id.nav_room:
                        if (roomId != null){
                            selectedFragment = new InsideRoomFragment(roomId, roomStatus);
                        }else{
                            selectedFragment = new SearchRoomFragment();
                        }
                        break;
                    case R.id.nav_history:
                        selectedFragment = new HistoryFragment();
                        break;
                    case R.id.nav_Profile:
                        selectedFragment = new ProfileFragment();
                        break;
                }

                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                        selectedFragment).commitAllowingStateLoss();

                return true;
            }
        });

    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment instanceof InsideRoomFragment) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("Are you sure you want to exit this room?");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //delete();
                            roomId = roomStatus = null;
                            Fragment fragment = new TravelFragment();
                            replaceFragment(fragment);
                        }
                    });

            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }else{
            if(getSupportFragmentManager().getBackStackEntryCount() == 0) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("Are you sure you want to close the application?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finishAffinity();
                                System.exit(0);
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }else if (getSupportFragmentManager().findFragmentByTag("InsideRoom") != null){
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("Are you sure you want to exit this room?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //delete();
                                roomId = roomStatus = null;
                                Fragment fragment = new TravelFragment();
                                replaceFragment(fragment);
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }else{
                getFragmentManager().popBackStackImmediate();
                getSupportFragmentManager().popBackStack(getSupportFragmentManager().getBackStackEntryCount()-1, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
    }

    public void replaceFragment(Fragment someFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, someFragment);
        transaction.commitAllowingStateLoss();
    }
}
