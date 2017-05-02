package com.tikle.mosms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.tikle.mosms.models.Message;
import com.tikle.mosms.models.Operator;

public class CheckUrlService extends Service {
	private boolean noNetwork = false;	
	private BroadcastReceiver batReceiver;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {				
		Util.isRunningCheckUrl=true;
		int op = 0;
		
		batReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
		    	int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
				boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
						status == BatteryManager.BATTERY_STATUS_FULL;

				int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				Logger.i(getClass().getName(), "battery level-----" + level);
				
				SharedPreferences prefs = context.getSharedPreferences("mosms", Context.MODE_PRIVATE);
				if(isCharging) {
					if(level > 15) {
						SharedPreferences.Editor editor = prefs.edit();
						editor.putBoolean("sentLowBatteryAlert", false);
						editor.commit();
					}
				} else {					
					boolean sentLowBatteryAlert = prefs.getBoolean("sentLowBatteryAlert", false);
					
					if(level < 15 && !sentLowBatteryAlert) {
						SharedPreferences.Editor editor = prefs.edit();
						editor.putBoolean("sentLowBatteryAlert", true);
						editor.commit();
						sendLowBatterySms(context, level);
					}
				}
			}
		};
		
		registerReceiver(batReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		// Internet olmamasi durumunda bilgi smsi gonderilecek numaralarin guncellenmesi
		new GetMsisdnInfos(getApplicationContext()).start();
		
		SharedPreferences prefs = getSharedPreferences("mosms", MODE_MULTI_PROCESS);
		op = prefs.getInt("operator", 0);
		Logger.i(getClass().getName(), "operator--->" + op);
		
		// Eger operator 0 ise bir sorun var demektir
		if(op!=0){
			boolean changed = prefs.getBoolean("changed", false);
						
			Operator operator = Operator.fromType(op);
			new GetOperatorInfo(getApplicationContext(), operator, changed).start();
		} else {
			Intent i = new Intent(CheckUrlService.this, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
			stopSelf();
		}

		return START_REDELIVER_INTENT;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Util.isRunningCheckUrl=false;
		unregisterReceiver(batReceiver);
	}
	
	private void sendLowBatterySms(Context context, float level) {
		SharedPreferences prefs = context.getSharedPreferences("mosms", Context.MODE_PRIVATE);
		int op = prefs.getInt("operator", 0);

		ArrayList<Message> messages = new ArrayList<>();

		try {								
			Calendar c = Calendar.getInstance(new Locale("TR"));				
			String text = c.getTime().toString();

			String result = Util.readStringFromFile(context, "msisdn");
			Logger.i(getClass().getName(), result);

			JSONArray jar = new JSONArray(result);
			for(int i =0; i < jar.length(); i++) {
				JSONObject json = jar.getJSONObject(i);

				Message message = new Message();
				message.setShortNumber(json.getString("msisdn"));
				message.setDescription(json.getString("Description"));					
				message.setOperator(Operator.fromType(op));
				message.setID(i - jar.length());
				message.setSleepMinutes(30);
				message.setText(text + " saati itibarý ile " 
						+ Operator.fromType(op).name() + " cihazýnýn þarjý yüzde " + level);

				messages.add(message);
			}
		} catch (JSONException e) {
			Logger.e(getClass().getName(), e.getMessage());
		}

		for(Message message : messages) {
			sendSMS(message);
		}
	}
	
	public void sendSMS(final Message message) { 	
	    String phoneNumber = message.getShortNumber();
	    String text = message.getText();
	    
	    Logger.i(getClass().getName(), "sms sending--->" + message.getOperator().getType() + "---" + message.getShortNumber());    

	    SmsManager smsManager = SmsManager.getDefault();	    
	    smsManager.sendTextMessage(phoneNumber, null, text, null, null);
	}

	private class GetOperatorInfo extends Thread {
		private Operator operator;
		private Context context;
		private boolean changed;

		// changed parametresi yeni operator secilmisse true 
		public GetOperatorInfo(Context context, Operator operator, boolean changed) {		
			this.context = context;
			this.operator = operator;
			this.changed = changed;
		}

		@Override
		public void run() {
			super.run();

			String uri = String
					.format(Util.operatorRootUrl, operator.getType());

			AlarmManager am = AlarmManager.getInstance(context);
			am.scheduleAlarms(getMessagesFromUrl(uri), changed);
			//AlarmManager am = new AlarmManager(context);
//			if(changed)
//				am.scheduleAlarms(getMessagesFromUrl(uri), changed);
//			else am.scheduleLastAlarms();
		}

		// Urlden secili operatore ait numaralar cekilir ve dosyaya kaydedilir
		// Eger bir sorundan dolayi  urlden bilgi alinamazsa dosyadan son hali okunur
		// ve ilgili kisilere bilgi smsi atilir
		private ArrayList<Message> getMessagesFromUrl(String uri) {
			ArrayList<Message> messages = null;

			String jsonString;
			try {
				jsonString = Util.getStringFromUrl(uri);
				Logger.i(getClass().getName(), "noNetwork--->" + noNetwork);
				if(noNetwork) {
					noNetwork = false;
					sendNoNetworkSms();
				}

				Util.writeStringToFile(context, operator.name(), jsonString);
				messages = convertToMessage(jsonString);
			} catch (IOException e) {
				Logger.e(getClass().getName(), "ulaþamadýk");
				Logger.logToFile(context, e);

				noNetwork = true;
				Logger.i(getClass().getName(), "noNetwork--->" + noNetwork);
				jsonString = Util.readStringFromFile(context, operator.name());				
				Logger.i(getClass().getName(), jsonString);
				if(jsonString.equals("")) {
					showFirstTimeToast();
				}
				messages = convertToMessage(jsonString);
				sendNoNetworkSms();
			}					

			return messages;
		}

		private ArrayList<Message> convertToMessage(String jsonString) {
			ArrayList<Message> messages = new ArrayList<Message>();

			try {
				JSONArray jar = new JSONArray(jsonString);

				for (int i = 0; i < jar.length(); i++) {
					JSONObject json = jar.getJSONObject(i);

					Message message = new Message();
					message.setID(json.getInt("ID"));
					message.setShortNumber(json.getString("ShortNumber"));
					message.setSleepMinutes(json.getInt("SleepMinutes"));
					message.setText(json.getString("Text"));
					message.setDescription(json.getString("Description"));
					message.setOperator(Operator.fromType(json
							.getInt("Operator")));

					messages.add(message);
				}
			} catch (JSONException e) {
				Logger.e(getClass().getName(), e.getMessage());
			}

			return messages;
		}

		private void sendNoNetworkSms() {
			ArrayList<Message> messages = new ArrayList<>();

			try {								
				Calendar c = Calendar.getInstance(new Locale("TR"));				
				String text = c.getTime().toString();

				String result = Util.readStringFromFile(context, "msisdn");
				Logger.i(getClass().getName(), result);
				
				JSONArray jar = new JSONArray(result);
				for(int i =0; i < jar.length(); i++) {
					JSONObject json = jar.getJSONObject(i);

					Message message = new Message();
					message.setShortNumber(json.getString("msisdn"));
					message.setDescription(json.getString("Description"));					
					message.setOperator(operator);
					message.setID(i - jar.length());
					message.setSleepMinutes(30);
					if(noNetwork)
						message.setText(text + " tarihinde kýsa numaralara eriþilemedi.");
					else message.setText("Kýsa numaralara eriþilebiliyor artýk.");

					messages.add(message);
				}
			} catch (JSONException e) {
				Logger.e(getClass().getName(), e.getMessage());
			}

			for(Message message : messages) {
				sendSMS(message);
			}
		}			

		private void showFirstTimeToast() {
			Handler h = new Handler(context.getMainLooper());

			h.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(context,"Uygulamanýn ilk açýlýþýnda internet baðlantýnýz olmasý gerekmektedir.",Toast.LENGTH_LONG).show();
				}
			});
		}
	}
	
	// Urlden numaralari cekme sirasinda hata yasanirsa bilgi mesaji atilacak
	// kisilerin numaralari baska bir urldeb okunur ve dosyaya yazilir
	private class GetMsisdnInfos extends Thread {
		private Context context;
		public GetMsisdnInfos(Context context) {
			this.context=context;
		}
		
		@Override
		public void run() {
			super.run();
			
			String result;
			try {
				result = Util.getStringFromUrl(Util.alertMsisdnUrl);
				Util.writeStringToFile(context, "msisdn", result);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
}
