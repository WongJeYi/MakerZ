package com.makerz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

// This activity is used to display and commemorate MakerZ's first committee members.

public class MakerZ2Activity extends AppCompatActivity {

    private Button Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maker_z2);

        Back = (Button) findViewById(R.id.button_back2);

        // When Button Back is pressed, will move to MakerZActivity.
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}
