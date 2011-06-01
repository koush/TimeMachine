package com.koushikdutta.timemachine;

import java.io.File;
import java.io.FileFilter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.widget.TextView;

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
    
    static public void showAlertDialogWithTitle(Context context, int titleResource, int stringResource, DialogInterface.OnClickListener positiveListener)
    {
        AlertDialog.Builder builder = new Builder(context);
        builder.setTitle(titleResource);
        builder.setMessage(stringResource);
        builder.setPositiveButton(android.R.string.ok, positiveListener);
        builder.create().show();
    }
    
    static public boolean isNullOrEmpty(CharSequence cs) {
        return cs == null || cs.toString().equals("");
    }
    
    static public boolean isNullOrEmpty(TextView tv) {
        return isNullOrEmpty(tv.getText());
    }
    
    static public void showAlertDialogWithTitle(Context context, int titleResource, int stringResource)
    {
        showAlertDialogWithTitle(context, titleResource, stringResource, null);
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
    
    static public <T> void unique(List<T> list) {
        HashSet<T> h = new HashSet<T>();
        for (T i: list) {
            h.add(i);
        }
        list.clear();
        for (T i: h) {
            list.add(i);
        }
    }
    
    public static final String BASE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/clockworkmod/timemachine";
    public static final String CONFIG_FILE = BASE_DIR + "/config.json";
    public static final String ICON_DIR = BASE_DIR + "/icons";
    public static final String BACKUP_DIR = BASE_DIR + "/backups";
    public static final String ASSETS_DIR = BASE_DIR + "/assets";
}
