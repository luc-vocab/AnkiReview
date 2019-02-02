package com.luc.ankireview;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {

    public static String convertStreamToString(InputStream is) {
        String contentOfMyInputStream = "";
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is), 4096);
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            contentOfMyInputStream = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contentOfMyInputStream;
    }

    public static void reportAnkiAPIException(final AppCompatActivity activity, Exception e) {
        new AlertDialog.Builder(activity)
                .setTitle("Couldn't call AnkiDroid API")
                .setMessage("Unable to retrieve data from AnkiDroid. Please export your deck and send it to " +
                        activity.getString(R.string.developer_email) + " to help resolve the issue (" + e.getMessage() + ")")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        activity.finish();
                    }
                })
                .show();
    }
}
