package com.koushikdutta.timemachine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ApplicationsActivity extends Activity {
    LayoutInflater mInflater;
    TextView mCountView;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem date = menu.add(R.string.sort_by_date);
        date.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mAllAdapter.sort(new Comparator<SingleApplicationInfo>() {
                    @Override
                    public int compare(SingleApplicationInfo object1, SingleApplicationInfo object2) {
                        long d1 = object1.getBackupDate(ApplicationsActivity.this);
                        long d2 = object2.getBackupDate(ApplicationsActivity.this);
                        if (d1 > d2)
                            return 1;
                        if (d1 < d2)
                            return -1;
                        String n1 = object1.name;
                        String n2 = object2.name;
                        return n1.compareToIgnoreCase(n2);
                    }
                });
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });

        MenuItem pkg = menu.add(R.string.sort_by_package);
        pkg.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mAllAdapter.sort(new Comparator<SingleApplicationInfo>() {
                    @Override
                    public int compare(SingleApplicationInfo object1, SingleApplicationInfo object2) {
                        String n1 = object1.info.packageName;
                        String n2 = object2.info.packageName;
                        return n1.compareToIgnoreCase(n2);
                    }
                });
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });

        MenuItem name = menu.add(R.string.sort_by_name);
        name.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mAllAdapter.sort(new Comparator<SingleApplicationInfo>() {
                    @Override
                    public int compare(SingleApplicationInfo object1, SingleApplicationInfo object2) {
                        String n1 = object1.name;
                        String n2 = object2.name;
                        return n1.compareToIgnoreCase(n2);
                    }
                });
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });
        
        return super.onCreateOptionsMenu(menu);
    }
    
    void refreshCount() {
        int count = 0;
        for (SingleApplicationInfo sinfo: mAllPackages.values()) {
            if (sinfo.disabled || sinfo.backup) 
                count++;
        }
        mCountView.setText(getString(R.string.backup_count, count));
    }
    
    private abstract class BackupItem {
        ArrayList<ApplicationInfo> infos = new ArrayList<ApplicationInfo>();
        public boolean disabled = false;
        public boolean backup = false;
        public Drawable drawable;
        public String name;
        
        public void onCheckedChanged() {
            afterCheckedChanged();
            refreshCount();
        }
        
        protected void afterCheckedChanged() {
            for (SingleApplicationInfo sinfo: mAllPackages.values()) {
                sinfo.disabled = false;
            }
            
            for (int i = 0; i < mGroupsAdapter.getCount(); i++) {
                ApplicationBatch batch = mGroupsAdapter.getItem(i);
                if (!batch.backup)
                    continue;
                for (ApplicationInfo info: batch.infos) {
                    SingleApplicationInfo sinfo = mAllPackages.get(info.packageName);
                    sinfo.disabled = true;
                }
            }
            
            mAdapter.notifyDataSetChanged();
        }
        
        public long getBackupDate(Context context) {
            return 0;
        }
        
        public int getColor(Context context) {
            return 0;
        }
    }
    
    private class ApplicationBatch extends BackupItem {
    }
    
    private class SingleApplicationInfo extends BackupItem
    {
        ApplicationInfo info;
        public SingleApplicationInfo(ApplicationInfo info, PackageManager pm) {
            this.info = info;
            name = info.loadLabel(pm).toString();
            drawable = info.loadIcon(pm);
            infos.add(info);
        }

        @Override
        protected void afterCheckedChanged() {
        }
        
        @Override
        public long getBackupDate(Context context) {
            BackupManager mgr = BackupManager.getInstance(context);
            BackupEntry entry = mgr.backups.get(info.packageName);
            if (entry == null)
                return Long.MAX_VALUE;
            return entry.newest;
        }
        
        @Override
        public int getColor(Context context) {
            return BackupManager.getColor(context, info.packageName);
        }
    }

    static void bindCheckedState(View view, boolean checked) {
        view.setBackgroundColor(checked ? view.getResources().getColor(R.color.checked_application_background) : 0);
    }
    
    class BackupEntryAdapter<T extends BackupItem> extends ArrayAdapter<T>
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
            
            final BackupItem info = getItem(position);
            ImageView image = (ImageView)convertView.findViewById(R.id.icon);
            TextView name = (TextView)convertView.findViewById(R.id.name);
            name.setText(info.name);
            image.setImageDrawable(info.drawable);
            
            View v = convertView.findViewById(R.id.age);
            v.setBackgroundColor(info.getColor(ApplicationsActivity.this));

            bindCheckedState(convertView, info.backup || info.disabled);
            
            return convertView;
        }
    }
    
    SeparatedListAdapter mAdapter;
    BackupEntryAdapter<ApplicationBatch> mGroupsAdapter;
    BackupEntryAdapter<SingleApplicationInfo> mAllAdapter;
    
    HashMap<String, SingleApplicationInfo> mAllPackages = new HashMap<String, SingleApplicationInfo>();

    void addBatch(int resourceName, int drawable, String[] packages) {
        ApplicationBatch batch = new ApplicationBatch();
        batch.name = getString(resourceName);
        batch.drawable = getResources().getDrawable(drawable);
        for (String messagingPackage: packages) {
            SingleApplicationInfo sinfo = mAllPackages.get(messagingPackage);
            if (sinfo != null)
                batch.infos.add(mAllPackages.get(messagingPackage).info);
        }
        if (batch.infos.size() > 0)
            mGroupsAdapter.add(batch);
    }
    
    void addBatch(BackupEntryGroup bg) {
        ApplicationBatch batch = new ApplicationBatch();
        batch.name = bg.name;
        batch.drawable = bg.drawable;
        for (String messagingPackage: bg.packages) {
            SingleApplicationInfo sinfo = mAllPackages.get(messagingPackage);
            if (sinfo != null)
                batch.infos.add(mAllPackages.get(messagingPackage).info);
        }
        if (batch.infos.size() > 0)
            mGroupsAdapter.add(batch);
    }

    int[] mOld;
    int[] mNew;
    
    static int[] from(int color) {
        return new int[] { Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color) };
    }
    
    int scale(float f) {
        int[] ret = new int[4];
        
        for (int i = 0; i < 4; i++) {
            ret[i] = mOld[i] + (int)((mNew[i] - mOld[i]) * f);
        }
        
        return Color.argb(ret[0], ret[1], ret[2], ret[3]);
    }
    
    void refresh() {
        mGroupsAdapter.clear();
        for (BackupEntryGroup bg: BackupManager.getInstance(this).groups.values()) {
            addBatch(bg);
        }
        mAdapter.notifyDataSetChanged();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mOld = from(getResources().getColor(R.color.backup_old));
        mNew = from(getResources().getColor(R.color.backup_new));

        setContentView(R.layout.applications);

        mCountView = (TextView)findViewById(R.id.backup_count); 
        
        PackageManager pm = getPackageManager();
        mInflater = getLayoutInflater();
        
        ListView lv = (ListView)findViewById(R.id.list);
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                BackupItem bi = (BackupItem)mAdapter.getItem(position);
                if (bi.disabled)
                    return;
                bi.backup = !bi.backup;
                bindCheckedState(view, bi.backup | bi.disabled);
                bi.onCheckedChanged();
            }
        });
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                BackupItem bi = (BackupItem)mAdapter.getItem(position);
                if (!(bi instanceof ApplicationBatch))
                    return false;
                final ApplicationBatch batch = (ApplicationBatch)bi;
                Helper.showAlertDialogWithTitle(ApplicationsActivity.this, R.string.remove_group, R.string.remove_group_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BackupManager bm = BackupManager.getInstance(ApplicationsActivity.this);
                        bm.groups.remove(batch.name);
                        bm.saveGroups();
                        refresh();
                    }
                });
                return true;
            }
        });
        
        mAllAdapter = new BackupEntryAdapter<SingleApplicationInfo>(this);

        List<ApplicationInfo> packages = pm.getInstalledApplications(0);
        for (ApplicationInfo info: packages) {
            SingleApplicationInfo ci = new SingleApplicationInfo(info, pm);
            mAllAdapter.add(ci);
            mAllPackages.put(info.packageName, ci);
        }
        mAllAdapter.sort(new Comparator<BackupItem>() {
            @Override
            public int compare(BackupItem object1, BackupItem object2) {
                return object1.name.compareToIgnoreCase(object2.name);
            }
        });
        
        mGroupsAdapter = new BackupEntryAdapter<ApplicationBatch>(this);
        
        mAdapter = new SeparatedListAdapter(this);
        mAdapter.addSection(getString(R.string.application_groups), mGroupsAdapter);
        mAdapter.addSection(getString(R.string.all_applications), mAllAdapter);

        refresh();

        // TODO: add previous backups adapter
        
        lv.setAdapter(mAdapter);
        
        Button backup = (Button)findViewById(R.id.backup);
        backup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HashSet<String> packagesHash = new HashSet<String>();
                for (int i = 0; i < mAllAdapter.getCount(); i++) {
                    SingleApplicationInfo sinfo = mAllAdapter.getItem(i);
                    if (sinfo.disabled || sinfo.backup)
                        packagesHash.add(sinfo.info.packageName);
                }
                
                int groupCount = 0;
                String groupName = null;
                for (int i = 0; i < mGroupsAdapter.getCount(); i++) {
                    ApplicationBatch binfo = mGroupsAdapter.getItem(i);
                    if (!binfo.backup)
                        continue;
                    groupCount++;
                    groupName = binfo.name;
                    for (ApplicationInfo info: binfo.infos) {
                        packagesHash.add(info.packageName);
                    }
                }
                
                if (packagesHash.size() == 0) {
                    Helper.showAlertDialogWithTitle(ApplicationsActivity.this, R.string.backup, R.string.select_an_application_to_backup);
                    return;
                }
                
                Intent i = new Intent(ApplicationsActivity.this, BackupActivity.class);
                if (groupCount == 1) {
                    i.putExtra("groupName", groupName);
                }
                i.putStringArrayListExtra("packages", new ArrayList<String>(packagesHash));
                startActivity(i);
            }
        });
        
        Button clear = (Button)findViewById(R.id.clear);
        clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clear(mGroupsAdapter);
                clear(mAllAdapter);
                mAdapter.notifyDataSetChanged();
            }
        });
    }
    
    <T extends BackupItem> void clear(BackupEntryAdapter<T> adapter) {
        for (int i = 0; i < adapter.getCount(); i++) {
            T item = adapter.getItem(i);
            item.disabled = false;
            item.backup = false;
        }
    }
    
}
