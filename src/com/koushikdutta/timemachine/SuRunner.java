package com.koushikdutta.timemachine;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.os.Handler;

public class SuRunner {
    HashMap<String, String> mEnvironment = new HashMap<String, String>();
    
    public void setEnvironment(String name, String value)
    {
        mEnvironment.put(name, value);
    }
    
    public interface SuCommandCallback
    {
        public void onCompleted(int result);
    }

    public void runSuCommandAsync(final Context context, final String command, final SuCommandCallback callback) {
        Handler handler = null;
        try {
            if (callback != null)
                handler = new Handler();
        }
        catch (Exception ex) {
        }
        
        final Handler finalHandler = handler;

        Thread thread = new Thread()
        {
            int result = -1;
            @Override
            public void run() {
                try {
                    Process p = runSuCommandAsync(context, command);
                    result = p.waitFor();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally
                {
                    if (callback == null)
                        return;
                    if (finalHandler != null) {
                        finalHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onCompleted(result);
                            }
                        });
                    }
                    else {
                        callback.onCompleted(result);
                    }
                }
            }  
        };
        thread.start();
    }
    
    public Process runSuCommandAsync(Context context, String command) throws IOException
    {
        String scriptName = String.valueOf(System.currentTimeMillis());
        DataOutputStream fout = new DataOutputStream(context.openFileOutput(scriptName, 0));
        
        mEnvironment.put("BUSYBOX", context.getFilesDir().getAbsolutePath()+ "/busybox");
        for (String key: mEnvironment.keySet()) {
            String value = mEnvironment.get(key);
            if (value == null)
                continue;
            fout.writeBytes(String.format("export %s='%s'\n", key, value));
        }
        fout.writeBytes(command);
        fout.close();
        
        String[] args = new String[] { "su", "-c", ". " + context.getFilesDir().getAbsolutePath() + "/" + scriptName };
        return Runtime.getRuntime().exec(args);
    }

    public int runSuCommand(Context context, String command) throws IOException, InterruptedException
    {
        return runSuCommandAsync(context, command).waitFor();
    }
}
