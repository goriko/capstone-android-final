package com.example.dar.share;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

@SuppressLint("ValidFragment")
public class InsideRoomFragment extends Fragment {

    private View rootView;

    public InsideRoomFragment(String id, String status){
        ((NavBarActivity)this.getActivity()).roomId = id;
        ((NavBarActivity)this.getActivity()).roomStatus = status;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_inside_room, container, false);

        Toast.makeText(NavBarActivity.sContext, NavBarActivity.roomId.toString(), Toast.LENGTH_SHORT);

        return rootView;
    }
}
