package com.makerz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;



public class ForgotPasswordActivity extends AppCompatActivity {

    private Button Back;
    private Button Confirm;
    private EditText mEmail;

    private FirebaseAuth firebaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setupUIViews();


        firebaseAuth = FirebaseAuth.getInstance();

        Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    //upload data to the database
                    String email_name = mEmail.getText().toString().trim();

                    if (email_name.equals("")) {
                        Toast.makeText(ForgotPasswordActivity.this, "Please enter your registered Heriot-Watt email ID", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        firebaseAuth.sendPasswordResetEmail(email_name).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ForgotPasswordActivity.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                                    finish();
                                    startActivity(new Intent(ForgotPasswordActivity.this, MainActivity.class));
                                }else{
                                    Toast.makeText(ForgotPasswordActivity.this, "Error in sending password reset email.", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    }
                }
            }
        });

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ForgotPasswordActivity.this, MainActivity.class));
                //or      Intent loginIntent = new Intent(RegisterActivity.this, MainActivity.class);
                //        startActivity(loginIntent);
            }
        });


    }

    private void setupUIViews() {

        Back = (findViewById(R.id.button_back));
        Confirm = (findViewById(R.id.button_confirm));
        mEmail = (findViewById(R.id.reemail));
    }

    private Boolean validate() {
        Boolean result = false;

        String emailname = mEmail.getText().toString();

        if(emailname.isEmpty())
        {
            Toast.makeText(this,"Email column is blank.",Toast.LENGTH_SHORT).show();}
        else{
            result = true;
        }
        return result;
    }
}
