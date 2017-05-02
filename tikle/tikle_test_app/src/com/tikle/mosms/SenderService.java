package com.tikle.mosms;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;

import com.tikle.mosms.models.Message;

public class SenderService extends Service {			
	public String SENT = "SMS_SENT";
    public String DELIVERED = "SMS_DELIVERED";
    
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Bundle bundle = intent.getExtras();
    	if(bundle == null) 
    		return START_NOT_STICKY;
    	
    	Message message = (Message) intent.getExtras().getSerializable("message");    	        	
        
    	Logger.i(getClass().getName(), "onStartCommand--->" + message.getShortNumber());
    	sendSMS(message);        	
		
        return START_STICKY;
    }
    	
	// Kisa numaraya mesaj atilir
	// Gonderildi ve iletildi bilgisi almak icin SENT ve DELIVERED
	// pendingintentleri olusturulur.	
    public void sendSMS(Message message) { 	
	    String phoneNumber = message.getShortNumber();
	    String text = message.getText();
	    
	    Logger.i(getClass().getName(), "sms sending--->" + message.getOperator().getType() + "---" + message.getShortNumber());	    	   	           

	    Intent sendIntent = new Intent(SENT);
	    sendIntent.putExtra("message", message);
	    PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 
	    		generateRequestCode(message.getOperator().getType(), message.getID()),
            sendIntent, 0);
 
        Intent deliverIntent = new Intent(DELIVERED);
        deliverIntent.putExtra("message", message);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(getApplicationContext(), 
        		generateRequestCode(message.getOperator().getType(), message.getID()),
    		deliverIntent, 0);
        
	    SmsManager smsManager = SmsManager.getDefault();
	    smsManager.sendTextMessage(phoneNumber, null, text, sentPI, deliveredPI);
	}
    
    // Mesaj gonderilecek her numara icin olusturulan pendingIntentlerin ayrilmasi
    // icin requestCode olusturma
    public int generateRequestCode(int op, int id) {
		return (int) (Math.pow(100, op) + id);		
	}
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }      
    
    @Override
    public void onDestroy() {    	
       super.onDestroy();
    }
}
