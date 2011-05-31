package com.koushikdutta.timemachine;

import java.io.FileOutputStream;
import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BackupActivity extends Activity {
    
    private static class SingleApplicationInfo
    {
        ApplicationInfo info;
        PackageInfo pinfo;
        public Drawable drawable;
        public String name;
        public SingleApplicationInfo(PackageInfo pinfo, PackageManager pm) {
            this.pinfo = pinfo;
            info = pinfo.applicationInfo;
            name = info.loadLabel(pm).toString();
            drawable = info.loadIcon(pm);
        }
    }
    
    static class ApplicationInfoAdapter extends ArrayAdapter<SingleApplicationInfo>
    {
        LayoutInflater mInflater;
        public ApplicationInfoAdapter(Context context) {
            super(context, 0);
            mInflater = LayoutInflater.from(context);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.confirm_appinfo, null);
            
            final SingleApplicationInfo info = getItem(position);
            ImageView image = (ImageView)convertView.findViewById(R.id.icon);
            TextView name = (TextView)convertView.findViewById(R.id.name);
            name.setText(info.name);
            image.setImageDrawable(info.drawable);
            
            return convertView;
        }
    }
    
    ApplicationInfoAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup);
        
        final PackageManager pm = getPackageManager();
        ArrayList<String> packages = getIntent().getStringArrayListExtra("packages");
        
        mAdapter = new ApplicationInfoAdapter(this);
        for (String p: packages) {
            try {
                PackageInfo pi = pm.getPackageInfo(p, 0);
                mAdapter.add(new SingleApplicationInfo(pi, pm));
            }
            catch (Exception e) {
            }
        }
        
        final ImageView groupIcon = (ImageView)findViewById(R.id.application_group_icon);
        groupIcon.setImageDrawable(mAdapter.getItem(0).drawable);
        
        ListView lv = (ListView)findViewById(R.id.list);
        lv.setAdapter(mAdapter);
        
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                SingleApplicationInfo info  = mAdapter.getItem(position);
                groupIcon.setImageDrawable(info.drawable);
            }
        });
        
        Button startBackup = (Button)findViewById(R.id.start_backup);
        startBackup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final long time = System.currentTimeMillis();
                AlertDialog.Builder builder = new Builder(BackupActivity.this);
                builder.setCancelable(false);
                View content = getLayoutInflater().inflate(R.layout.backup_progress, null);
                builder.setView(content);
                builder.setTitle(R.string.performing_backup);
                final Dialog progress = builder.create();
                final ImageView appIcon = (ImageView)content.findViewById(R.id.application_icon);
                final TextView appName = (TextView)content.findViewById(R.id.application_name);
                final TextView appStatus = (TextView)content.findViewById(R.id.application_status);
                final ProgressBar progressBar = (ProgressBar)content.findViewById(R.id.progress);
                final Button done = (Button)content.findViewById(R.id.done);
                progressBar.setIndeterminate(false);
                progressBar.setMax(mAdapter.getCount());
                progress.show();
                
                final Runnable runner = new Runnable() {
                    {
                        done.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!finished) {
                                    finished = true;
                                }
                                else {
                                    finish();
                                }
                            }
                        });
                    }
                    
                    boolean finished = false;
                    int current = 0;
                    @Override
                    public void run() {
                        if (current >= mAdapter.getCount() || finished) {
                            finished = true;
                            done.setText(android.R.string.ok);
                            appName.setText(R.string.backup_complete);
                            appIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon));
                            appStatus.setText(getString(R.string.applications_backed_up, current));
                            progressBar.setVisibility(View.GONE);
                            return;
                        }
                        progressBar.setProgress(current);
                        
                        SuRunner suRunner = new SuRunner();
                        try {
                            final SingleApplicationInfo sinfo = mAdapter.getItem(current);
                            appName.setText(sinfo.name);
                            appIcon.setImageDrawable(sinfo.drawable);
                            final JSONObject metadata = new JSONObject();
                            final String outputDir = String.format("%s/%s/%d", Helper.BACKUP_DIR, sinfo.info.packageName, time);
                            metadata.put("installer", pm.getInstallerPackageName(sinfo.info.packageName));
                            metadata.put("name", sinfo.name);
                            metadata.put("versionCode", sinfo.pinfo.versionCode);
                            metadata.put("versionName", sinfo.pinfo.versionName);
                            metadata.put("packageName", sinfo.info.packageName);
                            suRunner.mEnvironment.put("OUTPUT_DIR", outputDir);
                            suRunner.mEnvironment.put("PACKAGE_NAME", sinfo.info.packageName);
                            suRunner.addCommand(String.format("%s/backup.sh", getFilesDir().getAbsolutePath()));
                            suRunner.runSuCommandAsync(BackupActivity.this, new SuCommandCallback() {
                                @Override
                                public void onResult(Integer result) {
                                    // need to be on the foreground thread so the surunner has a handler
                                    System.out.println(result);
                                    current++;
                                    run();
                                }
                                
                                void onStartBackground() {
                                    // do this stuff on the background to prevent ui thread blocking
                                    try {
                                        StreamUtility.writeFile(outputDir + "/metadata.json", metadata.toString(4));
                                        BitmapDrawable bmp = (BitmapDrawable)sinfo.drawable;
                                        FileOutputStream fout = new FileOutputStream(outputDir + "/icon.png");
                                        bmp.getBitmap().compress(CompressFormat.PNG, 100, fout);
                                        fout.close();
                                    }
                                    catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                
                                @Override
                                public void onOutputLine(String line) {
                                    appStatus.setText(line);
                                }
                            });
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                            current++;
                            run();
                        }
                    }
                };
                runner.run();
            }
        });
    }
}
