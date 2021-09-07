package com.makerz.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

// This class is used in MenuActivity and AddNoteActivity
// to download image as AsyncTask,
// there will be a callback to a listener by overriding at the MenuActivity and AddNoteActivity to return value
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    private OnTaskCompleted listener;
    String encodedString = "";

    public DownloadImageTask(ImageView bmImage,OnTaskCompleted listener) {
        this.bmImage = bmImage;
        this.listener=listener;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            // download image
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
            // image could be in the form of encoded base64
            if(urldisplay.split(",").length>1) {
                String base64Image = urldisplay.split(",")[1];
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                mIcon11 = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }
        }
        // check bitmap size
        // database will reject if image size more than 2MB
        if(mIcon11!=null) {
            if (sizeOf(mIcon11) > 2000000) {
                double scale = 2000000.0 / sizeOf(mIcon11);
                Log.d("Scale", String.valueOf(sizeOf(mIcon11)));
                Log.d("Scale", String.valueOf(scale));
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(mIcon11, (int) (mIcon11.getWidth() * scale), (int) (mIcon11.getHeight() * scale), true);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                mIcon11 = scaledBitmap;
                byte[] imageBytes = stream.toByteArray();
                encodedString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            } else {
                // encode to string
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mIcon11.compress(Bitmap.CompressFormat.PNG, 50, stream);
                byte[] imageBytes = stream.toByteArray();
                encodedString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            }

            return mIcon11;
        }else{
            return null;
        }
    }

    protected void onPostExecute(Bitmap result) {
        if(result!=null) {
            bmImage.setImageBitmap(result);
            listener.onTaskCompleted(result, encodedString);
        }

    }
    protected int sizeOf(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        } else {
            return data.getByteCount();
        }
    }

}