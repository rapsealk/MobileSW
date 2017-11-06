package com.rapsealk.mobilesw;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class FragmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        Fragment_WorldPhoto fragment = new Fragment_WorldPhoto();
        fragmentTransaction.replace(R.id.tt, fragment);
        fragmentTransaction.commit();

/*
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment_WorldPhoto fragment = new Fragment_WorldPhoto();
        fragmentTransaction.add(R.id.tt, fragment);
        fragmentTransaction.commit();
        */
    }
}
