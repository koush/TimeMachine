package com.koushikdutta.timemachine;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;

public class Helper {
    static public void showAlertDialog(Context context, int stringResource)
    {
        try {
            AlertDialog.Builder builder = new Builder(context);
            builder.setMessage(stringResource);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.create().show();
        }
        catch(Exception ex) {
            
        }
    }
    
    static public void showAlertDialogWithTitle(Context context, int titleResource, int stringResource)
    {
        AlertDialog.Builder builder = new Builder(context);
        builder.setTitle(titleResource);
        builder.setMessage(stringResource);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }
}
