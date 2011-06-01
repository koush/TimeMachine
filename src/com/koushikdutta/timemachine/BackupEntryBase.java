package com.koushikdutta.timemachine;

import android.graphics.drawable.Drawable;

public class BackupEntryBase {
    public Drawable drawable;
    public String name;

    public String getUniqueName() {
        return name;
    }

    public int getColor() {
        return 0;
    }
}
