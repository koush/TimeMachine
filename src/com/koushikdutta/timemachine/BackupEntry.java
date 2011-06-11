package com.koushikdutta.timemachine;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

public class BackupEntry extends BackupEntryBase {
    public JSONObject info;
    public String packageName;
    public long newest;
    public PackageInfo packageInfo;
    private ArrayList<BackupRecord> backups;
    public int versionCode;
    
    public ArrayList<BackupRecord> getBackups() {
        if (backups == null) {
            
        }
        return backups;
    }
    
    private BackupEntry() {
    }
    public static BackupEntry from(JSONObject info, Drawable drawable, long newest) throws JSONException {
        String name = info.getString("name");
        String packageName = info.getString("packageName");
        int versionCode = info.getInt("versionCode");
        BackupEntry ret = new BackupEntry();
        ret.info = info;
        ret.drawable = drawable;
        ret.name = name;
        ret.packageName = packageName;
        ret.newest = newest;
        ret.versionCode = versionCode;
        return ret;
    }
    
    private static long ONE_WEEK = 7L * 24L * 60L * 60L * 1000L;
    
    @Override
    public int getColor() {
        int[] ret = new int[4];
        
        long weekAgo = System.currentTimeMillis() - ONE_WEEK;
        long time = Math.min(System.currentTimeMillis(), newest);
        time = Math.max(weekAgo, time);
        long diff = time - weekAgo;
        float f = (float)diff / (float)ONE_WEEK;
        
        for (int i = 0; i < 4; i++) {
            ret[i] = BackupManager.mOld[i] + (int)((BackupManager.mNew[i] - BackupManager.mOld[i]) * f);
        }
        
        return Color.argb(ret[0], ret[1], ret[2], ret[3]);
    }
    
    @Override
    public String getUniqueName() {
        return packageName;
    }
}
