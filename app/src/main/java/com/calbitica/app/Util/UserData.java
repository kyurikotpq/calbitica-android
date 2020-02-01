package com.calbitica.app.Util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class UserData {
    public static boolean save(HashMap<String, String> data, Context context) {
        // save to shared pref
        boolean successful = true;
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences("user", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            for (String key : data.keySet()) {
                String value = data.get(key);
                editor.putString(key, value);
            }

            editor.apply(); // BG thread
        } catch (Exception e) {
            successful = false;
        }
        return successful;
    }

    public static String get(String key, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", MODE_PRIVATE);
        String value = sharedPreferences.getString(key, null); // defValue: Default value

        return value;
    }

    public static boolean clearAll(Context context) {
        boolean successful = true;
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            editor.clear();
            editor.apply();
        } catch (Exception e) {
            successful = false;
        }
        return successful;
    }
}
