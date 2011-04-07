package com.koushikdutta.timemachine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ApplicationsActivity extends Activity {
    LayoutInflater mInflater;
    ArrayList<SingleApplicationInfo> mAppInfo = new ArrayList<ApplicationsActivity.SingleApplicationInfo>();
    
    abstract class BackupItem implements OnCheckedChangeListener {
        ArrayList<ApplicationInfo> infos = new ArrayList<ApplicationInfo>();
        public boolean disabled = false;
        public boolean backup = false;
        public Drawable drawable;
        public String name;
        
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            backup = isChecked;
            afterCheckedChanged();
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
    }
    
    class ApplicationBatch extends BackupItem {
    }
    
    class SingleApplicationInfo extends BackupItem
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
    }
    
    static class BackupEntryAdapter<T extends BackupItem> extends ArrayAdapter<T>
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
            CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.checked);
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(info.backup | info.disabled);
            checkBox.setOnCheckedChangeListener(info);
            checkBox.setEnabled(!info.disabled);
            ImageView image = (ImageView)convertView.findViewById(R.id.icon);
            TextView name = (TextView)convertView.findViewById(R.id.name);
            name.setText(info.name);
            image.setImageDrawable(info.drawable);
            
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.applications);
        
        PackageManager pm = getPackageManager();
        mInflater = getLayoutInflater();
        
        List<ApplicationInfo> packages = pm.getInstalledApplications(0);
        
        ListView lv = (ListView)findViewById(R.id.list);
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                CheckBox checkBox = (CheckBox)arg1.findViewById(R.id.checked);
                checkBox.setChecked(!checkBox.isChecked());
            }
        });
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                return true;
            }
        });
        
        mAllAdapter = new BackupEntryAdapter<SingleApplicationInfo>(this);

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
        addBatch(R.string.sms_and_mms, R.drawable.ic_launcher_smsmms, new String[] { "com.android.mms", "com.android.providers.telephony" });
        addBatch(R.string.games, R.drawable.ic_launcher_games, new String[] { "com.android.mms", "com.android.providers.telephony" });
        addBatch(R.string.social_networking, R.drawable.ic_launcher_twitter, new String[] { "com.twitter.android", "com.facebook.katana" });

        mAdapter = new SeparatedListAdapter(this);
        mAdapter.addSection(getString(R.string.application_groups), mGroupsAdapter);
        mAdapter.addSection(getString(R.string.all_applications), mAllAdapter);

        // TODO: add previous backups adapter
        
        lv.setAdapter(mAdapter);
        
        Button backup = (Button)findViewById(R.id.backup);
        backup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> packages = new ArrayList<String>();
                for (int i = 0; i < mAllAdapter.getCount(); i++) {
                    SingleApplicationInfo sinfo = mAllAdapter.getItem(i);
                    if (sinfo.disabled || sinfo.backup)
                        packages.add(sinfo.info.packageName);
                }
                
                if (packages.size() == 0) {
                    Helper.showAlertDialogWithTitle(ApplicationsActivity.this, R.string.backup, R.string.select_an_application_to_backup);
                    return;
                }
                
                Intent i = new Intent(ApplicationsActivity.this, BackupActivity.class);
                i.putStringArrayListExtra("packages", packages);
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
