package com.makerz;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

// This class is used to zoom-in picture messages at ChatActivity and PrivateGroupChatActivity,
// when picture messages are clicked.

public class ZoomPicActivitiy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_pic_activitiy);

        // Zoom in function.
        PhotoView zoomView = findViewById(R.id.zoomView);

        // Load userMessageList.get(position).getMessage() as "filepath" from ChatAdapter.
        Picasso.get().load(getIntent().getExtras().get("filepath").toString()).into(zoomView);
    }
}
