package com.haraye.friendlocation.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocalStorageHandler extends SQLiteOpenHelper {

	private static final String TAG = LocalStorageHandler.class.getSimpleName();
	
	private static final String DATABASE_NAME = "AndroidIM.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final String _ID = "_id";
	private static final String TABLE_NAME_MESSAGES = "friends_messages";
	public static final String MESSAGE_RECEIVER = "receiver";
	public static final String MESSAGE_SENDER = "sender";
	public static final String MESSAGE_RECEIVER_ID = "receiverId";
	public static final String MESSAGE_SENDER_ID= "senderId";
	private static final String MESSAGE_MESSAGE = "message";
	
	
	private static final String TABLE_MESSAGE_CREATE
	= "CREATE TABLE " + TABLE_NAME_MESSAGES
	+ " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
	+ MESSAGE_RECEIVER + " VARCHAR(25), "
	+ MESSAGE_SENDER + " VARCHAR(25), "
	+ MESSAGE_RECEIVER_ID + " VARCHAR(25), "
	+ MESSAGE_SENDER_ID + " VARCHAR(25), "
	+MESSAGE_MESSAGE + " VARCHAR(255));";
	
	private static final String TABLE_MESSAGE_DROP = 
			"DROP TABLE IF EXISTS "
			+ TABLE_NAME_MESSAGES;
	
	
	public LocalStorageHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_MESSAGE_CREATE);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrade der DB von V: "+ oldVersion + " zu V:" + newVersion + "; Alle Daten werden gelöscht!");
		db.execSQL(TABLE_MESSAGE_DROP);
		onCreate(db);
		
	}
	
	public void insert(String sender, String senderId, String receiver, String receiverId, String message){
		long rowId = -1;
		try{
			
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(MESSAGE_RECEIVER, receiver);
			values.put(MESSAGE_RECEIVER_ID, receiverId);
			values.put(MESSAGE_SENDER, sender);
			values.put(MESSAGE_SENDER_ID, senderId);
			values.put(MESSAGE_MESSAGE, message);
			rowId = db.insert(TABLE_NAME_MESSAGES, null, values);
			
		} catch (SQLiteException e){
			Log.e(TAG, "insert()", e);
		} finally {
			Log.d(TAG, "insert(): rowId=" + rowId);
		}
		
	}
	
	public Cursor get(String senderId, String receiverId) {
					
			SQLiteDatabase db = getWritableDatabase();
			String SELECT_QUERY = "SELECT * FROM " + TABLE_NAME_MESSAGES + " WHERE " + MESSAGE_SENDER_ID + " LIKE '" + senderId + "' AND " + MESSAGE_RECEIVER_ID + " LIKE '" + receiverId + "' OR " + MESSAGE_SENDER_ID + " LIKE '" + receiverId+ "' AND " + MESSAGE_RECEIVER_ID + " LIKE '" + senderId + "' ORDER BY " + _ID + " ASC";
			Log.e("Cursor For GET"," "+db.rawQuery(SELECT_QUERY,null).toString());
			return db.rawQuery(SELECT_QUERY,null);
			
			//return db.query(TABLE_NAME_MESSAGES, null, MESSAGE_SENDER + " LIKE ? OR " + MESSAGE_SENDER + " LIKE ?", sender , null, null, _ID + " ASC");
		
	}
	
	

}
