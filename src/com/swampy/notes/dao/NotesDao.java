package com.swampy.notes.dao;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;

import com.swampy.notes.entity.Note;
import com.swampy.notes.entity.NoteMedia;
import com.swampy.notes.entity.Tag;
import com.swampy.notes.utils.DataBaseHelper;
import com.swampy.notes.utils.DataBaseHelper.KeysNotes;
import com.swampy.notes.utils.DataBaseHelper.KeysNotesMedia;
import com.swampy.notes.utils.DataBaseHelper.KeysNotesTags;
import com.swampy.notes.utils.DataBaseHelper.KeysTags;

/**
 * DAO for notes, provides operations like retrieving, storing modifying notes in SQLite database
 * 
 * @author Saulius Alisauskas
 * 
 */
public class NotesDao {

	/**
	 * Interface to be implemented by Activity
	 * 
	 * @author Saulius Alisauskas
	 *
	 */
	public interface DaoProvider {
		NotesDao getDao();
	}
	private static NotesDao instance;
	private static final String TAG = "NotesDao";
	/**
	 * Get/Create Data object instance
	 * 
	 * @return
	 */
	public static NotesDao getDao() {
		if (null == instance) {
			Log.d(TAG, "Creating new instance");
			instance = new NotesDao();
		}
		return instance;
	}
	public static String join(@SuppressWarnings("rawtypes") Collection s, String delimiter) {
		StringBuffer buffer = new StringBuffer();
		@SuppressWarnings("rawtypes")
		Iterator iter = s.iterator();
		while (iter.hasNext()) {
			buffer.append(iter.next());
			if (iter.hasNext()) {
				buffer.append(delimiter);
			}
		}
		return buffer.toString();
	}
	
	private SQLiteDatabase database;
	
	private DataBaseHelper dbHelper;

	private boolean inited = false;

	/**
	 * Save note media object to database
	 * 
	 * @param noteId
	 * @param media
	 */
	public long addMedia(int noteId, NoteMedia media) {

		if (media.getType().equals(NoteMedia.Type.IMG.toString())) {
			Log.d(TAG, "Persisting IMG for note:" + noteId);
			Bitmap bmp = (Bitmap) media.getData();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // Should compress it (probably)

			ContentValues cv = new ContentValues();
			cv.put(DataBaseHelper.KeysNotesMedia.NOTE_ID.toString(), noteId);
			cv.put(DataBaseHelper.KeysNotesMedia.TITLE.toString(), media.getTitle());
			cv.put(DataBaseHelper.KeysNotesMedia.TYPE.toString(), media.getType());
			cv.put(DataBaseHelper.KeysNotesMedia.DATA.toString(), out.toByteArray());

			long mediaId = database.insert(DataBaseHelper.TABLE_NOTE_MEDIA, null, cv);
			Log.d(TAG, "Media added with id:" + mediaId);
			return mediaId;
		}

		return 0L;

	}

	// TODO: this should not be used anymore
	/**
	 * Add Note to database
	 * 
	 * @param note
	 */
	public void addNote(Note note) {
		Log.d(TAG, "Perisisting note: " + note);
		ContentValues values = new ContentValues(5);

		values.put(KeysNotes.TITLE.toString(), note.getTitle()); // mandatory

		if (note.getText() != null) {
			values.put(KeysNotes.MESSAGE.toString(), note.getText());
		} else {
			Log.w(TAG, "Title was not found in note object");
		}

		if (note.getCalendar() != null) {
			values.put(KeysNotes.DUE_DATE.toString(), note.getCalendar().getTimeInMillis());
		} else {
			Log.w(TAG, "Calender was not found in note object");
		}

		long noteId = database.insert(DataBaseHelper.TABLE_NOTES, null, values);

		// Add note tags
		if (note.getTags() != null) {

			for (Tag tag : note.getTags()) {

				// Add tag to note
				Log.d(TAG, "Attaching tag:" + tag + "(" + tag.mId + ") to note:" + noteId);
				ContentValues noteTagValues = new ContentValues();
				noteTagValues.put(KeysNotesTags.NOTE_ID.toString(), noteId);
				noteTagValues.put(KeysNotesTags.TAG_ID.toString(), tag.mId);
				database.insert(DataBaseHelper.TABLE_NOTE_TAGS, null, noteTagValues);
			}
		}

		// Add note media
		if (note.getMedia().size() > 0) {
			Log.d(TAG, "Found " + note.getMedia().size() + " media attachments");
			for (NoteMedia media : note.getMedia()) {
				if (media.getType().equals(NoteMedia.Type.IMG.toString())) {
					Log.d(TAG, "Persisting IMG with title:" + media.getTitle());
					Bitmap bmp = (Bitmap) media.getData();
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					bmp.compress(Bitmap.CompressFormat.PNG, 0, out);

					ContentValues cv = new ContentValues();
					cv.put(DataBaseHelper.KeysNotesMedia.NOTE_ID.toString(), noteId);
					cv.put(DataBaseHelper.KeysNotesMedia.TITLE.toString(), media.getTitle());
					cv.put(DataBaseHelper.KeysNotesMedia.TYPE.toString(), media.getType());
					cv.put(DataBaseHelper.KeysNotesMedia.NOTE_ID.toString(), out.toByteArray());

					database.insert(DataBaseHelper.TABLE_NOTE_MEDIA, null, cv);
				}
			}
		} else {
			Log.d(TAG, "Note didn't have any media attachments");
		}
	}

	/**
	 * Create a note in database which will be update later
	 * 
	 * @return
	 */
	public long addNoteDummy() {
		Log.d(TAG, "Adding dummy note");
		ContentValues values = new ContentValues(2);
		values.put(KeysNotes.TITLE.toString(), "");
		values.put(KeysNotes.MESSAGE.toString(), "");
		return database.insert(DataBaseHelper.TABLE_NOTES, null, values);
	}

	/**
	 * Add new tag to database
	 * 
	 * @param title
	 * @return
	 */
	public int addTag(String title) {
		return getTagId(title);
	}

	/**
	 * Close DataBaseHelper
	 */
	public void close() {
		Log.d(TAG, "Closing database");
		dbHelper.close();
		instance = null;
	}

	/**
	 * Delete note tags
	 * 
	 * @param noteId
	 */
	private void deleteAllTags(int noteId) {
		Log.d(TAG, "Removing tags of noteId:" + noteId);
		int rows = database.delete(DataBaseHelper.TABLE_NOTE_TAGS, KeysNotesTags.NOTE_ID.toString() + " = '" + noteId
				+ "'", null);
		Log.d(TAG, "Removed " + rows + " rows");
	}

	/**
	 * Delete specific note media object
	 * 
	 * @param mediaId
	 */
	public void deleteMedia(int mediaId) {
		Log.d(TAG, "Deleting media id:" + mediaId);
		int rows = database.delete(DataBaseHelper.TABLE_NOTE_MEDIA, KeysNotesMedia.MEDIA_ID + " = '" + mediaId + "'",
				null);
		Log.d(TAG, "Removed " + rows + " rows");
	}

	/**
	 * Delete Note from database
	 * 
	 * @param mId
	 */
	public void deleteNote(Note note) {
		int noteId = note.getId();
		Log.d(TAG, "Deleting note id:" + noteId + " and all associated entities");

		// Delete tags
		deleteAllTags(noteId);

		// Delete media
		for (NoteMedia media : note.getMedia()) {
			deleteMedia(media.getId());
		}

		// Delete note object
		Log.d(TAG, "Removing note entity:" + noteId);
		int rows = database.delete(DataBaseHelper.TABLE_NOTES, KeysNotesTags.NOTE_ID + " = '" + noteId + "'", null);
		Log.d(TAG, "Removed " + rows + " rows");

	}

	/**
	 * Get all tags
	 * 
	 * @return
	 */
	public List<Tag> getAllTags() {
		List<Tag> tags = new ArrayList<Tag>();
		Cursor cursor = database.query(DataBaseHelper.TABLE_TAGS, new String[] { KeysTags.TAG_ID.toString(),
				KeysTags.NAME.toString() }, null, null, null, null, null);
		while (cursor.moveToNext()) {
			tags.add(new Tag(cursor.getInt(0), cursor.getString(1)));
		}
		Log.d(TAG, "Found tags(" + tags.size() + "):" + tags);
		return tags;
	}

	/**
	 * Get all tags with usage numbers
	 * 
	 * @return
	 */
	public List<Tag> getAllTagsWithCounts() {
		Log.d(TAG, "getAllTagsWithCounts");

		List<Tag> tags = new ArrayList<Tag>();
		Cursor cursor = database.query(DataBaseHelper.TABLE_NOTE_TAGS + " NATURAL JOIN " + DataBaseHelper.TABLE_TAGS,
				new String[] { KeysNotesTags.TAG_ID.toString(), KeysTags.NAME.toString(), "COUNT(*) AS count" },
				null, null, KeysNotesTags.TAG_ID.toString(), null, "count");
		while (cursor.moveToNext()) {
			Tag tag = new Tag(cursor.getInt(0), cursor.getString(1), cursor.getInt(2));
			tags.add(tag);
		}
		Log.d(TAG, "Found tags(" + tags.size() + "):" + tags);
		return (tags.size() > 0) ? tags : null;
	}

	public Note getNote(int noteId) {
		Log.d(TAG, "getNote " + noteId);
		Note note = null;
		final String[] fields = { KeysNotes.NOTE_ID.toString(), KeysNotes.TITLE.toString(),
				KeysNotes.MESSAGE.toString(), KeysNotes.DATE.toString(), KeysNotes.DUE_DATE.toString() };

		Cursor cursor = database.query(DataBaseHelper.TABLE_NOTES, fields, KeysNotes.NOTE_ID.toString() + " = ?",
				new String[] { String.valueOf(noteId) }, null, null, null);

		if (cursor.moveToFirst()) {
			note = new Note();
			note.setId(cursor.getInt(0));
			note.setTitle(cursor.getString(1));
			note.setText(cursor.getString(2));
			note.setDate(cursor.getLong(3));
			String dueDate = cursor.getString(4);
			if (dueDate != null && dueDate.length() > 0) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(Long.valueOf(dueDate));
				note.setCalendar(cal);
			}

			cursor.close();

			// Get note tags
			note.setTags(getNoteTags(noteId));
		}

		if (note == null) {
			Log.d(TAG, "Note id:" + noteId + " not found");
		}

		Log.d(TAG, "Returning note:" + note);
		return note;
	}

	/**
	 * Get image by id
	 * 
	 * @param mId
	 * @return
	 */
	public Cursor getNoteImagesCursor(int noteId) {
		Log.d(TAG, "Get cursor to images of note:" + noteId);
		// TODO: need to add TYPE = IMG..
		return database.query(DataBaseHelper.TABLE_NOTE_MEDIA, new String[] { KeysNotesMedia.MEDIA_ID.toString(),
				KeysNotesMedia.DATA.toString() }, KeysNotesMedia.NOTE_ID.toString() + " = ?",
				new String[] { String.valueOf(noteId) }, null, null, null);
	}

	/**
	 * Get all notes cursor
	 * 
	 * @return
	 */
	public Cursor getNotesCursor() {
		Log.d(TAG, "getNotesCursor()");
		final String[] fields = { KeysNotes.NOTE_ID.toString(), KeysNotes.TITLE.toString(),
				KeysNotes.MESSAGE.toString(), KeysNotes.DATE.toString(), KeysNotes.DUE_DATE.toString(),
				KeysNotes.PRIORITY.toString() };

		return database.query(DataBaseHelper.TABLE_NOTES, fields, null, null, null, null, "date DESC");
	}

	public Cursor getNotesCursor(Integer[] tagIds) {
		Log.d(TAG, "getNotesCursor() with tagIds");
		final String[] fields = { KeysNotes.NOTE_ID.toString(), KeysNotes.TITLE.toString(),
				KeysNotes.MESSAGE.toString(), KeysNotes.DATE.toString(), KeysNotes.DUE_DATE.toString(),
				KeysNotes.PRIORITY.toString() };
		List<String> whereClause = new ArrayList<String>();
		for (Integer tagId : tagIds) {
			whereClause.add(KeysNotesTags.TAG_ID.toString() + " = '" + tagId + "'");
		}

		return database.query(DataBaseHelper.TABLE_NOTE_TAGS + " NATURAL JOIN " + DataBaseHelper.TABLE_NOTES, fields,
				join(whereClause, " OR "), null, null, null, "date DESC");
	}

	/**
	 * Get Tags of specified note
	 * 
	 * @param noteId
	 * @return
	 */
	public List<Tag> getNoteTags(int noteId) {
		Log.d(TAG, "Get tag of note:" + noteId);
		List<Tag> tags = new ArrayList<Tag>();
		Cursor cursor = database.query(DataBaseHelper.TABLE_NOTE_TAGS + " NATURAL JOIN " + DataBaseHelper.TABLE_TAGS,
				new String[] { KeysTags.TAG_ID.toString(), KeysTags.NAME.toString() },
				KeysNotesTags.NOTE_ID.toString() + " = ?", new String[] { String.valueOf(noteId) }, null, null, null);
		while (cursor.moveToNext()) {
			tags.add(new Tag(cursor.getInt(0), cursor.getString(1)));
		}
		Log.d(TAG, "Found tags(" + tags.size() + "):" + tags);
		return (tags.size() > 0) ? tags : null;
	}

	/**
	 * Checks if tag exist, if not creates
	 * 
	 * @param tag
	 * @return tagId
	 */
	private int getTagId(String tag) {
		int tagId = -1;
		Cursor cursor = database.query(DataBaseHelper.TABLE_TAGS, new String[] { KeysTags.TAG_ID.toString() },
				KeysTags.NAME.toString().toLowerCase() + " = '" + tag + "'", null, null, null, null);
		if (cursor.moveToFirst()) {
			tagId = (int) cursor.getLong(0);
			Log.d(TAG, "Tag:" + tag + " found id:" + tagId);
			cursor.close();
		} else {
			// Create new tag in database
			Log.d(TAG, "Tag:" + tag + " not found, creating");
			ContentValues tagValues = new ContentValues(1);
			tagValues.put(KeysTags.NAME.toString(), tag);
			tagId = (int) database.insert(DataBaseHelper.TABLE_TAGS, null, tagValues);
			Log.d(TAG, "Tag:" + tag + " created with id:" + tagId);
		}
		return tagId;
	}

	/**
	 * Constructor, instantiate DataBaseHelper
	 * 
	 * @param context
	 * @return
	 */
	public void init(Context context) {
		Log.d(TAG, "Initing dao");
		if (!inited) {
			dbHelper = new DataBaseHelper(context);
			open();
			inited = true;
		} else {
			Log.w(TAG, "Dao was already inited !");
		}
	}

	/**
	 * Get access to database
	 * 
	 * @throws SQLException
	 */
	private void open() throws SQLException {
		Log.d(TAG, "Openning database");
		database = dbHelper.getWritableDatabase();
	}

	public void updateNote(Note note) {
		Log.d(TAG, "Updating note: " + note);
		ContentValues values = new ContentValues(3);

		if (note.getTitle() != null) {
			values.put(KeysNotes.TITLE.toString(), note.getTitle()); // mandatory
		}

		if (note.getText() != null) {
			values.put(KeysNotes.MESSAGE.toString(), note.getText());
		}

		if (note.getDate() != null) {
			values.put(KeysNotes.DATE.toString(), note.getDate());
		}

		if (note.getCalendar() != null) {
			values.put(KeysNotes.DUE_DATE.toString(), note.getCalendar().getTimeInMillis());
		} else {
			Log.w(TAG, "Calender was not found in note object");
		}

		// Delete tags from database
		deleteAllTags(note.getId());

		// Add note tags
		if (note.getTags() != null) {
			for (Tag tag : note.getTags()) {
				// Add tag to note
				Log.d(TAG, "Attaching tag:" + tag.mId + "(" + tag.mId + ") to note:" + note.getId());
				ContentValues noteTagValues = new ContentValues();
				noteTagValues.put(KeysNotesTags.NOTE_ID.toString(), note.getId());
				noteTagValues.put(KeysNotesTags.TAG_ID.toString(), tag.mId);
				database.insert(DataBaseHelper.TABLE_NOTE_TAGS, null, noteTagValues);
			}
		}

		Log.d(TAG, "Updating note with values:" + values);

		if (values.size() > 0
				&& database.update(DataBaseHelper.TABLE_NOTES, values, KeysNotesTags.NOTE_ID.toString() + " = ?",
						new String[] { String.valueOf(note.getId()) }) < 1) {
			Log.e(TAG, "Error updating note:" + note);
		} else {
			Log.d(TAG, "Note has bee updated");
		}
	}
}
