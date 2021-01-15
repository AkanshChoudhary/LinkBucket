package com.linkbucket.linkbucket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class LoginSignupScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup_screen);

        findViewById(R.id.openLoginButton).setOnClickListener(v -> {startActivity(new Intent(getApplicationContext(),LoginScreen.class));finish();});

        findViewById(R.id.openSignButton).setOnClickListener(v -> {startActivity(new Intent(getApplicationContext(),SignUpScreen.class));finish();});
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        moveTaskToBack(true);
    }
}
