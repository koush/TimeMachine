package com.koushikdutta.timemachine;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class TimeMachineActivity extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timemachine);
        
        TabHost tabHost = getTabHost();
        
        Intent intent = new Intent(this, ApplicationsActivity.class);
        TabSpec backup = tabHost.newTabSpec("backup").setIndicator(getString(R.string.applications), getResources().getDrawable(R.drawable.ic_tab_applications)).setContent(intent);
        tabHost.addTab(backup);
        
        intent = new Intent(this, RestoreActivity.class);
        TabSpec restore = tabHost.newTabSpec("restore").setIndicator(getString(R.string.restore), getResources().getDrawable(R.drawable.ic_tab_clock)).setContent(intent);
        tabHost.addTab(restore);
    }
}