package com.makerz.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makerz.ChatActivity;
import com.makerz.R;

import java.util.ArrayList;

// Pop-up window for ChatActivity.
// This is used to fill in details such as group name to create private/public groups.

public class ExampleDialog extends AppCompatDialogFragment {

    String groupName;
    String type="";
    int verify = 0;

    private EditText groupNameField;
    private RadioButton privateButton, publicButton;
    private ExampleDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.myDialogTheme);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view  = inflater.inflate(R.layout.custom_dialog, null);

        groupNameField = view.findViewById(R.id.group_chat_name);
        privateButton = view.findViewById(R.id.radio_private);
        publicButton = view.findViewById(R.id.radio_public);

        builder.setView(view)
                .setTitle("Enter Group Name :");


        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                verify = verify + 1;

                groupName = groupNameField.getText().toString();

                if (privateButton.isChecked())
                {
                    type = "Private";
                }
                if (publicButton.isChecked())
                {
                    type = "Public";
                }

                // Only when all the details (groupname & checker for either public or private group) are filled,
                // will be able to create groups.
                // Else different situation, will have different toast to tell users about what they are missing.
               if (type.equals("") && groupName.isEmpty())
               {
                   Toast.makeText(view.getContext(), "Please fill in the details.", Toast.LENGTH_SHORT).show();

               }
               else if (!type.equals("") && groupName.isEmpty())
               {
                   Toast.makeText(view.getContext(), "Please enter your group name.", Toast.LENGTH_SHORT).show();

               }
               else if (type.equals("") && !groupName.isEmpty())
               {
                   Toast.makeText(view.getContext(), "Please select your group type to be private or public to other users.", Toast.LENGTH_SHORT).show();
               }
               else if (!type.equals("") && !groupName.isEmpty())
               {
                       listener.applyTexts(groupName,type);
               }

               }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try
        {
            listener = (ExampleDialogListener) context;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString() +
                    "must implement ExampleDiaglogListener");
        }
    }

    public interface ExampleDialogListener
    {
       void applyTexts(String groupName, String type);
    }
}
