package com.example.linkbucket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LoginSignupScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup_screen);

        findViewById(R.id.openLoginButton).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),LoginScreen.class)));

        findViewById(R.id.openSignButton).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),SignUpScreen.class)));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        moveTaskToBack(true);
    }
}
