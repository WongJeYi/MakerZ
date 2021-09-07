package com.makerz.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.Layout;
import android.view.LayoutInflater;

import com.makerz.R;

// LoadingDialog with animation.

public class LoadingDialog {
   private Activity activity;
   private AlertDialog dialog;

    public LoadingDialog(Activity myActivity){
        activity = myActivity;
    }

    public void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_progress_bar, null));
        builder.setCancelable(true);

        dialog = builder.create();
        dialog.show();
    }

    public void dismissDialog(){
        dialog.dismiss();
    }
}
