package com.sample.login;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class MTUtilities {
    private static final String TAG = "MEUtilities";
    /**
     * Method to check the network connection
     *
     * @return Return the connection status
     */
    public static Boolean checkNetworkConnection(Context context) {
        ConnectivityManager connectivityManager;
        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                return true;
            }
        } catch (Exception exception) {
            Log.d(TAG, "CheckConnectivity Exception: " + exception.getMessage());
        }
        return false;
    }
}