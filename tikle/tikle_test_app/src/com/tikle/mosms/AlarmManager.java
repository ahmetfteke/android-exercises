package com.tikle.mosms;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.tikle.mosms.db.MessageDataSource;
import com.tikle.mosms.models.Message;

public class AlarmManager {	
	private static AlarmManager manager;

	private android.app.AlarmManager alarmManager;

	private Context context;

	public AlarmManager(Context context) {
		this.context = context;
		alarmManager = (android.app.AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
	}

	public static AlarmManager getInstance(Context context) {
		if(manager == null)
			manager = new AlarmManager(context);

		return manager;
	}

	// changed parametresi yeni operator secilmisse true 
	public void scheduleAlarms(ArrayList<Message> messagesFromUrl,
			boolean changed) {
		Logger.i(getClass().getName(), "scheduleAlarms, changed----" + changed);
		if (changed) {
			SharedPreferences prefs = context.getSharedPreferences("mosms",
					Context.MODE_MULTI_PROCESS);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("changed", false);
			editor.commit();

			MessageDataSource msgDs = new MessageDataSource(context);
			msgDs.open();
			ArrayList<Message> messages = msgDs.getAllMessages();
			msgDs.close();

			deleteAlarms(messages);
			createAlarms(messagesFromUrl);		
		} else {
			createAlarms(messagesFromUrl);
		}

		updateMessagesDb(messagesFromUrl);
	}

	public void scheduleLastAlarms() {
		MessageDataSource msgDs = new MessageDataSource(context);
		msgDs.open();
		ArrayList<Message> messages = msgDs.getAllMessages();
		msgDs.close();

		deleteAlarms(messages);
		for(Message message : messages) {
			createImmediateAlarm(message);
		}
	}	

	public void cancelSending() {
		MessageDataSource msgDs = new MessageDataSource(context);
		msgDs.open();
		ArrayList<Message> messages = msgDs.getAllMessages();
		msgDs.close();

		deleteAlarms(messages);
	}

	public void deleteAlarm(Message message) {
		PendingIntent pi = getPendingIntent(context, message);
		android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Logger.i(getClass().getName(), "deleting--->" + message.getOperator().getType() + " " + message.getShortNumber());
		alarmManager.cancel(pi);
		pi.cancel();
	}

	private void deleteAlarms(ArrayList<Message> messages) {
		for(Message message : messages) {
			deleteAlarm(message);
		}
	}
		
	// CheckUrl sonrasý veya mesaj gönderimi acma sonrasý hemen mesaj
	// gönderebilmek ilk alarm suresi 1 dakika olarak ayarlandý.
	private void createImmediateAlarm(Message message) {
		Logger.i(getClass().getName(), "creating immediate alarm--->" + message.getOperator().getType() + " " + message.getShortNumber());

		Calendar c = Calendar.getInstance(new Locale("TR"));
		c.add(Calendar.MINUTE, 1);
		long intervalMillis = c.getTimeInMillis();
		alarmManager.set(android.app.AlarmManager.RTC_WAKEUP,
				intervalMillis, getPendingIntent(context, message));
	}

	// Ilk mesajlar gonderildikten sonra diger mesaj gonderimleri
	// urlden gelen sleepMinutes a gore ayarlandý
	public void createAlarm(Message message) {
		Logger.i(getClass().getName(), "creating alarm--->" + message.getOperator().getType() + " " + message.getShortNumber());

		Calendar c = Calendar.getInstance(new Locale("TR"));
		c.add(Calendar.MINUTE, message.getSleepMinutes());
		long intervalMillis = c.getTimeInMillis();
		alarmManager.set(android.app.AlarmManager.RTC_WAKEUP,
				intervalMillis, getPendingIntent(context, message));
	}

	private void createAlarms(ArrayList<Message> messages) {
		for (Message message : messages) {
			createImmediateAlarm(message);
		}
	}

	private void updateMessagesDb(ArrayList<Message> messages) {
		MessageDataSource msgDs = new MessageDataSource(context);
		msgDs.open();
		msgDs.deleteAllMessages();
		msgDs.close();		
		for (Message message : messages) {
			msgDs.open();
			msgDs.createMessage(message);
			msgDs.close();
		}		
	}

	// Mesaj gonderilecek her numara icin olusturulan pendingIntentlerin ayrilmasi
    // icin requestCode olusturma
	public int generateRequestCode(int op, int id) {
		return (int) (Math.pow(100, op) + id);		
	}

	// Alarm kurma ve iptal etme icin gerekli pendingintentlerin olusturulmasi
	public PendingIntent getPendingIntent(Context context, Message message) {
		Intent intent =  new Intent(context, AlarmReceiver.class)
		.putExtra("message", message);
		return PendingIntent.getBroadcast(context, generateRequestCode(message.getOperator().getType(), message.getID()), 
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
