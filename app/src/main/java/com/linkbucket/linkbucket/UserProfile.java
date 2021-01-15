package com.linkbucket.linkbucket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class UserProfile extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    String userId;
    TextView nameField;
    TextView idField;
    TextView emailField;
    TextView mainTitle;
    TextView share;

    enum ChangeItem {
        email,
        name,
        password
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        firebaseAuth = FirebaseAuth.getInstance();
        mainTitle = findViewById(R.id.mainName);
        share=findViewById(R.id.shareProfile);
        firebaseFirestore = FirebaseFirestore.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();
        nameField = findViewById(R.id.userName);
        idField = findViewById(R.id.userId);
        emailField = findViewById(R.id.userEmail);
        TextView resetPass = findViewById(R.id.resetPassword);
        firebaseFirestore.collection("user+" + userId).document("Id").get()
                .addOnSuccessListener(documentSnapshot -> {
                    String name = documentSnapshot.getString("name");
                    String personalId = documentSnapshot.getString("personalId");
                    String email = firebaseAuth.getCurrentUser().getEmail();
                    mainTitle.setText(name);
                    nameField.setText(name);
                    idField.setText(personalId);
                    emailField.setText(email);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Please check your connection", Toast.LENGTH_SHORT).show();
                });

        nameField.setOnClickListener(v -> changeDetail(ChangeItem.name));
        emailField.setOnClickListener(v -> changeDetail(ChangeItem.email));
        resetPass.setOnClickListener(v -> changeDetail(ChangeItem.password));
        findViewById(R.id.logoutMain).setOnClickListener(v -> {
            firebaseAuth.signOut();
            startActivity(new Intent(getApplicationContext(), LoginSignupScreen.class));
        });
        share.setOnClickListener(v->{
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey there! I am "+mainTitle.getText().toString()+" and here is my Link Bucket code: "+idField.getText().toString());
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        });
    }

    public void changeDetail(ChangeItem changeItem) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(UserProfile.this, R.style.DialogTheme);
        View v = getLayoutInflater().inflate(R.layout.change_detail_dialog, null);
        dialog.setView(v);
        EditText newDetail = v.findViewById(R.id.newDetail);
        Button save = v.findViewById(R.id.saveNewDetail);
        Button cancel = v.findViewById(R.id.cancelBox);
        AlertDialog alert = dialog.create();
        if (changeItem == ChangeItem.name) {
            newDetail.setHint("Enter name to change");
        }
        if (changeItem == ChangeItem.email) {
            newDetail.setHint("Enter Email to change");
        }
        if (changeItem == ChangeItem.password) {
            newDetail.setHint("Enter your Email to send reset mail");
            save.setText("Send mail");
        }
        alert.show();
        cancel.setOnClickListener(v1 -> alert.dismiss());
        save.setOnClickListener(v1 -> {
            if (changeItem == ChangeItem.name) {
                firebaseFirestore.collection("user+" + userId).document("Id").update("name", newDetail.getText().toString())
                        .addOnSuccessListener(aVoid -> {
                            mainTitle.setText(newDetail.getText().toString());
                            nameField.setText(newDetail.getText().toString());
                            alert.dismiss();
                        });
            } else if (changeItem == ChangeItem.email) {
                firebaseAuth.getCurrentUser().updateEmail(newDetail.getText().toString())
                        .addOnSuccessListener(aVoid -> {
                            emailField.setText(newDetail.getText().toString());
                            alert.dismiss();
                        });
            } else {
                firebaseAuth.sendPasswordResetEmail(newDetail.getText().toString())
                        .addOnSuccessListener(aVoid -> {
                            alert.dismiss();
                            Toast.makeText(this, "Email sent for password reset.", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}