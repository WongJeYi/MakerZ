package com.makerz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

// This activity shows info of the current user.

public class ProfileActivity extends AppCompatActivity {

    String userId;
    String text = "";

    private FirebaseAuth firebaseAuth;
    private DatabaseReference myRef;
    private StorageReference imageReference;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser currentUser;

    private ImageView profilePic;
    private TextView Codename;
    private TextView Email;
    private Button Edit;
    private Button ChangePassword;
    private Button Back;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Declaration of elements.
        setupUIViews();

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference("Images");

        // Get current user's info.
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        userId = firebaseAuth.getCurrentUser().getUid();

        firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
        imageReference = storageReference.child("Profile Picture").child(userId + "/" + "profilepic.jpg");

        progressDialog = new ProgressDialog(ProfileActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        // When Button Edit is pressed, will move to Editctivity.
        Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, EditActivity.class));
                finish();
            }
        });

        // When Button ChangePassword is pressed, will move to ChangePasswordActivity.
        ChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
                finish();
            }
        });

        // When Button Back is pressed, will move to MenuActivity.
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);

// Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor myEdit = spref.edit();

        if (spref.getBoolean("FirstTimeProfile", true)) {
            myEdit.putBoolean("FirstTimeProfile", false);
            myEdit.commit();
            newUser();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(ProfileActivity.this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        } else {
            //updateUserStatus("online");
            VerifyUserExistance();
        }
    }

    // Listen to current user's data values.
    private void VerifyUserExistance() {

        final String userId = firebaseAuth.getCurrentUser().getUid();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                Codename.setText("Codename: " + dataSnapshot.child("Users").child(userId).child("codename").getValue(String.class));
                Email.setText("Email: " + dataSnapshot.child("Users").child(userId).child("email").getValue(String.class));

                    progressDialog.dismiss();

                    // If TextView Codename is pressed,
                    // the textview will switch to display user's full name when current textview is codename.
                    // & vice versa.
                    Codename.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Codename.getText().toString().equals("Codename: " + dataSnapshot.child("Users").child(userId).child("codename").getValue(String.class))) {
                            Codename.setText("Full Name: " + dataSnapshot.child("Users").child(userId).child("fullname").getValue(String.class));

                        } else if (Codename.getText().toString().equals("Full Name: " + dataSnapshot.child("Users").child(userId).child("fullname").getValue(String.class))) {
                            Codename.setText("Codename: " + dataSnapshot.child("Users").child(userId).child("codename").getValue(String.class));
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }
        });

        // Set picture
        // Load profile image from firebase database to ImageView imageReference
        imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).fit().centerCrop().into(profilePic);
            }
        });

    }
    private void newUser(){
        TapTargetView.showFor(ProfileActivity.this, TapTarget.forView(Edit, "Click here to edit profile", "You can edit your profile here")
                .outerCircleColor(R.color.colorPrimary)
                .outerCircleAlpha(0.96f)
                .targetCircleColor(R.color.browser_actions_bg_grey)
                .titleTextColor(R.color.colorPrimaryDark)
                .titleTextSize(30)
                .descriptionTextSize(20)
                .descriptionTextColor(R.color.colorPrimaryDark)
                .textTypeface(Typeface.SANS_SERIF)
                .drawShadow(true)
                .cancelable(false)
                .tintTarget(true)
                .dimColor(R.color.colorPrimary)
                .targetRadius(60)
                .transparentTarget(true), new TapTargetView.Listener() {
            @Override
            public void onTargetClick(TapTargetView view) {
                super.onTargetClick(view);
                view.dismiss(true);
                startActivity(new Intent(ProfileActivity.this,EditActivity.class));
                finish();
            }
        });
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            //updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            //updateUserStatus("offline");
        }
    }

    private void setupUIViews() {

        profilePic = (ImageView) findViewById(R.id.profile_pic_1);
        Codename = (TextView) findViewById(R.id.text_codename);
        Email = (TextView) findViewById(R.id.text_email);
        Edit = (Button) findViewById(R.id.button_edit);
        ChangePassword = (Button) findViewById(R.id.button_change_password);
        Back = (Button) findViewById(R.id.button_backz);

    }
}

