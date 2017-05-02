package com.tikle.mosms;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class AutoStart extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {  
		Logger.i(getClass().getName(), "AutoStart action--->" + intent.getAction());
		startSenderService(context, intent);
	}

	private void startSenderService(Context context, Intent intent) {
		intent.setClass(context, CheckUrlService.class);    	
		context.startService(intent);    	

		ComponentName receiver = new ComponentName(context, AutoStart.class);
		PackageManager pm = context.getPackageManager();

		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);
	}
}
