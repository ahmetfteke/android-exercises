package com.tikle.mosms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.tikle.mosms.models.Message;

public class DeliverSmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Message message = (Message) intent.getExtras().getSerializable("message");
    	Logger.i(getClass().getName(), "onDelivered--->" + message.getOperator().getType() + " " + message.getShortNumber());    	    	
    	
        switch (getResultCode())
        {
            case Activity.RESULT_OK:
            	Logger.writeDeliverLog(context, message, "RESULT_OK");         	
            	Logger.i("SMS Deliver", "SMS delivered-->" + message.getOperator().getType() + " " + message.getShortNumber());
//                Toast.makeText(getBaseContext(), "SMS delivered --->" + message.getOperator().getType() + " " + message.getShortNumber(), 
//                        Toast.LENGTH_SHORT).show();                    	
                break;
            case Activity.RESULT_CANCELED:
            	Logger.writeDeliverLog(context, message, "RESULT_CANCELED");
            	Logger.e("SMS Deliver", "SMS not delivered");
                Toast.makeText(context, "SMS not delivered " + message.getOperator().getType() + " " + message.getShortNumber(), 
                        Toast.LENGTH_SHORT).show();
                break;                        
        }
        
        // Mesaj gonderme aciksa sleepMinutes sure kadar sonraya yeni alarm kurulur
        SharedPreferences prefs = context.getSharedPreferences("mosms", Context.MODE_MULTI_PROCESS);
		Util.sendMessage = prefs.getBoolean("sendMessage", false);
		
        Logger.i(getClass().getName(), "senmessage--->" + Util.sendMessage);
        if(Util.sendMessage)
    		AlarmManager.getInstance(context).createAlarm(message);
	}

}
