package com.koushikdutta.timemachine;

import java.io.DataOutputStream;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;

public class Helper {
    public final static String SCRIPT_NAME = "surunner.sh";

    public static Process runSuCommandAsync(Context context, String command) throws IOException
    {
        DataOutputStream fout = new DataOutputStream(context.openFileOutput(SCRIPT_NAME, 0));
        fout.writeBytes(command);
        fout.close();
        
        String[] args = new String[] { "su", "-c", ". " + context.getFilesDir().getAbsolutePath() + "/" + SCRIPT_NAME };
        Process proc = Runtime.getRuntime().exec(args);
        return proc;
    }

    public static int runSuCommand(Context context, String command) throws IOException, InterruptedException
    {
        return runSuCommandAsync(context, command).waitFor();
    }
    
    public static int runSuCommandNoScriptWrapper(Context context, String command) throws IOException, InterruptedException
    {
        String[] args = new String[] { "su", "-c", command };
        Process proc = Runtime.getRuntime().exec(args);
        return proc.waitFor();
    }
    
    static public void showAlertDialog(Context context, int stringResource)
    {
        try {
            AlertDialog.Builder builder = new Builder(context);
            builder.setMessage(stringResource);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.create().show();
        }
        catch(Exception ex) {
            
        }
    }
    
    static public void showAlertDialogWithTitle(Context context, int titleResource, int stringResource)
    {
        AlertDialog.Builder builder = new Builder(context);
        builder.setTitle(titleResource);
        builder.setMessage(stringResource);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }
}
