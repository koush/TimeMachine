package com.koushikdutta.timemachine;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;

public class BackupManager {
    Context mContext;
    private BackupManager(Context context) {
        mContext = context;
    }

    static int[] mOld;
    static int[] mNew;
    
    private static int[] from(int color) {
        return new int[] { Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color) };
    }

    private static BackupManager mInstance;
    public static BackupManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BackupManager(context.getApplicationContext());
            mOld = from(context.getResources().getColor(R.color.backup_old));
            mNew = from(context.getResources().getColor(R.color.backup_new));
            mInstance.refresh();
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

    public void refresh() {
        refreshGroups();
        refreshBackups();
    }
    
    public void saveGroups() {
        JSONObject config;
        try {
            config = StreamUtility.readJSON(Helper.CONFIG_FILE);
        }
        catch (Exception ex) {
            config = new JSONObject();
        }
        
        try {
            JSONObject groupsJson = new JSONObject();
            config.put("groups", groupsJson);
            for (BackupEntryGroup bg: groups.values()) {
                JSONObject group = new JSONObject();
                group.put("name", bg.name);
                JSONArray packages = group.optJSONArray("packages");
                if (packages == null) {
                    packages = new JSONArray();
                    group.put("packages", packages);
                }
                for (String pkg: bg.packages) {
                    packages.put(pkg);
                }
                BitmapDrawable bmp = (BitmapDrawable)bg.drawable;
                File iconFile = new File(Helper.ICON_DIR + "/" + bg.name + ".png");
                iconFile.getParentFile().mkdirs();
                FileOutputStream fout = new FileOutputStream(iconFile);
                bmp.getBitmap().compress(CompressFormat.PNG, 100, fout);
                fout.close();
                groupsJson.put(bg.name, group);
            }

            StreamUtility.writeFile(Helper.CONFIG_FILE, config.toString(4));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void refreshGroups() {
        try {
            groups.clear();
            JSONObject config = StreamUtility.readJSON(Helper.CONFIG_FILE);
            JSONObject groupsJson = config.optJSONObject("groups");
            if (groupsJson == null)
                return;
            Iterator<String> iter = groupsJson.keys();
            while (iter.hasNext()) {
                try {
                    String groupName = iter.next();
                    JSONObject group = groupsJson.getJSONObject(groupName);
                    JSONArray packages = group.getJSONArray("packages"); 
                    
                    BackupEntryGroup bg = new BackupEntryGroup();
                    bg.name = group.getString("name");

                    for (int i = 0; i < packages.length(); i++) {
                        String pkg = packages.getString(i);
                        bg.packages.add(pkg);
                    }
                    
                    try {
                        String icon = String.format("%s/%s.png", Helper.ICON_DIR, bg.name);
                        FileInputStream fin = new FileInputStream(icon);
                        Bitmap bmp = BitmapFactory.decodeStream(fin);
                        BitmapDrawable drawable = new BitmapDrawable(bmp);
                        bg.drawable = drawable;
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    
                    groups.put(bg.name, bg);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            try {
                BackupEntryGroup messaging = new BackupEntryGroup();
                messaging.name = mContext.getString(R.string.sms_and_mms);
                messaging.drawable = mContext.getResources().getDrawable(R.drawable.ic_launcher_smsmms);
                messaging.packages.add("com.android.mms");
                messaging.packages.add("com.android.providers.telephony");
                groups.put(messaging.name, messaging);

                BackupEntryGroup social = new BackupEntryGroup();
                social.name = mContext.getString(R.string.social_networking);
                social.drawable = mContext.getResources().getDrawable(R.drawable.ic_launcher_twitter);
                social.packages.add("com.twitter.android");
                social.packages.add("com.facebook.katana");
                groups.put(social.name, social);
                
                saveGroups();
            }
            catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
    }
    
    private void refreshBackups() {
        PackageManager pm = mContext.getPackageManager();
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
                try {
                     b.packageInfo = pm.getPackageInfo(b.packageName, 0);
                }
                catch (Exception ex) {
                }
                backups.put(dir.getName(), b);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
