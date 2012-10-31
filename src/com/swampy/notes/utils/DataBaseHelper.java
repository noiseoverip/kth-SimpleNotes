package com.swampy.notes.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Database helper class for handling database creation.
 * 
 * @author Saulius Alisauskas
 *
 */
public class DataBaseHelper extends SQLiteOpenHelper {

	// Database table names
	public enum KeysNotes {
		DATE, DUE_DATE, MESSAGE, NOTE_ID, PRIORITY, TITLE
	}

	public enum KeysNotesMedia {
		DATA, MEDIA_ID, NOTE_ID, TITLE, TYPE
	};

	public enum KeysNotesTags {
		NOTE_ID, TAG_ID
	};

	public enum KeysTags {
		NAME, TAG_ID
	};	

	private static final String DATABASE_NAME = "notes.db";

	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_NOTE_MEDIA = "notes_media";

	public static final String TABLE_NOTE_TAGS = "notes_tags";;

	public static final String TABLE_NOTES = "notes";

	public static final String TABLE_TAGS = "tags";

	private static final String TAG = "DataBaseHelper";
	
	/****************************************/
	/** Commands to create required tables **/
	/****************************************/

	private static final String CREATE_TABLE_NOTES = "CREATE TABLE " + TABLE_NOTES
			+ " (note_id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , date INTEGER, title VARCHAR, "
			+ "message TEXT check(typeof(`message`) = 'text') , due_date INTEGER, priority INTEGER);";

	private static final String CREATE_TABLE_NOTES_MEDIA = "CREATE TABLE "
			+ TABLE_NOTE_MEDIA
			+ " (media_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, note_id INTEGER, title VARCHAR, type VARCHAR, data BLOB);";

	private static final String CREATE_TABLE_NOTES_TAGS = "CREATE TABLE " + TABLE_NOTE_TAGS
			+ " (note_id INTEGER, tag_id INTEGER);";

	private static final String CREATE_TABLE_TAGS = "CREATE TABLE " + TABLE_TAGS
			+ " (tag_id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , name VARCHAR);";
	
	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.d(TAG, "Creating tables");
		database.execSQL(CREATE_TABLE_NOTES);
		database.execSQL(CREATE_TABLE_NOTES_TAGS);
		database.execSQL(CREATE_TABLE_NOTES_MEDIA);
		database.execSQL(CREATE_TABLE_TAGS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// do nothing for now
	}

}
