package com.sample.login.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.sample.login.R;

public class MTSplashActivity extends MTBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MTSplashActivity.this, MTLoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 3000);
        } catch (NullPointerException e) {
            Log.d("MTSplashActivity", "Caught null pointer exception in splash activity: " + e.getMessage());
        } catch (Exception e) {
            Log.d("MTSplashActivity", e.getLocalizedMessage());
        }
    }
}