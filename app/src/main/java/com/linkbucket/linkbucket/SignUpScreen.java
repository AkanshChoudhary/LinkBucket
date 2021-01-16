package com.linkbucket.linkbucket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SignUpScreen extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    String userId;
    LottieAnimationView lottieAnimationView;
    AlertDialog.Builder dialog;
    AlertDialog alertDialog;
    static String userName;
    TextView switchToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);
        firebaseAuth = FirebaseAuth.getInstance();
        switchToLogin = findViewById(R.id.switchToSign);
        firebaseFirestore = FirebaseFirestore.getInstance();
        View v1 = getLayoutInflater().inflate(R.layout.loading_dialog, null);
        lottieAnimationView = v1.findViewById(R.id.loadinglottie);
        dialog = new AlertDialog.Builder(SignUpScreen.this, R.style.DialogTheme);
        dialog.setView(v1);
        alertDialog = dialog.create();
        alertDialog.setCanceledOnTouchOutside(false);
        switchToLogin.setOnClickListener(view -> {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginScreen.class));
        });
        findViewById(R.id.signUpButton).setOnClickListener(v -> {
            alertDialog.show();
            lottieAnimationView.playAnimation();
            final EditText confirmPass = findViewById(R.id.cnfrmSignPassBox);
            final EditText username = findViewById(R.id.usernameSignBox);
            final EditText password = findViewById(R.id.passwordSignBox);

            if (username.getText().toString().length() == 0 || password.getText().toString().length() == 0) {
                Toast.makeText(SignUpScreen.this, "Please Enter some username and password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.getText().toString().equals(confirmPass.getText().toString())) {
                String user=username.getText().toString();
                String pass=password.getText().toString();
                createUser(user,pass);
            } else {
                Toast.makeText(SignUpScreen.this, "Please enter same password in both fields.", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void createUser(String username, String password) {
        firebaseAuth.createUserWithEmailAndPassword(username, password)
                .addOnSuccessListener(authResult -> {
                    userId = firebaseAuth.getCurrentUser().getUid();
                    makeAndStorePersonalId(userId);
                })
                .addOnFailureListener(e -> Toast.makeText(SignUpScreen.this, "Please Check your net Connection", Toast.LENGTH_SHORT).show());
    }

    public void makeAndStorePersonalId(String userId) {
        String personalId = generateRandomId();
        EditText name = findViewById(R.id.nameSignBox);
        userName=name.getText().toString();
        Map<String, Object> personalIdMap = new HashMap<>();
        personalIdMap.put("personalId", personalId);
        personalIdMap.put("name", name.getText().toString());
        firebaseFirestore.collection("user+" + userId).document("Id").set(personalIdMap)
                .addOnSuccessListener(aVoid -> {
                    firebaseFirestore.collection("usersList").document(userId).set(personalIdMap)
                            .addOnSuccessListener(aVoid1 -> {
                                lottieAnimationView.pauseAnimation();
                                alertDialog.dismiss();
                                Intent intent = new Intent(getApplicationContext(), MainUserDashboard.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            });
                })
                .addOnFailureListener(e -> {Toast.makeText(SignUpScreen.this, "error", Toast.LENGTH_SHORT).show();
                    lottieAnimationView.pauseAnimation();
                    alertDialog.dismiss();
                });
    }

    private String generateRandomId() {
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrst0123456789".toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 7; i++) {
            char c = chars[random.nextInt(chars.length)];
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

}
