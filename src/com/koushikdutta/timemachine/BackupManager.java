package com.koushikdutta.timemachine;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Hashtable;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;

public class BackupManager {
    private BackupManager() {
    }

    static int[] mOld;
    static int[] mNew;
    
    private static int[] from(int color) {
        return new int[] { Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color) };
    }

    private static BackupManager mInstance;
    public static BackupManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BackupManager();
            mOld = from(context.getResources().getColor(R.color.backup_old));
            mNew = from(context.getResources().getColor(R.color.backup_new));
            mInstance.refreshBackups();
        }
        return mInstance;
    }
    
    public static int getColor(Context context, String packageName) {
        BackupManager manager = getInstance(context);
        BackupEntry entry = manager.backups.get(packageName);
        if (entry == null)
            return 0;
        return entry.getColor();
    }
    
    public Hashtable<String, BackupEntry> backups = new Hashtable<String, BackupEntry>();
    public Hashtable<String, BackupEntryGroup> groups = new Hashtable<String, BackupEntryGroup>();

    public void refreshBackups() {
        backups.clear();
        File backupDir = new File(Helper.BACKUP_DIR);
        // find all the metadata.json and populate the ui
        for (File dir: Helper.getDirectories(backupDir)) {
            try {
                File[] appBackups = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        if (!pathname.isDirectory())
                            return false;
                        try {
                            Long.parseLong(pathname.getName());
                        }
                        catch (Exception e) {
                            return false;
                        }
                        return true;
                    }
                });
                
                if (appBackups == null || appBackups.length == 0)
                    continue;
                Arrays.sort(appBackups);

                File metadata = new File(dir.getAbsolutePath() + "/metadata.json");
                File icon = new File(dir.getAbsolutePath() + "/icon.png");
                JSONObject info = new JSONObject(StreamUtility.readFile(metadata));
                FileInputStream fin = new FileInputStream(icon);
                Bitmap bmp = BitmapFactory.decodeStream(fin);
                BitmapDrawable drawable = new BitmapDrawable(bmp);
                fin.close();
                BackupEntry b = BackupEntry.from(info, drawable, Long.parseLong(appBackups[appBackups.length - 1].getName()));
                backups.put(dir.getName(), b);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
