package com.makerz.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.makerz.MenuActivity;
import com.makerz.model.FBData;

import java.util.ArrayList;
import java.util.HashMap;

// DatabaseHelper is used to save the facebook pictures that is downloaded upon refresh is triggered
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MyDatabase.db";
    private static final String IMAGE_TABLENAME = "imageTable";
    private static final String ID = "ID";
    private static final String MESSAGE = "MESSAGE";
    private static final String IMAGE = "IMAGE";
    private static final String CREATED_TIME = "CREATEDTIME";
    private HashMap hp;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TRANSACTION_TABLE = "CREATE TABLE IF NOT EXISTS " + IMAGE_TABLENAME + "("
                + IMAGE + " BLOB,"
                + CREATED_TIME + " TEXT,"
                + ID + " TEXT PRIMARY KEY,"
                + MESSAGE + " TEXT);";
        db.execSQL(CREATE_TRANSACTION_TABLE);
    }

    public void delete(Context context) {
        context.deleteDatabase(DATABASE_NAME);
        Log.d("Delete", "Database  " + DATABASE_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS transactiontable");
        onCreate(db);
    }

    public boolean insertPost(String Image, String Message, String Id, String CreatedTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(IMAGE, Image);
        contentValues.put(MESSAGE, Message);
        contentValues.put(ID, Id);
        contentValues.put(CREATED_TIME, CreatedTime);
        db.insert(IMAGE_TABLENAME, null, contentValues);
        db.close();
        Log.v("Message", Message);
        return true;
    }

    public ArrayList<FBData> getAllPosts() {

        ArrayList<FBData> posts = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from imageTable order by " + CREATED_TIME, null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            String image = (res.getString(res.getColumnIndex(IMAGE)));
            String message = (res.getString(res.getColumnIndex(MESSAGE)));
            String id = (res.getString(res.getColumnIndex(ID)));
            String createdTime = (res.getString(res.getColumnIndex(CREATED_TIME)));
            Log.d("Image", image);
            posts.add(new FBData(image, message, id, createdTime));
            res.moveToNext();
        }
        res.close();
        db.close();
        return posts;
    }
}
