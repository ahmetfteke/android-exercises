package com.tikle.mosms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.tikle.mosms.models.Message;

public class SendSmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		Message message = (Message) arg1.getExtras().getSerializable("message");
		AlarmManager.getInstance(context).deleteAlarm(message);
    	Logger.i(getClass().getName(), "onSent--->" + message.getOperator().getType() + " " + message.getShortNumber());    	
    	
        switch (getResultCode())
        {
            case Activity.RESULT_OK:   
            	Logger.writeSendLog(context, message, "RESULT_OK");
            	Logger.i("SMS", "SMS sent--->" + message.getOperator().getType() + " " + message.getShortNumber());
                Toast.makeText(context, "SMS sent--->" + message.getOperator().getType() + " " + message.getShortNumber(), 
                        Toast.LENGTH_SHORT).show();                        
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
            	Logger.writeSendLog(context, message, "RESULT_ERROR_GENERIC_FAILURE");
            	Logger.e("SMS", "Generic failure");
                Toast.makeText(context, "Generic failure", 
                        Toast.LENGTH_SHORT).show();
                AlarmManager.getInstance(context).createAlarm(message);
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
            	Logger.writeSendLog(context, message, "RESULT_ERROR_NO_SERVICE");
            	Logger.i("SMS", "No service");
                Toast.makeText(context, "No service", 
                        Toast.LENGTH_SHORT).show();
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
            	Logger.writeSendLog(context, message, "RESULT_ERROR_NULL_PDU");
            	Logger.i("SMS", "Null PDU");
                Toast.makeText(context, "Null PDU", 
                        Toast.LENGTH_SHORT).show();
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
            	Logger.writeSendLog(context, message, "RESULT_ERROR_RADIO_OFF");
            	Logger.i("SMS", "Radio off");
                Toast.makeText(context, "Radio off", 
                        Toast.LENGTH_SHORT).show();
                break;
        }  
	}	
}
