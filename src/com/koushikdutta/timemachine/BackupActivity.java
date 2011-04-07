package com.koushikdutta.timemachine;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
        
        PackageManager pm = getPackageManager();
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
        startBackup.requestFocus();
    }
}
