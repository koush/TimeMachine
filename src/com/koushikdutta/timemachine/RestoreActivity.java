package com.koushikdutta.timemachine;

import java.io.File;
import java.io.FileInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class RestoreActivity extends Activity {
    
    private static class SingleApplicationInfo
    {
        public JSONObject info;
        public Drawable drawable;
        public String name;
        public String packageName;
        private SingleApplicationInfo() {
        }
        public static SingleApplicationInfo from(JSONObject info, Drawable drawable) throws JSONException {
            String name = info.getString("name");
            String packageName = info.getString("packageName");
            SingleApplicationInfo ret = new SingleApplicationInfo();
            ret.info = info;
            ret.drawable = drawable;
            ret.name = name;
            ret.packageName = packageName;
            return ret;
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
                convertView = mInflater.inflate(R.layout.appinfo, null);
            
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
        try {
            mAdapter.clear();
            File backupDir = new File(Helper.BACKUP_DIR);
            String[] dirs = backupDir.list();
            // find all the metadata.json and populate the ui
            for (String dirString: dirs) {
                try {
                    File dir = new File(backupDir.getAbsolutePath() + "/" + dirString);
                    if (!dir.isDirectory())
                        continue;
                    File metadata = new File(dir.getAbsolutePath() + "/metadata.json");
                    File icon = new File(dir.getAbsolutePath() + "/icon.png");
                    JSONObject info = new JSONObject(StreamUtility.readFile(metadata));
                    FileInputStream fin = new FileInputStream(icon);
                    Bitmap bmp = BitmapFactory.decodeStream(fin);
                    BitmapDrawable drawable = new BitmapDrawable(bmp);
                    fin.close();
                    SingleApplicationInfo sinfo = SingleApplicationInfo.from(info, drawable);
                    mAdapter.add(sinfo);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
