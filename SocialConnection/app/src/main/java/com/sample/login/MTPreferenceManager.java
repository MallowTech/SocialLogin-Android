package com.sample.login;

import android.content.Context;
import android.content.SharedPreferences;

public class MTPreferenceManager {
    public static void setUserId(String Session_id, Context context) {
        SharedPreferences preferences;
        preferences = context.getSharedPreferences("settings",
                Context.MODE_PRIVATE);
        preferences.edit().putString("Session_Id_", Session_id).commit();
    }

    public static String getUserId(Context context) {
        SharedPreferences preferences;
        preferences = context.getSharedPreferences("settings",
                Context.MODE_PRIVATE);
        return preferences.getString("Session_Id_", "");
    }
}
