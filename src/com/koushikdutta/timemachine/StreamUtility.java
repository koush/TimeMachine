package com.koushikdutta.timemachine;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;

public class StreamUtility {
	private static final String LOGTAG = "ROMManager";
	public static int copyStream(InputStream input, OutputStream output) throws IOException
	{
		byte[] stuff = new byte[1024];
		int read = 0;
		int total = 0;
		while ((read = input.read(stuff)) != -1)
		{
			output.write(stuff, 0, read);
			total += read;
		}
		return total;
	}
	
	public static String readToEnd(InputStream input) throws IOException
	{
		DataInputStream dis = new DataInputStream(input);
		byte[] stuff = new byte[1024];
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		int read = 0;
		int total = 0;
		while ((read = dis.read(stuff)) != -1)
		{
			buff.write(stuff, 0, read);
			total += read;
		}
		
		return new String(buff.toByteArray());
	}
	
    public static void writeStringToFile(File file, String string) throws IOException {
        writeStringToFile(file.getAbsolutePath(), string);
    }
    
    public static void writeStringToFile(String file, String string) throws IOException {
        DataOutputStream dout = new DataOutputStream(new FileOutputStream(file));
        dout.write(string.getBytes());
        dout.close();
    }
}
