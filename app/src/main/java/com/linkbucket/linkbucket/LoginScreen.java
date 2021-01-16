package com.linkbucket.linkbucket;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;

public class LoginScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        final EditText username = findViewById(R.id.usernameSignBox);
        final EditText password = findViewById(R.id.passwordSignBox);

        findViewById(R.id.loginBtn).setOnClickListener(v -> {
            Dialog dialog = new Dialog(LoginScreen.this, R.style.DialogTheme);
            dialog.setContentView(R.layout.loading_dialog);
            LottieAnimationView lottieAnimationView = dialog.findViewById(R.id.loadinglottie);
            dialog.create();
            dialog.show();
            lottieAnimationView.playAnimation();
            if (username.getText().toString().length() == 0 || password.getText().toString().length() == 0) {
                Toast.makeText(LoginScreen.this, "Please enter some details", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseAuth.getInstance().signInWithEmailAndPassword(username.getText().toString(), password.getText().toString())
                    .addOnSuccessListener(authResult -> {
                        lottieAnimationView.pauseAnimation();
                        dialog.dismiss();
                        Intent intent = new Intent(getApplicationContext(), MainUserDashboard.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(LoginScreen.this, "Invalid Credentials.", Toast.LENGTH_LONG).show();
                        lottieAnimationView.pauseAnimation();
                        dialog.dismiss();
                    });
        });

        findViewById(R.id.switchToSign).setOnClickListener(v -> {finish();startActivity(new Intent(getApplicationContext(), SignUpScreen.class));});

        findViewById(R.id.forgetBtn).setOnClickListener(v -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(LoginScreen.this, R.style.DialogTheme);
            View v1 = getLayoutInflater().inflate(R.layout.forget_password_dialog, null);
            dialog.setView(v1);
            final AlertDialog alertDialog = dialog.create();
            alertDialog.show();
            alertDialog.setCanceledOnTouchOutside(false);

            final EditText email = v1.findViewById(R.id.emailbox);

            v1.findViewById(R.id.sendMail).setOnClickListener(v22 -> {
                if (email.getText().toString().length() == 0) {
                    Toast.makeText(LoginScreen.this, "Please enter an email id", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(LoginScreen.this, "Reset link sent to your given mail id", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(LoginScreen.this, "Please enter the correct email id or check your connection", Toast.LENGTH_LONG).show());


            });
            v1.findViewById(R.id.closeDialog).setOnClickListener(v2 -> alertDialog.dismiss());
        });
    }
}
