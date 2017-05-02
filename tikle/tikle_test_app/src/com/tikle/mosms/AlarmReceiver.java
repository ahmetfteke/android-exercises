package com.tikle.mosms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    
	@Override
	public void onReceive(final Context ctx, final Intent intent) {
		intent.setClass(ctx, SenderService.class);
		ctx.startService(intent);
	}
}
