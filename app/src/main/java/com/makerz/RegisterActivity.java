package com.makerz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makerz.model.user;

import java.util.Calendar;


public class RegisterActivity extends AppCompatActivity {

    private EditText Codename;
    private EditText Fullname;
    private EditText Email;
    private EditText mTextPassword;
    private EditText mTextRePassword;
    private Button mButtonRegister;
    private TextView mTextViewLogin;
    DatePickerDialog.OnDateSetListener setListener;
    int i = 0;

    private String codename,name,email,password,repassword;
    String RESULT = "false";
    String Result = "false";
    private FirebaseAuth firebaseAuth;
    DatabaseReference myRef;

    private ProgressDialog progressDialog;

    private static final String TAG = "Uploading data...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupUIViews();

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);




        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validate()) {
                    i = 1;

                    progressDialog.setMessage("Loading...");
                    progressDialog.show();

                    //upload data to the database
                    String code_name = Codename.getText().toString().trim();
                    String real_name = Fullname.getText().toString().trim();
                    String password = mTextPassword.getText().toString().trim();
                    String email = Email.getText().toString().trim(); //+ "@hw.ac.uk".trim();


                    firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                            sendEmailVerification();

                            } else {
                                Log.d(TAG,"onComplete: Failed =" + task.getException().getMessage());
                                Toast.makeText(RegisterActivity.this,"Registration Failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });

        mTextViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(RegisterActivity.this, MainActivity.class));

            }
        });

    }

    private void setupUIViews() {
        Codename = findViewById(R.id.text_input_codename);
        Fullname = findViewById(R.id.name);
        Email = findViewById(R.id.edittext_email);
        mTextPassword = findViewById(R.id.edittext_password);
        mTextRePassword = findViewById(R.id.edittext_re_password);
        mButtonRegister = findViewById(R.id.button_register);
        mTextViewLogin = findViewById(R.id.textview_login);
    }

    private Boolean validate() {
        Boolean result = false;

        codename = Codename.getText().toString();
        name = Fullname.getText().toString();
        email = Email.getText().toString();
        password = mTextPassword.getText().toString();
        repassword  = mTextRePassword.getText().toString();

        if(((codename.isEmpty()) || (name.isEmpty()) || (email.isEmpty()) || (password.isEmpty()) || (repassword.isEmpty())))
        {
            Toast.makeText(RegisterActivity.this,"Please enter all the details.",Toast.LENGTH_SHORT).show();
        }else if(!email.endsWith("@hw.ac.uk")){
            Toast.makeText(RegisterActivity.this,"Please use your heriot watt email-address.",Toast.LENGTH_SHORT).show();
        }else if(!isValidEmail(email)){
            Toast.makeText(RegisterActivity.this,"Please use a valid email-address.",Toast.LENGTH_SHORT).show();
        } else{

            String ed1 = mTextPassword.getText().toString();
            String ed2 = Email.getText().toString();
            int size = ed1.length();
            int size2 = ed2.length();

            if (size>5) {

                if (repassword.equals(password)) {
                    RESULT = "true";

                } else {
                    RESULT = "false";
                    Toast.makeText(RegisterActivity.this, "Please check your password again.", Toast.LENGTH_SHORT).show();
                }

            } else {
                RESULT = "false";
                Toast.makeText(RegisterActivity.this, "Please make sure your password is at least 6 characters or more.", Toast.LENGTH_SHORT).show();
            }

           /* if (size2 <= 6){
               RESULT = "true";


            }else{
                RESULT = "false";
                Toast.makeText(RegisterActivity.this, "Please make sure it is your Heriot-Watt email address", Toast.LENGTH_SHORT).show();
            }*/

            myRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference dataRef = myRef.child("Users");

            dataRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for(final DataSnapshot data : dataSnapshot.getChildren())
                    {
                        String firebaseCodename = data.child("codename").getValue(String.class);
                        String firebaseEmail = data.child("email").getValue(String.class);

                        if (!Codename.getText().toString().equals(firebaseCodename) && !Email.getText().toString().equals(firebaseEmail))
                        {
                            Result = "true";
                        }
                        else
                            {

                            if (Codename.getText().toString().equals(firebaseCodename) && !Email.getText().toString().equals(firebaseEmail))
                            {
                                Toast.makeText(RegisterActivity.this, "This codename already exists. Please try another codename.", Toast.LENGTH_SHORT).show();
                            }
                            else if (Email.getText().toString().equals(firebaseEmail) && !Codename.getText().toString().equals(firebaseCodename) )
                            {
                                Toast.makeText(RegisterActivity.this, "This email already exists. Please try another email.", Toast.LENGTH_SHORT).show();
                            }
                            else if (Codename.getText().toString().equals(firebaseCodename) && Email.getText().toString().equals(firebaseEmail)) {

                                if (i == 0)
                                {
                                    Toast.makeText(RegisterActivity.this, "Both codename and email already exists. Please use a different codename and email.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            Result = "false";

                            }



                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            if(RESULT.equals("true") && Result.equals("true"))
            {
                result = true;
            }
            else
            {
                result = false;
            }


        }
        return result;
    }
    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
    private void sendEmailVerification(){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser!=null)
        {
          firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                  if(task.isSuccessful()) {
                      sendUserData();
                      progressDialog.dismiss();
                      Toast.makeText(RegisterActivity.this, "Successfully registered. Verification email sent!", Toast.LENGTH_SHORT).show();
                      firebaseAuth.signOut();
                      finish();
                      startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                  }else{
                      progressDialog.dismiss();
                   Toast.makeText(RegisterActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                  }
                  }

          });
        }
    }
private void sendUserData(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        String userId= firebaseAuth.getUid();
        DatabaseReference myRef = firebaseDatabase.getReference().child("Users").child(userId);
        user userProfile = new user(codename,name,email);
        myRef.setValue(userProfile);

}


}
