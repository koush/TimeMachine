package com.koushikdutta.timemachine;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

public class BackupEntry extends BackupEntryBase {
    public JSONObject info;
    public String packageName;
    public long newest;
    
    private BackupEntry() {
    }
    public static BackupEntry from(JSONObject info, Drawable drawable, long newest) throws JSONException {
        String name = info.getString("name");
        String packageName = info.getString("packageName");
        BackupEntry ret = new BackupEntry();
        ret.info = info;
        ret.drawable = drawable;
        ret.name = name;
        ret.packageName = packageName;
        ret.newest = newest;
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
