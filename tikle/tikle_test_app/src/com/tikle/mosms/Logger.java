package com.tikle.mosms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Locale;

import com.tikle.mosms.models.Message;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

public class Logger {
	// TODO change false before release
	public static final boolean DEBUG = true;

	private static final String logFileName = "log.txt";
	private static final String globalLogFileName = "uncaughtLog.txt";
	private static final String sendLogFileName = "sendLog.txt";
	private static final String deliverLogFileName = "deliverLog.txt";

	public static void i(String tag, String text) {
		if(DEBUG)
			Log.i(tag, text);
	}

	public static void e(String tag, String text) {
		if(DEBUG)
			Log.i(tag, text);
	}

	public static void logToFile(Context context, Throwable ex) {
		File root = getRootFile(context);
		File dir = new File(root.getAbsolutePath() + File.separator + "MoSmsSender");
		dir.mkdirs();
		File logFile = new File(dir, logFileName);

		if (!logFile.exists())
		{
			try
			{
				logFile.createNewFile();
			} 
			catch (IOException e)
			{
				Logger.e("Util.logToFile", e.getMessage());
			}
		}
		try
		{
			//BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 			
			buf.append(Calendar.getInstance(new Locale("TR")).getTime().toString());
			buf.newLine();

			if(ex.getCause() != null) {
				buf.append(ex.getCause().toString());
				buf.newLine();
			}

			if(ex.getMessage() != null) {
				buf.append(ex.getMessage());
				buf.newLine();
			}

			buf.append("----------------------------------------------");
			buf.newLine();
			buf.close();
		}
		catch (IOException e)
		{
			Logger.e("Util.logToFile", e.getMessage());
		} 		
	}

	public static void extractLogToFile(Context context)
	{
		PackageManager manager = context.getPackageManager();
		PackageInfo info = null;
		try {
			info = manager.getPackageInfo (context.getPackageName(), 0);
		} catch (NameNotFoundException e2) {
		}
		String model = Build.MODEL;
		if (!model.startsWith(Build.MANUFACTURER))
			model = Build.MANUFACTURER + " " + model;

		// Make file name - file must be saved to external storage or it wont be readable by
		// the email app.

		// Extract to file.
		File root = getRootFile(context);
		File dir = new File(root.getAbsolutePath() + File.separator + "MoSmsSender");
		dir.mkdirs();
		File file = new File(dir, globalLogFileName);		

		//String fullName = file.getAbsolutePath();

		InputStreamReader reader = null;
		FileWriter writer = null;
		try
		{
			// For Android 4.0 and earlier, you will get all app's log output, so filter it to
			// mostly limit it to your app's output.  In later versions, the filtering isn't needed.
			String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
					"logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" :
						"logcat -d -v time";

			// get input stream
			Process process = Runtime.getRuntime().exec(cmd);
			reader = new InputStreamReader (process.getInputStream());

			// write output stream
			writer = new FileWriter (file);			
			writer.write ("Android version: " +  Build.VERSION.SDK_INT + "\n");
			writer.write ("Device: " + model + "\n");
			writer.write ("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");

			char[] buffer = new char[10000];
			do 
			{
				int n = reader.read (buffer, 0, buffer.length);
				if (n == -1)
					break;
				writer.write (buffer, 0, n);
			} while (true);

			reader.close();
			writer.close();
		}
		catch (IOException e)
		{
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e1) {
				}
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e1) {
				}
		}
	}

	private static File getRootFile(Context context) {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File root = Environment.getExternalStorageDirectory();
			File dir = new File(root.getAbsolutePath() + File.separator + "MoSmsSender");		
			dir.mkdirs();
			return dir;
		} else {
			return context.getFilesDir();
		}
	}

	public static void writeSendLog(Context context, Message message, String result) {
//		File root = getRootFile();
//		File dir = new File(root.getAbsolutePath() + File.separator + "MoSmsSender");		
//		dir.mkdirs();
		File logFile = new File(getRootFile(context), sendLogFileName);

		if (!logFile.exists())
		{
			try
			{
				logFile.createNewFile();
			} 
			catch (IOException e)
			{
				Logger.e("Util.writeSendLog", e.getMessage());
			}
		}
		try
		{
			//BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 			
			buf.append(Calendar.getInstance(new Locale("TR")).getTime().toString());
			buf.newLine();

			buf.append(message.getOperator().name());
			buf.newLine();
			buf.append(message.getShortNumber());
			buf.newLine();
			buf.append(result);
			buf.newLine();

			buf.append("----------------------------------------------");
			buf.newLine();
			buf.close();
		}
		catch (IOException e)
		{
			Logger.e("Util.writeSendLog", e.getMessage());
		}
	}
	
	public static void writeDeliverLog(Context context, Message message, String result) {
		File root = getRootFile(context);
		File dir = new File(root.getAbsolutePath() + File.separator + "MoSmsSender");
		dir.mkdirs();
		File logFile = new File(dir, deliverLogFileName);

		if (!logFile.exists())
		{
			try
			{
				logFile.createNewFile();
			} 
			catch (IOException e)
			{
				Logger.e("Util.writeDeliverLog", e.getMessage());
			}
		}
		try
		{
			//BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 			
			buf.append(Calendar.getInstance(new Locale("TR")).getTime().toString());
			buf.newLine();

			buf.append(message.getOperator().name());
			buf.newLine();
			buf.append(message.getShortNumber());
			buf.newLine();
			buf.append(result);
			buf.newLine();

			buf.append("----------------------------------------------");
			buf.newLine();
			buf.close();
		}
		catch (IOException e)
		{
			Logger.e("Util.writeDeliverLog", e.getMessage());
		}
	}
}
