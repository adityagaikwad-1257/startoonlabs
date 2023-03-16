package com.adi.startoonlabs.apputil;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.adi.startoonlabs.R;

public class AppUtil {
    public static void closeKeyboard(Activity activity){
        if (activity != null){
            View view = activity.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)activity.getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static boolean isConnectedToInternet(Context context){
        if (!checkInternetConnection(context)){
            // not connected to internet
            // showing Error dialog

            String title = "YOU ARE OFFLINE";
            String message = "It seems like you are not connected to the Internet.\nKindly check and try again.";
            int rawRes = R.raw.no_internet; // no internet illustration

            ErrorDialog.create(context).show(rawRes, title, message, "ok", null);
            return false;
        }

        return true;
    }

    /*
        check internet connection
     */
    private static boolean checkInternetConnection(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr != null) {
            NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

            if (activeNetworkInfo != null) { // connected to the internet
                // connected to the mobile provider's data plan
                if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    return true;
                } else return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            }
        }
        return false;
    }
}
