package com.koushikdutta.timemachine;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class BackupEntry extends BackupEntryBase {
    public JSONObject info;
    public String packageName;
    public long newest;
    public PackageInfo packageInfo;
    private ArrayList<BackupEntry> backups;
    public int versionCode;
    
    public ArrayList<BackupEntry> getBackups() {
        if (backups == null) {
            backups = new ArrayList<BackupEntry>();
            File backupDir = new File(String.format("%s/%s", Helper.BACKUP_DIR, packageName));
            File[] dirs = backupDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    try {
                        if (!pathname.isDirectory())
                            return false;
                        Long.parseLong(pathname.getName());
                        return true;
                    }
                    catch (Exception ex) {
                        return false;
                    }
                }
            });
            for (File dir: dirs) {
                try {
                    BackupEntry be = from(dir, drawable);
                    if (be != null)
                        backups.add(be);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
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
    
    public static BackupEntry from(File dir, Drawable useBmp) {
        try {
            File metadata = new File(dir.getAbsolutePath() + "/metadata.json");
            File icon = new File(dir.getAbsolutePath() + "/icon.png");
            JSONObject info = new JSONObject(StreamUtility.readFile(metadata));
            Drawable drawable = useBmp;
            if (drawable == null) {
                FileInputStream fin = new FileInputStream(icon);
                Bitmap bmp = BitmapFactory.decodeStream(fin);
                drawable = new BitmapDrawable(bmp);
                fin.close();
            }
            
            long newest = Long.parseLong(dir.getName());
            return BackupEntry.from(info, drawable, newest);
        }
        catch (Exception ex) {
            return null;
        }
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
