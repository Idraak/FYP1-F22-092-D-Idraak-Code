package com.adeenayub.idraakphase1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class splashscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        getSupportActionBar().hide();

//        // Hide the action bar if it exists
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.hide();
//        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent ihome = new Intent(splashscreen.this, MainActivity.class);
                startActivity(ihome);
                finish();
            }
        }, 3000);

    }
}