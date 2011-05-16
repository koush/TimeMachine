package com.koushikdutta.timemachine;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BackupActivity extends Activity {
    
    class SingleApplicationInfo
    {
        ApplicationInfo info;
        public Drawable drawable;
        public String name;
        public SingleApplicationInfo(ApplicationInfo info, PackageManager pm) {
            this.info = info;
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
                ApplicationInfo ai = pm.getApplicationInfo(p, 0);
                mAdapter.add(new SingleApplicationInfo(ai, pm));
            }
            catch (Exception e) {
            }
        }
        
        ListView lv = (ListView)findViewById(R.id.list);
        lv.setAdapter(mAdapter);
        
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
                            SingleApplicationInfo sinfo = mAdapter.getItem(current);
                            appName.setText(sinfo.name);
                            appIcon.setImageDrawable(sinfo.drawable);
                            Context pkgContext = createPackageContext(sinfo.info.packageName, 0);
                            String apk = pkgContext.getPackageCodePath();
                            suRunner.mEnvironment.put("OUTPUT_DIR", String.format("%s/clockworkmod/timemachine/%s/%d", Environment.getExternalStorageDirectory().getAbsolutePath(), sinfo.info.packageName, time));
                            suRunner.mEnvironment.put("PACKAGE_NAME", sinfo.info.packageName);
                            suRunner.mEnvironment.put("PACKAGE_APK", apk);
                            suRunner.addCommand(String.format("%s/backup.sh", getFilesDir().getAbsolutePath()));
                            suRunner.runSuCommandAsync(BackupActivity.this, new SuCommandCallback() {
                                @Override
                                public void onResult(Integer result) {
                                    System.out.println(result);
                                    current++;
                                    run();
                                }
                                
                                @Override
                                public void onOutputLine(String line) {
                                    System.out.println(line);
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
