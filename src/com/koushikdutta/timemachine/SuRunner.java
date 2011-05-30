package com.koushikdutta.timemachine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;

public class SuRunner {
    HashMap<String, String> mEnvironment = new HashMap<String, String>();
    StringBuilder mCommands = new StringBuilder();
    
    public void setEnvironment(String name, String value)
    {
        mEnvironment.put(name, value);
    }

    public void runSuCommandAsync(final Context context, final SuCommandCallback callback) {
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
                    callback.onStartBackground();
                    Process p = runSuCommandAsync(context);
                    if (p == null) {
                        return;
                    }
                    DataInputStream dis = new DataInputStream(p.getInputStream());
                    String line;
                    while (null != (line = dis.readLine())) {
                        if (callback != null) {
                            if (finalHandler != null) {
                                final String fline = line;
                                finalHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onOutputLine(fline);
                                    }
                                });
                            }
                            else {
                                callback.onOutputLine(line);
                            }
                        }
                    }
                    dis.close();
                    result = p.waitFor();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally
                {
                    if (callback == null)
                        return;
                    callback.onResultBackground(result);
                    if (finalHandler != null) {
                        finalHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(result);
                            }
                        });
                    }
                    else {
                        callback.onResult(result);
                    }
                }
            }  
        };
        thread.start();
    }
    
    public final static String SCRIPT_NAME = "surunner.sh";
    public Process runSuCommandAsync(Context context)
    {
        try {
            //String scriptName = String.valueOf(System.currentTimeMillis());
            String scriptName = SCRIPT_NAME;
            DataOutputStream fout = new DataOutputStream(context.openFileOutput(scriptName, 0));
            
            mEnvironment.put("BUSYBOX", context.getFilesDir().getAbsolutePath() + "/busybox");
            mEnvironment.put("FILES_DIR", context.getFilesDir().getAbsolutePath());
            mEnvironment.put("TIMEMACHINE_DIR", Helper.BASE_DIR);
            mEnvironment.put("ASSETS_DIR", Helper.ASSETS_DIR);
            for (String key: mEnvironment.keySet()) {
                String value = mEnvironment.get(key);
                if (value == null)
                    continue;
                fout.writeBytes(String.format("export %s='%s'\n", key, value));
            }
            fout.writeBytes(mCommands.toString());
            fout.close();
            
            String[] args = new String[] { "su", "-c", ". " + context.getFilesDir().getAbsolutePath() + "/" + scriptName };
            return Runtime.getRuntime().exec(args);
        }
        catch (Exception ex) {
            return null;
        }
    }

    public int runSuCommand(Context context)
    {
        try {
            return runSuCommandAsync(context).waitFor();
        }
        catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    public void addCommand(String command) {
        mCommands.append(command);
        mCommands.append('\n');
    }
}
