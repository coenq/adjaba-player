package com.rnd.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {
    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        return prefs.getString("token", null);
    }
}
