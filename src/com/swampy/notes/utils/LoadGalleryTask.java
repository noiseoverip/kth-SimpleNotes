package com.swampy.notes.utils;

import java.io.ByteArrayInputStream;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Gallery;

import com.swampy.notes.adaptors.NoteGalleryAdapter;
import com.swampy.notes.dao.NotesDao;
import com.swampy.notes.entity.Note;
import com.swampy.notes.entity.NoteMedia;
import com.swampy.notes.entity.NoteMedia.Type;

/**
 * AsyncTask for loading gallery images, images apear one after another
 * 
 * @author Saulius Alisauskas
 *
 */
public class LoadGalleryTask extends AsyncTask<String, Boolean, String> {

	private static final String TAG = "LoadGalleryTask";
	private NotesDao mDao;
	private Gallery mGallery;
	private Note mNote;
	
	/**
	 * Main constructor
	 * 
	 * @param note
	 * @param dao
	 * @param gallery
	 */
	public LoadGalleryTask(Note note, NotesDao dao, Gallery gallery) {
		this.mNote = note;
		this.mDao = dao;
		this.mGallery = gallery;
		Log.d(TAG, "Instatiated");
	}

	@Override
	protected String doInBackground(String... params) {
		Log.d(TAG, "doInBackground");

		// Get cursor to images
		Cursor cursor = mDao.getNoteImagesCursor(mNote.getId());
		int totalImages = cursor.getCount();
		Log.d(TAG, "Found images:" + totalImages + " for note:" + mNote.getId());

		if (totalImages < 1) {
			publishProgress(false);
		}

		int i = 0;
		while (cursor.moveToNext()) {
			i++;
			Log.d(TAG, "Image:" + (i) + " loading...");

			byte[] imageByteArray = cursor.getBlob(1);
			if (imageByteArray != null) {
				// Get image from database
				ByteArrayInputStream imageStream = new ByteArrayInputStream(imageByteArray);
				Bitmap bmp = BitmapFactory.decodeStream(imageStream);

				// Create NoteMedia and add a scaled version of bitmap
				NoteMedia media = new NoteMedia("", Type.IMG, BitmapHelper.decodeBitmap(bmp, 150, 150));

				// Add NoteMedia object to Note object
				mNote.getMedia().add(media);

				publishProgress(true);

			}
			Log.d(TAG, "Image:" + (i) + " loaded");
		}

		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void onPostExecute(String result) {
		Log.d(TAG, "Finished");
	}
	
	/**
	 * Notified adapter about data set change, handles setting gallery to be visible
	 * 
	 * {@inheritDoc}
	 */
	protected void onProgressUpdate(Boolean... progress) {
		if (progress[0]) {
			if (mGallery.getVisibility() != View.VISIBLE) {
				mGallery.setVisibility(View.VISIBLE);
			}
			((NoteGalleryAdapter) mGallery.getAdapter()).notifyDataSetChanged();
		} else {
			mGallery.setVisibility(View.GONE);
		}
	}

}
