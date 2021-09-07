package com.makerz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// This activity is used to change password for the user.

public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;

    private Button Update;
    private Button Back;
    private EditText mTextPassword;
    private EditText mTextRePassword;
    private EditText mTextOldPassword;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        // Declaration of elements.
        setupUIViews();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // When Button Back is pressed, move to ProfileActivity.
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // When Button Update is pressed, the key-in password will be updated to firebase
        // Password length must be 6 or more.
        Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(ChangePasswordActivity.this);
                progressDialog.setMessage("Changing password...");
                progressDialog.show();
                final String userPasswordNew = mTextPassword.getText().toString();
                final String userPasswordNewConfirm = mTextRePassword.getText().toString();
                final String userPasswordOld = mTextOldPassword.getText().toString();

                String ed1 = mTextPassword.getText().toString();
                final int size = ed1.length();
                if (size > 5) {
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(firebaseUser.getEmail(), userPasswordOld);
                    firebaseUser.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        firebaseUser.updatePassword(userPasswordNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (userPasswordNewConfirm.equals(userPasswordNew)) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(ChangePasswordActivity.this, "Password changed.", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    } else {
                                                        Toast.makeText(ChangePasswordActivity.this, "Password update failed.", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {

                                                    Toast.makeText(ChangePasswordActivity.this, "Please check your password again.", Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });
                                    } else {
                                        Toast.makeText(ChangePasswordActivity.this, "Make sure old password is correct.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {

                    Toast.makeText(ChangePasswordActivity.this, "Please make sure your password is at least 6 characters or more.", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });
    }

    private void setupUIViews() {
        Update = findViewById(R.id.button_change_password_1);
        Back = findViewById(R.id.button_backz_1);
        mTextPassword = findViewById(R.id.edittext_password_1);
        mTextRePassword = findViewById(R.id.edittext_re_password_1);
        mTextOldPassword = findViewById(R.id.edittext_password_old);
    }
}
