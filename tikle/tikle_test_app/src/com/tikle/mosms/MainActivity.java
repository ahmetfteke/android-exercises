package com.tikle.mosms;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.tikle.mosms.models.Operator;

public class MainActivity extends Activity {
	int sp = 0;
	int requestCode;
	Spinner spinner;

	private ArrayList<Operator> operators;

	@Override	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		SharedPreferences prefs = MainActivity.this.getSharedPreferences("mosms", Context.MODE_MULTI_PROCESS);
		Util.sendMessage = prefs.getBoolean("sendMessage", false);
		
		CheckBox cb = (CheckBox)findViewById(R.id.checkBox1);
		cb.setChecked(Util.sendMessage);				
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			// Mesaj gonderimi acilinca o an secili olan operatorle son secilen
			// karsilastirilir. 
			// Eger degisiklik varsa SharedPreferecese changed alanina true 
			// yoksa false eklenir
			// Mesaj gonderimi kapatilinca o an kurulu olan tum alarmlar silinir
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Util.sendMessage = isChecked;
				SharedPreferences prefs = MainActivity.this.getSharedPreferences("mosms",
						Context.MODE_MULTI_PROCESS);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("sendMessage", Util.sendMessage);

				int o = prefs.getInt("operator", 0);
				
				if(spinner == null) {
					deleteAlarm(o);
					editor.commit();
					return;
				}
				
				if(!Util.sendMessage) {
					editor.commit();
					deleteAlarm(o);
					com.tikle.mosms.AlarmManager.getInstance(MainActivity.this).cancelSending();
				}
				else {								
					int position = spinner.getSelectedItemPosition();
					int op = operators.get(position).getType();													
										
					editor.putInt("operator", op);
					if(op != o)
						editor.putBoolean("changed", true);
					else editor.putBoolean("changed", false);
					editor.commit();
					
					createAlarm(op);
				}								
			}
		});

		operators = new ArrayList<>();
		operators.add(Operator.Turkcell);
		operators.add(Operator.Vodafone);
		operators.add(Operator.Avea);

		spinner = (Spinner)findViewById(R.id.spinner1);
		OperatorAdapter adapter = new OperatorAdapter(this, R.layout.spinner_item, operators);
		spinner.setAdapter(adapter);
		
		int op = prefs.getInt("operator", 0);
		if(op == 0)
			spinner.setSelection(0);
		else spinner.setSelection(op - 1);
		
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
//				if(sp==0) {
//					sp++;
//					return;
//				}
				
				if(!Util.sendMessage)
					return;

				int op = operators.get(arg2).getType();
				SharedPreferences prefs = getSharedPreferences("mosms", MODE_MULTI_PROCESS);
				int o = prefs.getInt("operator", 0);							

				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("operator", operators.get(arg2).getType());

				if(o!=op) {
					deleteAlarm(o);
					editor.putBoolean("changed", true);
					
					editor.commit();
					createAlarm(op);
				} else {
					editor.putBoolean("changed", false);
					editor.commit();
					com.tikle.mosms.AlarmManager.getInstance(MainActivity.this).scheduleLastAlarms();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}

		});
	}

	// PendingIntente verilecek requestCode alarmi kurulacak olan operatorun type i
	private void createAlarm(int op) {
		Intent intent = new Intent(getApplicationContext(), AutoStart.class);
		final PendingIntent pIntent = PendingIntent.getBroadcast(MainActivity.this, op,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		long firstMillis = System.currentTimeMillis(); // first run of alarm is immediate
		long intervalMillis = TimeUnit.SECONDS.toMillis(3600 * 6); // 1 hour
		AlarmManager alarm = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);		
		alarm.setRepeating(AlarmManager.RTC, firstMillis, intervalMillis, pIntent);
	}

	// Mesaj gönderme kapatýlýrsa CheckUrlin calismasina gerek yok
	// Operator degisirse eski operatorun alarmi iptal edimeli
	// reqCode eski operatorun type
	private void deleteAlarm(int reqCode) {
		AlarmManager alarmManager = (AlarmManager) MainActivity.this
				.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(getApplicationContext(), AutoStart.class);
		PendingIntent pendingUpdateIntent = PendingIntent.getBroadcast(MainActivity.this, reqCode, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		// Cancel alarms
		try {
			alarmManager.cancel(pendingUpdateIntent);
		} catch (Exception e) {
			Logger.e(getClass().getName(),
					"AlarmManager update was not canceled. shortNumber: "
							+ "AutoStart \n" + e.toString());
		}
	}

	private class OperatorAdapter extends ArrayAdapter<Operator>{

		private ArrayList<Operator> data;
		public OperatorAdapter(Context context, int resource, ArrayList<Operator> data) {
			super(context, resource, data);

			this.data=data;
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			return getCustomView(position, convertView, parent);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return getCustomView(position, convertView, parent);
		}

		private View getCustomView(int position, View convertView, ViewGroup parent){
			View v = convertView;

			if(v==null){
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.spinner_item, parent, false);
			}

			Operator operator = data.get(position);

			TextView tv=(TextView)v.findViewById(R.id.txtName);
			tv.setText(operator.name());
			tv.setGravity(Gravity.CENTER);

			return v;
		}
	}
}
