package com.adi.startoonlabs.signinup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.adi.startoonlabs.R;
import com.adi.startoonlabs.WelcomeActivity;
import com.adi.startoonlabs.dashboard.HomeActivity;
import com.adi.startoonlabs.signinup.mvvm.LoginRepository;
import com.google.firebase.auth.FirebaseAuth;

public class SignInNUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_sign_in_up);

        // checking if already logged in
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            // user logged in
            Intent intent;
            intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

            finish(); // finishing splash screen
        }

    }
}