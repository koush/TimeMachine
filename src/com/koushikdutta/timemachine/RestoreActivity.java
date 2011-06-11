package com.koushikdutta.timemachine;

import java.util.ArrayList;
import java.util.HashSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class RestoreActivity extends Activity {
    
    // groups will be maintained in a database.
    // a group is user defined, but starts with defaults.
    // a valid restore group is when all the applications in the group have the same
    // backup timestamp.
    
    HashSet<String> mChecked = new HashSet<String>();
    HashSet<String> mDisabled = new HashSet<String>();

    static void bindCheckedState(View view, boolean checked) {
        view.setBackgroundColor(checked ? view.getResources().getColor(R.color.checked_application_background) : 0);
    }

    class BackupEntryAdapter<T extends BackupEntryBase> extends ArrayAdapter<T>
    {
        LayoutInflater mInflater;
        public BackupEntryAdapter(Context context) {
            super(context, 0);
            mInflater = LayoutInflater.from(context);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.appinfo, null);
            
            final T info = getItem(position);
            ImageView image = (ImageView)convertView.findViewById(R.id.icon);
            TextView name = (TextView)convertView.findViewById(R.id.name);
            name.setText(info.name);
            image.setImageDrawable(info.drawable);

            View v = convertView.findViewById(R.id.age);
            v.setBackgroundColor(info.getColor());

            bindCheckedState(convertView, mChecked.contains(info.getUniqueName()) || mDisabled.contains(info.getUniqueName()));

            return convertView;
        }
    }
    
    BackupEntryAdapter<BackupEntryGroup> mGroupsAdapter;
    BackupEntryAdapter<BackupEntry> mAllAdapter;
    SeparatedListAdapter mAdapter;

    void refreshCount() {
        final TextView restoreCount = (TextView)findViewById(R.id.restore_count);
        int count = 0;
        for (int i = 0; i < mAllAdapter.getCount(); i++) {
            BackupEntry be = mAllAdapter.getItem(i);
            if (mDisabled.contains(be.getUniqueName()) || mChecked.contains(be.getUniqueName()))
                count++;
        }
        restoreCount.setText(getString(R.string.restore_count, count));
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.restore);
        
        mAllAdapter = new BackupEntryAdapter<BackupEntry>(this);
        ListView lv = (ListView)findViewById(R.id.list);
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                BackupEntryBase be = (BackupEntryBase)mAdapter.getItem(position);
                if (mDisabled.contains(be.getUniqueName()))
                    return;
                boolean restore;
                if (restore = !mChecked.remove(be.getUniqueName())) {
                    mChecked.add(be.getUniqueName());
                }
                bindCheckedState(view, restore);
                mDisabled.clear();
                
                for (int i = 0; i < mGroupsAdapter.getCount(); i++) {
                    BackupEntryGroup batch = mGroupsAdapter.getItem(i);
                    if (!mChecked.contains(batch.getUniqueName()))
                        continue;
                    for (String pkg: batch.packages) {
                        mDisabled.add(pkg);
                    }
                }
                
                mAdapter.notifyDataSetChanged();
                refreshCount();
            }
        });

        mGroupsAdapter = new BackupEntryAdapter<BackupEntryGroup>(this);
        
        refreshBackups();
        
        
        mAdapter = new SeparatedListAdapter(this);
        mAdapter.addSection(getString(R.string.application_groups), mGroupsAdapter);
        mAdapter.addSection(getString(R.string.all_applications), mAllAdapter);
        
        Button clear = (Button)findViewById(R.id.clear);
        clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDisabled.clear();
                mChecked.clear();
                mAdapter.notifyDataSetChanged();
                refreshCount();
            }
        });    
        
        lv.setAdapter(mAdapter);

        Button restore = (Button)findViewById(R.id.restore);
        restore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new Builder(RestoreActivity.this);
                builder.setCancelable(false);
                View content = getLayoutInflater().inflate(R.layout.restore_progress, null);
                builder.setView(content);
                builder.setTitle(R.string.performing_restore);
                final Dialog progress = builder.create();
                final ImageView appIcon = (ImageView)content.findViewById(R.id.application_icon);
                final TextView appName = (TextView)content.findViewById(R.id.application_name);
                final TextView appStatus = (TextView)content.findViewById(R.id.application_status);
                final ProgressBar progressBar = (ProgressBar)content.findViewById(R.id.progress);
                final Button done = (Button)content.findViewById(R.id.done);
                final ArrayList<BackupEntry> toRestore = new ArrayList<BackupEntry>();
                
                for (int i = 0; i < mAllAdapter.getCount(); i++) {
                    BackupEntry be = mAllAdapter.getItem(i);
                    if (mDisabled.contains(be.getUniqueName()) || mChecked.contains(be.getUniqueName())) {
                        toRestore.add(be);
                    }
                }
                
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

                        try {
                            final SuRunner suRunner = new SuRunner();
                            final SuCommandCallback callback = new SuCommandCallback() {
                                @Override
                                public void onResult(Integer result) {
                                    run();
                                }
                            };
                            BackupEntry be = toRestore.get(current);
                            if (be.versionCode == be.packageInfo.versionCode) {
                                // no op
                                suRunner.runSuCommandAsync(RestoreActivity.this, callback);
                            }
                            else if (be.versionCode < be.packageInfo.versionCode) {
                                AlertDialog.Builder builder = new Builder(RestoreActivity.this);
                                builder.setCancelable(false);
                                builder.setTitle(R.string.confirm_install);
                                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        suRunner.runSuCommandAsync(RestoreActivity.this, callback);
                                    }
                                });
                                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        run();
                                    }
                                });
                            }
                            else {
                                // install the newer one automatically
                                suRunner.runSuCommandAsync(RestoreActivity.this, callback);
                            }
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
    
    @Override
    protected void onResume() {
        super.onResume();
        refreshBackups();
    }
    
    void refreshBackups() {
        BackupManager.getInstance(this).refresh();

        mAllAdapter.clear();
        for (BackupEntry entry: BackupManager.getInstance(this).backups.values()) {
            mAllAdapter.add(entry);
        }
        
        mGroupsAdapter.clear();
        for (BackupEntryGroup group: BackupManager.getInstance(this).groups.values()) {
            mGroupsAdapter.add(group);
        }
    }
}
