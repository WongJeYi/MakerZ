package com.makerz.util;

import com.google.firebase.database.FirebaseDatabase;
// This class is for offline firebase database purpose
public class FirebaseUtil {
        private static FirebaseDatabase mDatabase;

        public static FirebaseDatabase getInstance() {
            if (mDatabase == null) {
                mDatabase = FirebaseDatabase.getInstance();
                mDatabase.setPersistenceEnabled(true);
            }
            return mDatabase;
        }

}
