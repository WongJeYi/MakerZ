package com.makerz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

// This activity is used to display MakerZ's vision and mission.
// Also shows the founder name of MakerZ.

public class MakerZActivity extends AppCompatActivity {

    private Button Back;
    private ImageView Logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maker_z);

        Back = (Button) findViewById(R.id.button_back1);
        Logo = (ImageView) findViewById(R.id.logo);

        // When Button Back is pressed, will move to MenuActivity.
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // When ImageView Logo is pressed, will move to MakerZActivity.
        Logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MakerZActivity.this, MakerZ2Activity.class));

            }
        });

    }
}
