package com.tikle.mosms;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Process;

public class MyApplication extends Application
{
	private PendingIntent pendingIntent;
	public void onCreate ()
	{
		// Setup handler for uncaught exceptions.
		Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException (Thread thread, Throwable e)
			{
				handleUncaughtException (thread, e);
			}
		});
	}

	public void handleUncaughtException (Thread thread, Throwable e)
	{
		e.printStackTrace(); // not all Android versions will print the stack trace automatically

		Logger.extractLogToFile(getApplicationContext());
		Logger.logToFile(getApplicationContext(), e);

		SharedPreferences prefs = getApplicationContext().getSharedPreferences("mosms", MODE_PRIVATE);
		int op = prefs.getInt("operator", 0);		

		//		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		//		intent.putExtra("crashed", true);
		//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), op,
				new Intent(getApplicationContext(), AutoStart.class), PendingIntent.FLAG_UPDATE_CURRENT);

		android.app.AlarmManager mgr = (android.app.AlarmManager) getSystemService(Context.ALARM_SERVICE);
		mgr.set(android.app.AlarmManager.RTC, System.currentTimeMillis() + 2000, pendingIntent);

		Process.killProcess(Process.myPid());
		System.exit(2);
	}
}
