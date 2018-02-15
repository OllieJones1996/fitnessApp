package com.example.ollie.fitnessapp;

import android.content.Context;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.SignInButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class homeFragment extends Fragment {

    private SignInButton SignInbutton;


    public homeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

        //cast signin button
        SignInbutton = (SignInButton) getView().findViewById(R.id.sign_in_button);

        SignInbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                       //MainActivity.test();
                        Log.v("t", "THIS IS FRAGMENT BUTTON");
                        break;
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

}