package com.tikle.mosms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.tikle.mosms.db.MessageDataSource;

public class SmsFilter extends BroadcastReceiver {
	
	// Mesaj gonderilen numaralardan gelen cevaplarin cihazin sms kutusuna
	// dusmesini engeller
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				Object[] pdus = (Object[])extras.get("pdus");

				if (pdus.length < 1) return; // Invalid SMS. Not sure that it's possible.

				StringBuilder sb = new StringBuilder();
				String sender = null;
				for (int i = 0; i < pdus.length; i++) {
					SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[i]);
					if (sender == null) 
						sender = message.getOriginatingAddress();
					
					String text = message.getMessageBody();
					if (text != null) 
						sb.append(text);
				}
				
				MessageDataSource msgDs = new MessageDataSource(context);
				msgDs.open();
				
				Logger.i(getClass().getName(), "Gönderen--->" + sender);
				if (sender != null && msgDs.existsShortNumber(sender)) {
					// Process our sms...
					abortBroadcast();
				}
				msgDs.close();
				return;
			}
		}

		// ...
	}
}
