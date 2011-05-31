package com.koushikdutta.timemachine;

import java.util.HashSet;

import android.app.Activity;
import android.content.Context;
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
    
    HashSet<String> mRestore = new HashSet<String>();

    static void bindCheckedState(View view, boolean checked) {
        view.setBackgroundColor(checked ? view.getResources().getColor(R.color.checked_application_background) : 0);
    }

    class ApplicationInfoAdapter extends ArrayAdapter<BackupEntry>
    {
        LayoutInflater mInflater;
        public ApplicationInfoAdapter(Context context) {
            super(context, 0);
            mInflater = LayoutInflater.from(context);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.appinfo, null);
            
            final BackupEntry info = getItem(position);
            ImageView image = (ImageView)convertView.findViewById(R.id.icon);
            TextView name = (TextView)convertView.findViewById(R.id.name);
            name.setText(info.name);
            image.setImageDrawable(info.drawable);

            View v = convertView.findViewById(R.id.age);
            v.setBackgroundColor(info.getColor());

            bindCheckedState(convertView, mRestore.contains(info.packageName));

            return convertView;
        }
    }
    
    ApplicationInfoAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.restore);
        
        mAdapter = new ApplicationInfoAdapter(this);
        ListView lv = (ListView)findViewById(R.id.list);
        lv.setAdapter(mAdapter);
        final TextView restoreCount = (TextView)findViewById(R.id.restore_count);
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                BackupEntry be = (BackupEntry)mAdapter.getItem(position);
                boolean restore;
                if (restore = !mRestore.remove(be.packageName)) {
                    mRestore.add(be.packageName);
                }
                bindCheckedState(view, restore);
                restoreCount.setText(getString(R.string.restore_count, mRestore.size()));
            }
        });

        refreshBackups();
        
        
        Button clear = (Button)findViewById(R.id.clear);
        clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mRestore.clear();
                mAdapter.notifyDataSetChanged();
            }
        });    

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
        BackupManager.getInstance(this).refreshBackups();

        mAdapter.clear();
        for (BackupEntry entry: BackupManager.getInstance(this).backups.values()) {
            mAdapter.add(entry);
        }
    }
}
