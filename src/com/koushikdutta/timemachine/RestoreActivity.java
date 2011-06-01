package com.koushikdutta.timemachine;

import java.util.HashSet;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
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
        
        public void onClick() {
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
        }
    }
    
    BackupEntryAdapter<BackupEntryGroup> mGroupsAdapter;
    BackupEntryAdapter<BackupEntry> mAllAdapter;
    SeparatedListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.restore);
        
        mAllAdapter = new BackupEntryAdapter<BackupEntry>(this);
        ListView lv = (ListView)findViewById(R.id.list);
        final TextView restoreCount = (TextView)findViewById(R.id.restore_count);
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
                BackupEntryAdapter<BackupEntryBase> badapter = (BackupEntryAdapter<BackupEntryBase>)mAdapter.getItemAdapter(position);
                badapter.onClick();
                restoreCount.setText(getString(R.string.restore_count, mChecked.size()));
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
                mChecked.clear();
                mAllAdapter.notifyDataSetChanged();
            }
        });    
        
        lv.setAdapter(mAdapter);

        Button restore = (Button)findViewById(R.id.restore);
        restore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
