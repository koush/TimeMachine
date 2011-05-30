package com.koushikdutta.timemachine;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class RestoreActivity extends Activity {
    
    // groups will be maintained in a database.
    // a group is user defined, but starts with defaults.
    // a valid restore group is when all the applications in the group have the same
    // backup timestamp.
    
    static class ApplicationInfoAdapter extends ArrayAdapter<BackupEntry>
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

        refreshBackups();
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
