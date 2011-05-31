package com.koushikdutta.timemachine;

import java.io.File;
import java.io.FileFilter;
import java.math.BigInteger;
import java.security.MessageDigest;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Environment;

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
    
    static public File[] getDirectories(File file) {
        return file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
    }
    
    static public String digest(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return new BigInteger(1, md.digest(input.getBytes())).toString(16).toUpperCase();
        }
        catch (Exception e) {
            return null;
        }
    }
    
    public static final String BASE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/clockworkmod/timemachine";
    public static final String BACKUP_DIR = BASE_DIR + "/backups";
    public static final String ASSETS_DIR = BASE_DIR + "/assets";
}
