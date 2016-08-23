package com.sample.login;

import android.content.Context;
import android.content.SharedPreferences;

public class MTSharedPreferenceManager {
    public static void setUserId(String UserId, Context context) {
        SharedPreferences preferences;
        preferences = context.getSharedPreferences("settings",
                Context.MODE_PRIVATE);
        preferences.edit().putString("user_id", UserId).commit();
    }

    public static String getUserId(Context context) {
        SharedPreferences preferences;
        preferences = context.getSharedPreferences("settings",
                Context.MODE_PRIVATE);
        return preferences.getString("user_id", "");
    }
}
