package com.tikle.mosms.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tikle.mosms.Logger;

public class DBHelper extends SQLiteOpenHelper {
	  public static final String TABLE_MESSAGES = "Messages";
	  public static final String COLUMN_MsgID = "ID";
	  public static final String COLUMN_ShortNumber = "ShortNumber";
	  public static final String COLUMN_Text="Text";
	  public static final String COLUMN_SleepMinutes="SleepMinutes";
	  public static final String COLUMN_Operator="Operator";
	  public static final String COLUMN_Description="Description";
	  
	  public static final String TABLE_ALARMS = "Alarms";
	  public static final String COLUMN_RequestCode="RequestCode";
	  public static final String COLUMN_AlarmSleep="SleepMinutes";
	  
	  public static final String TABLE_LASTOPERATOR = "LastOperator";
	  public static final String COLUMN_LastOperator="Operator";

	  private static final String DATABASE_NAME = "mosms.db";
	  private static int DATABASE_VERSION = 2;

	  public static int getDATABASE_VERSION() {
		return DATABASE_VERSION;
	}

	public static void setDATABASE_VERSION(int dATABASE_VERSION) {
		DATABASE_VERSION = dATABASE_VERSION;
	}

	// Database creation sql statement
	  private static final String MESSAGES_CREATE = "create table "
	      + TABLE_MESSAGES + "(" + COLUMN_MsgID
	      + " integer not null, "
	      + COLUMN_ShortNumber + " text not null, "
	      + COLUMN_Text + " text, "
	      + COLUMN_SleepMinutes + " integer not null, "
	      + COLUMN_Operator + " integer not null, "
	      + COLUMN_Description + " text);";
	  
	  private static final String ALARMS_CREATE = "create table "
		      + TABLE_ALARMS + "(" + COLUMN_RequestCode 
		      + " integer not null, "
		      + COLUMN_AlarmSleep + " integer not null);";
	  
	  private static final String LASTOPERATOR_CREATE = "create table "
		      + TABLE_LASTOPERATOR + "(" + COLUMN_LastOperator
		      + " text not null);";

	  public DBHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }
	  
	  public DBHelper(Context context, int dbVersion) {
		    super(context, DATABASE_NAME, null, dbVersion);
	  }

	  @Override
	  public void onCreate(SQLiteDatabase database) {
	    database.execSQL(MESSAGES_CREATE);
	    database.execSQL(ALARMS_CREATE);
	    database.execSQL(LASTOPERATOR_CREATE);
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Logger.i(DBHelper.class.getName(),
	        "Upgrading database from version " + oldVersion + " to "
	            + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMS);
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_LASTOPERATOR);
	    onCreate(db);
	  }
}
