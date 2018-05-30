package com.servilat.keepintouch;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AlertDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.vk.sdk.VKUIHelper.getApplicationContext;

public class Util {
    public static final String executeCode = "var offset = %d;" +
            "var dialogs = [];" +
            "var posts = API.messages.getDialogs({\"count\": 15, \"offset\" : offset});" +
            "var i = 0;" +
            "var count = posts.count;" +
            "while(i<posts.items.length) {" +
            "if(posts.items[i].message.chat_id!=null) {" +
            "dialogs.push(posts.items[i].message);" +
            "} else {" +
            "var user = API.users.get({\"user_ids\":  posts.items[i].message.user_id, \"fields\": \"photo_100\"});" +
            "var temp = posts.items[i].message + user[0];" +
            "dialogs.push(temp);" +
            "}" +
            "i = i +1;" +
            "}" +
            "return {\"dialogs\": dialogs, \"count\": count};";

    public static boolean isConnectedToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    public static void showAlertMessage(Activity activity, String title, String message) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(activity, android.R.style.Theme_Holo_Light);
        }
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    public static String convertTime(long unixTime) {
        Date date = new Date(unixTime * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        /*sdf.setTimeZone(TimeZone.getTimeZone("GMT+3"));*/
        return sdf.format(date);
    }

}
