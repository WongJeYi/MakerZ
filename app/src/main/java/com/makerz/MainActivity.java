package com.makerz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
import com.makerz.model.Data.MySingleton;

import java.util.ArrayList;

// This activity is also known as login page.
// And is used for user login.

public class MainActivity extends AppCompatActivity {

    private int counter = 5;

    private FirebaseAuth firebaseAuth;

    private EditText Codename;
    private EditText Password;
    private Button Login;
    private TextView Register;
    private TextView ForgotPassword;
    private TextView Info;

    private ProgressDialog progressDialog;
    private Dialog dialog;
    private FirebaseUser user;
    private Dialog errorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showCustomDialog();
        // Declaration of elements.
        setupUIViews();

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        // Get current user's info.
        // If already login, no need to key in email and password again.

        user = firebaseAuth.getCurrentUser();



        // When Button Login is clicked, the codename and password key-in by user will be used to cross-check
        // with authentification info on firebase.
        // If unsuccessfull, user will be given 4 more chances to login.
        // Else Button Login will be disabled and user will have to quit/refresh app to try again.
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String codename = Codename.getText().toString();
                String password = Password.getText().toString();
                if((codename.isEmpty()) && (!password.isEmpty()))
                {
                    Toast.makeText(MainActivity.this, "Please enter your codename.", Toast.LENGTH_SHORT).show();
                    Info.setText("Number of attempts remaining: " + String.valueOf(counter));
                    counter--;
                    if (counter == 0) {
                        Info.setText("Number of attempts remaining: 0");
                        Login.setEnabled(false);
                    }
                }
                else if ((!codename.isEmpty())&& (password.isEmpty()))
                {
                    Toast.makeText(MainActivity.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
                    Info.setText("Number of attempts remaining: " + String.valueOf(counter));
                    counter--;
                    if (counter == 0) {
                        Info.setText("Number of attempts remaining: 0");
                        Login.setEnabled(false);
                    }
                }
                else if((codename.isEmpty())&&(password.isEmpty())){
                    Toast.makeText(MainActivity.this, "Please enter your codename and password.", Toast.LENGTH_SHORT).show();
                    Info.setText("Number of attempts remaining: " + String.valueOf(counter));
                    counter--;
                    if (counter == 0) {
                        Info.setText("Number of attempts remaining: 0");
                        Login.setEnabled(false);
                    }

                }
                else if ((codename.equals("Admin"))&&(password.equals("MakerZ123")))
                {
                    Intent registerIntent = new Intent(MainActivity.this, AdminActivity.class);
                    startActivity(registerIntent);
                }
                else{
                    validate(Codename.getText().toString(), Password.getText().toString());
                }

            }
        });

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        ForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent retrievepasswordIntent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
                startActivity(retrievepasswordIntent);

            }


        });

    }

    private void showCustomDialog() {
        dialog = new Dialog(MainActivity.this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        //We have added a title in the custom layout. So let's disable the default title.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
        dialog.setCancelable(true);
        //Mention the name of the layout of your custom dialog.
        dialog.setContentView(R.layout.pop_up_privacy_notice);
        TextView notice=dialog.findViewById(R.id.text_privacy_notice);
        if(MySingleton.president==null){
            notice.setText("Heriot-Watt University Malaysia MakerZ club is responsible for members’ data collected using this app. We will keep your data securely – on Google servers in the USA and use it only to enable you to communicate with other members and keep up to date with club activities. We will not share your information with anyone else. All members need to keep to our community rules for online behaviour. You can delete content that you have provided to the app at any time. We delete chat automatically after two months and when you leave the club your membership account will be erased. If you have any questions or concerns about your personal information or other content on this app please contact the MakerZ President. As a Heriot-Watt University student, you can find out more about your privacy rights here https://www.hw.ac.uk/uk/services/information-governance/access/privacy-current-students.htm.");
        }else {
            notice.setText("Heriot-Watt University Malaysia MakerZ club is responsible for members’ data collected using this app. We will keep your data securely – on Google servers in the USA and use it only to enable you to communicate with other members and keep up to date with club activities. We will not share your information with anyone else. All members need to keep to our community rules for online behaviour. You can delete content that you have provided to the app at any time. We delete chat automatically after two months and when you leave the club your membership account will be erased. If you have any questions or concerns about your personal information or other content on this app please contact the MakerZ President " + MySingleton.president + ". As a Heriot-Watt University student, you can find out more about your privacy rights here https://www.hw.ac.uk/uk/services/information-governance/access/privacy-current-students.htm.");
        }
        //Initializing the views of the dialog.
        final Button accept = dialog.findViewById(R.id.accept);


        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) {

                    checkAdmin();
                    checkPresident();
                    load();
                }else{
                    dialog.dismiss();
                    dialog.cancel();
                }
            }
        });

        dialog.show();
    }
    private void load(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("Events");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                finish();
                startActivity(new Intent(MainActivity.this, MenuActivity.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                finish();
                startActivity(new Intent(MainActivity.this, MenuActivity.class));
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
    }

    private void setupUIViews() {

        Codename = (EditText) findViewById(R.id.codename);
        Password = (EditText) findViewById(R.id.password);
        Login = (Button) findViewById(R.id.button_login);
        Register = (TextView) findViewById(R.id.register);
        ForgotPassword = (TextView) findViewById(R.id.forgotpassword);
        Info = (TextView) findViewById(R.id.info);
    }

    private void validate(String Codename, String Password) {

        progressDialog.setMessage("Loading...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(Codename, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                    checkEmailVerification();
                    //updateUI(user);
                } else {
                    Toast.makeText(MainActivity.this, "Login Failed.", Toast.LENGTH_SHORT).show();
                    Info.setText("Number of attempts remaining: " + String.valueOf(counter));
                    counter--;
                    progressDialog.dismiss();
                    if (counter == 0) {
                        Info.setText("Number of attempts remaining: 0");
                        Login.setEnabled(false);
                        updateUI(null);
                    }
                }



            }
        });

    }

    private void checkAdmin() {
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        final String EMAIL = firebaseAuth.getCurrentUser().getEmail();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // get database reference
            final DatabaseReference databaseRefer = firebaseDatabase.getReference().child("Admin");
            // database listener
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // check event exists
                    if (dataSnapshot.exists()) {
                        ArrayList<String> admin=(ArrayList<String>)dataSnapshot.getValue();
                        if(admin!=null){
                            if(admin.contains(EMAIL)){
                                MySingleton.isAdmin = true;
                                Log.e("ADMIN","s");
                            }else{
                                MySingleton.isAdmin = false;
                            }
                        }else{
                            MySingleton.isAdmin = false;
                        }
                    } else {
                        MySingleton.isAdmin = false;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            // add listener
            databaseRefer.addListenerForSingleValueEvent(postListener);
        }
    }
    private void checkPresident() {
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        final String EMAIL = firebaseAuth.getCurrentUser().getEmail();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // get database reference
            final DatabaseReference databaseRefer = firebaseDatabase.getReference().child("President");
            // database listener
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // check event exists
                    if (dataSnapshot.exists()) {
                        MySingleton.president=dataSnapshot.getValue(String.class);
                        Log.e("President",MySingleton.president);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            // add listener
            databaseRefer.addListenerForSingleValueEvent(postListener);
        }
    }
    private void checkEmailVerification() {
        FirebaseUser firebaseUser = firebaseAuth.getInstance().getCurrentUser();
        Boolean emailflag = firebaseUser.isEmailVerified();

        if (emailflag) {
            checkAdmin();
            checkPresident();
            load();
        } else {
            Toast.makeText(MainActivity.this, "Please verify your email.", Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
        dialog.cancel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        dialog.dismiss();
        dialog.cancel();
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }
    private boolean checkPlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.makeGooglePlayServicesAvailable(this);
                if (errorDialog == null) {
                    errorDialog = googleApiAvailability.getErrorDialog(this, resultCode, 2404);
                    errorDialog.setCancelable(false);
                }

                if (!errorDialog.isShowing())
                    errorDialog.show();

            }
        }

        return resultCode == ConnectionResult.SUCCESS;
    }
}







