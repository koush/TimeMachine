package com.koushikdutta.timemachine;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

public class RestoreActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.restore);
    }
    
    void refreshBackups() {
        try {
            File file = new File(Helper.BACKUP_DIR);
            String[] dirs = file.list();
            // find all the metadata.json and populate the ui
            for (String dirString: dirs) {
                File dir = new File(dirString);
                if (!dir.isDirectory())
                    continue;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
