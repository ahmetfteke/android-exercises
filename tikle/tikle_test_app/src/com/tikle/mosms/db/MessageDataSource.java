package com.tikle.mosms.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.tikle.mosms.Logger;
import com.tikle.mosms.models.Message;
import com.tikle.mosms.models.Operator;

public class MessageDataSource {

	// Database fields
	private SQLiteDatabase database;
	private DBHelper dbHelper;
	private String[] allColumns = { DBHelper.COLUMN_MsgID, DBHelper.COLUMN_ShortNumber,
			DBHelper.COLUMN_Text, DBHelper.COLUMN_SleepMinutes,
			DBHelper.COLUMN_Operator, DBHelper.COLUMN_Description };

	public MessageDataSource(Context context) {
		dbHelper = new DBHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public boolean createMessage(Message message) {
		ContentValues values = new ContentValues();
		values.put(DBHelper.COLUMN_MsgID, message.getID());
		values.put(DBHelper.COLUMN_ShortNumber, message.getShortNumber());
		values.put(DBHelper.COLUMN_Text, message.getText());
		values.put(DBHelper.COLUMN_SleepMinutes, message.getSleepMinutes());
		values.put(DBHelper.COLUMN_Operator, message.getOperator().getType());
		values.put(DBHelper.COLUMN_Description, message.getDescription());
		long insertId = database.insert(DBHelper.TABLE_MESSAGES, null, values);
		Logger.i(getClass().getName(), message.getOperator().getType() + " " + message.getShortNumber() + " kaydedildi.");
		return insertId > 0;
	}	

	public void deleteMessage(Message message) {
		long id = message.getID();
		System.out.println("Message deleted with id: " + message.getOperator().getType() + " " + message.getShortNumber());
		database.delete(DBHelper.TABLE_MESSAGES, DBHelper.COLUMN_MsgID
				+ " = " + id, null);
	}

	public void deleteAllMessages() {
		ArrayList<Message> messages = getAllMessages();
		for (Message message : messages) {
			deleteMessage(message);
		}
	}

	public Message getMessage(int id) {
		Cursor cursor = database.query(DBHelper.TABLE_MESSAGES, allColumns,
				DBHelper.COLUMN_MsgID + "=?",
				new String[] { String.valueOf(id) }, null, null, null);
		if (cursor.moveToFirst())
			return cursorToMessage(cursor);
		else
			return null;
	}

	public ArrayList<Message> getAllMessages() {
		ArrayList<Message> messages = new ArrayList<Message>();

		Cursor cursor = database.query(DBHelper.TABLE_MESSAGES, allColumns,
				null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Message message = cursorToMessage(cursor);
			messages.add(message);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return messages;
	}
	
	public boolean existsShortNumber(String shortNumber) {
		Cursor cursor = database.query(DBHelper.TABLE_MESSAGES, new String[]{ DBHelper.COLUMN_ShortNumber }, 
				DBHelper.COLUMN_ShortNumber + "=?", new String[] { shortNumber }, null, null, null);
		
		cursor.moveToFirst();
		Logger.i(getClass().getName(), "cursor count--->" + cursor.getCount());
		if(cursor == null || cursor.getCount() == 0)
			return false;
		
		return true;
	}

	private Message cursorToMessage(Cursor cursor) {
		Message message = new Message();
		message.setID(cursor.getInt(0));
		message.setShortNumber(cursor.getString(1));
		message.setText(cursor.getString(2));
		message.setSleepMinutes(cursor.getInt(3));
		message.setOperator(Operator.fromType(cursor.getInt(4)));
		message.setDescription(cursor.getString(5));

		return message;
	}
}
