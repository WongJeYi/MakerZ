package com.makerz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class AdminActivity extends AppCompatActivity {

    private Button backZ;
    private Button add;
    private Button remove;
    private ListView mListView;
    private EditText Codename;
    private EditText Password;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private List<String> admin;
    private ArrayAdapter<String> mArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        backZ = findViewById(R.id.button_back);
        add = findViewById(R.id.button_add);
        remove = findViewById(R.id.button_remove);
        mListView = findViewById(R.id.listView);
        Codename = findViewById(R.id.admin_codename);
        Password = findViewById(R.id.admin_password);
        Codename.setEnabled(true);
        Password.setEnabled(true);
        backZ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String codename = Codename.getText().toString();
                String password = Password.getText().toString();
                validate(codename, password, "add");

            }
        });
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String codename = Codename.getText().toString();
                String password = Password.getText().toString();
                validate(codename, password, "remove");
            }
        });
        backZ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        admin = new ArrayList<String>();
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, admin);
        mListView.setAdapter(mArrayAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.signOut();
        }
    }

    private void validate(final String Codename, String Password, final String stat) {

        progressDialog.setMessage("Loading...");
        progressDialog.show();
        if (!Codename.isEmpty() && !Password.isEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(Codename, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        if (firebaseAuth.getCurrentUser() != null) {
                            if (stat.equals("add")) {
                                disablePassword();
                                listAdmin();
                                addAdmin(firebaseAuth.getCurrentUser().getEmail());
                            }
                            if (stat.equals("remove")) {
                                disablePassword();
                                listAdmin();
                                removeAdmin(firebaseAuth.getCurrentUser().getEmail());
                            }
                        }

                    } else {
                        Toast.makeText(AdminActivity.this, "User not found.", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();

                }
            });
        } else if (firebaseAuth.getCurrentUser() != null && !Codename.isEmpty()) {
            if (stat.equals("add")) {
                if (Codename.equals("President")) {
                    if (firebaseAuth.getCurrentUser() != null && (Codename.equals("President"))) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this, R.style.AlertDialog);
                        final EditText input = new EditText(AdminActivity.this);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        input.setLayoutParams(lp);
                        builder.setView(input);
                        builder.setIcon(R.drawable.email);
                        builder.setTitle("President, enter your email.");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String email = input.getText().toString();
                                setPresident(email);
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.show();
                    }
                } else {
                    addAdmin(Codename);
                }
            }
            if (stat.equals("remove")) {
                removeAdmin(Codename);
            }
        } else {
            progressDialog.dismiss();
            Toast.makeText(AdminActivity.this, "Input must not be empty.", Toast.LENGTH_SHORT).show();
        }

    }

    private void setPresident(final String email) {
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            final DatabaseReference databaseRefer = firebaseDatabase.getReference().child("President");

            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    databaseRefer.setValue(email);
                    Toast.makeText(AdminActivity.this, "President email is set as : "+email, Toast.LENGTH_SHORT).show();
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        // add listener
        databaseRefer.addListenerForSingleValueEvent(postListener);
        }else{
            Toast.makeText(AdminActivity.this, "No connection.", Toast.LENGTH_SHORT).show();
        }
        progressDialog.dismiss();
    }

    private void disablePassword() {
        Password.setVisibility(View.INVISIBLE);
        Password.getText().clear();
    }

    private void removeAdmin(String name) {
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final String email = name;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // get database reference
            final DatabaseReference databaseRefer = firebaseDatabase.getReference().child("Admin");
            // database listener
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // check event exists
                    if (dataSnapshot.getValue() != null) {
                        ArrayList<String> admin = (ArrayList<String>) dataSnapshot.getValue();
                        if (admin != null) {
                            if (admin.contains(email)) {
                                Iterator<String> itr = admin.iterator();
                                while (itr.hasNext()) {
                                    String ad = itr.next();
                                    if (ad.equals(email)) {
                                        itr.remove();
                                    }
                                }
                                for (String ad : admin) {
                                    Log.e("ADMIN", ad);
                                }
                                databaseRefer.setValue(admin);
                                Toast.makeText(AdminActivity.this, "Successfully removed admin.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminActivity.this, "Admin not exists.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AdminActivity.this, "Admin not exists.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            // add listener
            databaseRefer.addListenerForSingleValueEvent(postListener);

        } else {
            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show();
        }


    }

    private void listAdmin() {
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // get database reference
            final DatabaseReference databaseRefer = firebaseDatabase.getReference().child("Admin");
            // database listener
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // check event exists
                    if ((ArrayList<String>) dataSnapshot.getValue() == null) {

                    } else {
                        admin = (ArrayList<String>) dataSnapshot.getValue();
                        Log.e("ADMIN", "j");
                        mArrayAdapter.notifyDataSetChanged();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            // add listener
            databaseRefer.addListenerForSingleValueEvent(postListener);

        } else {
            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show();
        }


    }

    private void addAdmin(final String email) {
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // get database reference
            final DatabaseReference databaseRefer = firebaseDatabase.getReference().child("Admin");
            // database listener
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // check event exists
                    ArrayList<String> admin;
                    if ((ArrayList<String>) dataSnapshot.getValue() == null) {
                        admin = new ArrayList<String>();
                    } else {
                        admin = (ArrayList<String>) dataSnapshot.getValue();
                    }
                    if (admin.contains(email)) {
                        Toast.makeText(AdminActivity.this, email + " is already an admin.", Toast.LENGTH_SHORT).show();
                    } else {
                        admin.add(email);
                        databaseRefer.setValue(admin);
                        Toast.makeText(AdminActivity.this, "Successfully added admin.", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            // add listener
            databaseRefer.addListenerForSingleValueEvent(postListener);

        } else {
            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show();
        }
        progressDialog.dismiss();


    }

}
