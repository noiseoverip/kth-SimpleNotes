package com.swampy.notes.fragments;

import static com.swampy.notes.utils.NoteTagsHolder.getTagHolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.swampy.notes.ImageZoomActivity;
import com.swampy.notes.R;
import com.swampy.notes.adaptors.NoteGalleryAdapter;
import com.swampy.notes.entity.Note;
import com.swampy.notes.entity.NoteMedia;
import com.swampy.notes.entity.Tag;
import com.swampy.notes.utils.BitmapHelper;
import com.swampy.notes.utils.LoadGalleryTask;
import com.swampy.notes.utils.OneEntryDialog;
import com.swampy.notes.utils.Static;

/**
 * Fragment for viewing/editing notes
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Add/Set tags</li>
 * <li>Attach image</li>
 * <li>Set due date</li>
 * <li>Set note text</li>
 * <li>Export to calendar</li>
 * <li>Add/Set tags</li>
 * <li>Share oever email, social networks...</li>
 * </ul>
 * </p>
 * 
 * @author Saulius Alisauskas
 * 
 */
public class NoteViewFragment extends NotesAbstractFragment {

	/**
	 * Callback interface
	 * 
	 * @author Saulius Alisauskas
	 * 
	 */
	public interface Callback {

		/**
		 * Called when note has been deleted
		 */
		void onNoteDeleted();

		/**
		 * Note editing has been finished
		 */
		void onNoteEditFinished();
	}

	public static final String ARG_NOTE_ID = "noteId";

	public static final int ARG_NOTE_ID_VAL_NEWNOTE = -1;

	private static final int DATE_DIALOG_ID = 2;
	// Intent request id's
	private static final int REQ_IMG_CAMERA = 1;

	private static final int REQ_IMG_LIBRARY = 2;
	private static final int REQ_SHARE = 3;

	// Dialog id's
	private static final int TIME_DIALOG_ID = 1;

	private Callback mCallback;

	/**
	 * DatePickr OnDateSetListener
	 */
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {

			if (mNote.getCalendar() == null) {
				mNote.setCalendar(Calendar.getInstance());
			}

			mNote.getCalendar().set(Calendar.YEAR, year);
			mNote.getCalendar().set(Calendar.MONTH, month);
			mNote.getCalendar().set(Calendar.DAY_OF_MONTH, day);

			uiUpdateDuaDate();
		}
	};

	/**
	 * ActionBar action mode for editing notes
	 */
	private ActionMode.Callback mEditActionModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			Log.d(TAG, "Clicked on " + item.getItemId());

			switch (item.getItemId()) {
			case Static.ACTION_NOTE_ADD_IMAGE:
				addNewPicture();
				break;
			case Static.ACTION_NOTE_ADD_TAG:
				uiDialogTags();
				break;
			case Static.ACTION_NOTE_SET_DUEDATE:
				getDialog(DATE_DIALOG_ID).show();
				break;
			}
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			uiViewEditingEnable();

			menu.add(0, Static.ACTION_NOTE_ADD_IMAGE, 0, "Add Picutre").setIcon(R.drawable.addpicture)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(0, Static.ACTION_NOTE_ADD_TAG, 0, "Add Picture").setIcon(R.drawable.addtag)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(0, Static.ACTION_NOTE_SET_DUEDATE, 0, "Set due date").setIcon(R.drawable.adddate)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			uiViewEditingDisable();
			if (saveChanges(false)) {
				mCallback.onNoteEditFinished();
			}
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
	};

	/**
	 * Holder for sharing files, used to delete files after share
	 */
	private List<File> mFilesToAttach;

	private Gallery mGallery;

	/**
	 * AsyncTask for loading gallery images in the background
	 */
	private LoadGalleryTask mGalleryTask;
	boolean mIsNew = false; // flag for new note
	private Note mNote;

	boolean mSkipUpdate = false; // flag for skipping saving changes
	/**
	 * TimePicker OnTimeSetListener for time selection
	 */
	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

			if (mNote.getCalendar() == null) {
				mNote.setCalendar(Calendar.getInstance());
			}

			mNote.getCalendar().set(Calendar.HOUR_OF_DAY, hourOfDay);
			mNote.getCalendar().set(Calendar.MINUTE, minute);

			uiUpdateDuaDate();
		}
	};

	private TextView mUITextView;

	// UI views
	private TextView mUITitleView;

	private final String TAG = this.getClass().getSimpleName();

	/**
	 * Add new picture
	 */
	private void addNewPicture() {
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		startActivityForResult(intent, REQ_IMG_CAMERA);

		// Removed functionality to include image from existing images due to issues with memory
		/*
		 * final CharSequence[] items = { "Camera", "Library" }; AlertDialog.Builder builder = new
		 * AlertDialog.Builder(v.getContext()); builder.setTitle("Choose image source"); builder.setItems(items, new
		 * DialogInterface.OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface dialog, int item) { if (item == 0) { Intent intent = new
		 * Intent("android.media.action.IMAGE_CAPTURE"); startActivityForResult(intent, REQ_IMG_CAMERA); } else { Intent
		 * intent = new Intent(Intent.ACTION_PICK); intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		 * MediaStore.Images.Media.CONTENT_TYPE); startActivityForResult(intent, REQ_IMG_LIBRARY); } } }); AlertDialog
		 * alert = builder.create(); alert.show();
		 */
	}

	/**
	 * Animation to zoom into picture
	 */
	private void animZommIn() {
		getActivity().overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
	}

	private Dialog getDialog(int id) {
		Calendar cal;

		switch (id) {
		case TIME_DIALOG_ID:
			cal = (mNote.getCalendar() != null) ? mNote.getCalendar() : Calendar.getInstance();
			return new TimePickerDialog(getActivity(), mTimeSetListener, cal.get(Calendar.HOUR_OF_DAY),
					cal.get(Calendar.MINUTE), true);

		case DATE_DIALOG_ID:
			cal = (mNote.getCalendar() != null) ? mNote.getCalendar() : Calendar.getInstance();
			return new DatePickerDialog(getActivity(), mDateSetListener, cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		}
		return null;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated");

		// Bind gallery
		mGallery = (Gallery) getView().findViewById(R.id.gallery);

		// Bind title view
		mUITitleView = (TextView) getView().findViewById(R.id.title);

		// Bind message
		mUITextView = (TextView) getView().findViewById(R.id.text);

		/*
		 * If noteiD -1 show new, if > 0 load note
		 */
		Bundle arguments = getArguments();
		if (arguments == null) {
			// Should never happen
			Log.e(TAG, "Not arguments provided");

		} else {
			int noteId = arguments.getInt(ARG_NOTE_ID);
			if (noteId == -1) {
				// Start composing new note
				Log.i(TAG, "No note id provided, creating new note");
				mNote = new Note();
				mNote.setId((int) getDao().addNoteDummy());
				mIsNew = true;

				mUITitleView.requestFocus(); // set focus on title, keyboard will appear

				uiStartEditMode();
			} else {
				// Load note
				mNote = getDao().getNote(noteId);

				uiViewEditingDisable();

				populateFields();

				mGalleryTask = new LoadGalleryTask(mNote, getDao(), mGallery);
				mGallery.setAdapter(new NoteGalleryAdapter(getActivity(), mNote));
				mGalleryTask.execute();
			}
		}

		// Setup gallery on click
		mGallery.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

				// Launch Zoom activity
				Intent intent = new Intent().setClass(v.getContext(), ImageZoomActivity.class);
				intent.putExtra("noteId", mNote.getId());
				startActivity(intent);

				animZommIn();

			}
		});

		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult()");
		if (requestCode == REQ_SHARE) {
			if (mFilesToAttach != null && mFilesToAttach.size() > 0) {
				Log.d(TAG, "Found share files to delete");
				for (File file : mFilesToAttach) {
					Log.d(TAG, "Deleting file: " + file.getPath());
					file.delete();
				}
				mFilesToAttach = null;
			}
		}

		if (resultCode == Activity.RESULT_OK) {

			Bitmap bmp = null;
			if (requestCode == REQ_IMG_CAMERA) {
				Bundle extras = data.getExtras();

				if (extras == null) {
					Toast.makeText(getActivity(), "Image was no taken", Toast.LENGTH_SHORT).show();
					return;
				}

				bmp = (Bitmap) extras.get("data");

			} else if (requestCode == REQ_IMG_LIBRARY) { // Not used currently

				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null,
						null);
				if (cursor.moveToFirst()) {

					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
					String filePath = cursor.getString(columnIndex);
					cursor.close();

					bmp = BitmapFactory.decodeFile(filePath);
				}
			}

			if (bmp != null) {
				Log.i(TAG, "Image was imported. Size:" + bmp.getRowBytes());

				// Save bitmap to database
				NoteMedia media = new NoteMedia("", NoteMedia.Type.IMG, bmp);
				getDao().addMedia(mNote.getId(), media);

				// Scale bitmap for gallery view
				media.setData(BitmapHelper.decodeBitmap((Bitmap) media.getData(), 150, 150));

				// Attach image to note object
				mNote.getMedia().add(media);

				// Unhide gallery
				mGallery.setVisibility(View.VISIBLE);

				// update gallery
				if (mGallery.getAdapter() == null) {
					mGallery.setAdapter(new NoteGalleryAdapter(getActivity(), mNote));
				} else {
					((NoteGalleryAdapter) mGallery.getAdapter()).notifyDataSetChanged();
				}
			}
		}
	}

	/**
	 * Define action items
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		menu.add(0, Static.ACTION_NOTE_EXPORT_TO_CALENDAR, 0, "Export to Callendar").setIcon(R.drawable.callendar)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(0, Static.ACTION_NOTE_SHARE, 0, "Share").setIcon(R.drawable.share)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(0, Static.ACTION_NOTE_EDIT, 0, "Edit").setIcon(R.drawable.edit)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(0, Static.ACTION_NOTE_DELETE, 0, "Delete").setIcon(R.drawable.removediscard)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		return inflater.inflate(R.layout.fragment_note_layout, container, false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Handle taps on option items
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case Static.ACTION_NOTE_SHARE:
			uiShare();
			break;

		case Static.ACTION_NOTE_DELETE:

			AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
			String title = "Are you sure you want to delete this note ?";
			builder.setMessage(title).setCancelable(false)
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							getDao().deleteNote(mNote);
							mSkipUpdate = true;
							mCallback.onNoteDeleted();
						}
					}).setNegativeButton("No", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});

			AlertDialog alert = builder.create();
			alert.show();
			break;

		case Static.ACTION_NOTE_EDIT:
			uiStartEditMode();
			break;

		case Static.ACTION_NOTE_EXPORT_TO_CALENDAR:

			Calendar cal = mNote.getCalendar();
			Intent intent = new Intent(Intent.ACTION_EDIT);
			intent.setType("vnd.android.cursor.item/event");

			if (cal != null) {
				intent.putExtra("beginTime", cal.getTimeInMillis());
				intent.putExtra("endTime", cal.getTimeInMillis() + 60 * 60 * 1000);
			}

			intent.putExtra("title", mNote.getTitle());
			intent.putExtra("description", mNote.getText());
			startActivity(intent);

			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause()");
		super.onPause();
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume()");
		super.onStart();
	}

	/**
	 * Populate note fields used when loading existing note
	 * 
	 */
	private void populateFields() {
		// Set title
		mUITitleView.setText(mNote.getTitle());

		// Set text
		mUITextView.setText(mNote.getText());

		// Show tags
		uiUpdateTags();

		// Show due time
		uiUpdateDuaDate();
	}

	/**
	 * Check if mandatory fields have been filled and attach information to note object
	 * 
	 * @param validate
	 * @return
	 */
	private boolean processFields(boolean validate) {

		// Check and add title
		String title = mUITitleView.getText().toString();
		Log.d(TAG, "Adding title: " + title);
		if (title == null || title.length() < 1) {
			if (validate) {
				uiShowError("Please provide title");
				return false;
			}
		} else {
			mNote.setTitle(new String(title));
		}

		// Check and add text
		String text = mUITextView.getText().toString();
		// Log.d(TAG, "Adding text: " + title);
		// if (text == null || text.length() < 1) {
		// if (validate) {
		// uiShowError("Please provide text", mUITextView.getContext());
		// return false;
		// }
		// } else {
		// mNote.setText(new String(text));
		// }
		mNote.setText(new String(text));

		// Set date only first time
		if (mNote.getDate() == null) {
			mNote.setDate(String.valueOf(new Date().getTime()));
		} else {
			mNote.setDate(null);
		}

		return true;
	}

	/**
	 * Create note/save changes
	 */
	private boolean saveChanges(boolean validate) {
		boolean result = processFields(validate);
		if (!mSkipUpdate && result) {
			getDao().updateNote(mNote);
		}
		return result;
	}

	/**
	 * Set callback handler
	 * 
	 * @param callback
	 */
	public void setCallback(Callback callback) {
		this.mCallback = callback;
	}

	/**
	 * Present user with new tag dialog
	 */
	private void uiDialogAddTag() {
		OneEntryDialog dialog = new OneEntryDialog(getActivity(), new OneEntryDialog.Callback() {

			@Override
			public void selectedNegative(DialogInterface dialog, String entryText) {
				dialog.dismiss();
			}

			@Override
			public void selectedPositive(DialogInterface dialog, String entryText) {
				if (null != entryText && entryText.length() > 0) {
					// Create new tag
					int tagId = getDao().addTag(entryText);
					if (tagId < 1) {
						Toast.makeText(getActivity(), "Error adding new tag", Toast.LENGTH_SHORT).show();
						return;
					}
					// Tag current position with newly created tag
					mNote.getTags().add(new Tag(tagId, entryText));

					// Close dialog
					dialog.dismiss();
					uiUpdateTags();
				} else {
					Toast.makeText(getActivity(), "Please type in tag name first", Toast.LENGTH_SHORT).show();
				}

			}
		});
		// Show dialog
		dialog.create("Create tag", "OK", "Cancel").show();
	}

	/**
	 * Present user with tag selection dialog
	 */
	private void uiDialogTags() {
		// Set tags to display
		getTagHolder().init(getDao().getAllTags(), mNote.getTags());

		if (getTagHolder().getTagNames().length < 1) {
			uiDialogAddTag();
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select Tags");
		builder.setPositiveButton("New Tag", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				uiDialogAddTag();
			}
		});

		builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		builder.setMultiChoiceItems(getTagHolder().getTagNames(), getTagHolder().getSelectedTags(),
				new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int item, boolean isChecked) {
						getTagHolder().setItemValue(item, isChecked);
						if (isChecked) {
							mNote.getTags().add(getTagHolder().getTag(item));
						} else {
							mNote.getTags().remove(getTagHolder().getTag(item));
						}
						uiUpdateTags();
					}
				});

		builder.create().show();
	}

	/**
	 * Share not (title, text and images) using Intent.ACTION_SEND_MULTIPLE
	 */
	private void uiShare() {

		Log.d(TAG, "Sharing note");

		/**
		 * Create message text:
		 */
		StringBuilder messageText = new StringBuilder();

		messageText.append("Note from Notes app:");
		messageText.append("\n\n\n");

		// title
		messageText.append(mNote.getTitle());
		messageText.append("\n\n\n");

		// text
		messageText.append(mNote.getText());
		messageText.append("\n\n");

		// Temporary save images from database to public storage for sharing
		Cursor cursor = getDao().getNoteImagesCursor(mNote.getId());
		int i = 0;
		while (cursor.moveToNext()) {
			i++;

			if (mFilesToAttach == null) {
				mFilesToAttach = new ArrayList<File>();
			}

			Log.d(TAG, "Storing image nr:" + i + " into cache");
			File imageFile = new File(Environment.getExternalStorageDirectory(), "notesShare" + i + ".jpg");
			mFilesToAttach.add(imageFile);

			byte[] imageByteArray = cursor.getBlob(1);
			ByteArrayInputStream bis = new ByteArrayInputStream(imageByteArray);
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(imageFile);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = bis.read(buffer)) > 0) {
					fos.write(buffer, 0, length);
				}

				bis.close();
				fos.flush();
				fos.close();

			} catch (Exception e) {
				Log.e(TAG, "Cought exception while copying images to cache", e);
			}
		}

		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.putExtra(Intent.EXTRA_SUBJECT, "Note share:" + mNote.getTitle());
		intent.putExtra(Intent.EXTRA_TEXT, messageText.toString());
		intent.setType("text/plain");
		ArrayList<Uri> uris = new ArrayList<Uri>();
		if (null != mFilesToAttach) {
			for (File file : mFilesToAttach) {
				Log.d(TAG, "Adding image to share path:" + file.getPath());
				try {
					String url = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
							file.getAbsolutePath(), file.getName(), null);
					Log.d(TAG, "Created URL to image:" + url);
					uris.add(Uri.parse(url));
				} catch (Exception e) {
					Log.e(TAG, "Error while adding image to media store", e);
				}
			}
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			intent.setType("image/jpg");
		}

		startActivityForResult(Intent.createChooser(intent, "Choose how to share"), REQ_SHARE);
	}

	private void uiShowError(String message) {
		Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
	}

	/**
	 * Start editing mode
	 */
	private void uiStartEditMode() {
		getSherlockActivity().startActionMode(mEditActionModeCallback);
	}

	/**
	 * Helper method to display due date
	 * 
	 * @param cal
	 */
	private void uiUpdateDuaDate() {
		TextView dateView = (TextView) getView().findViewById(R.id.duaDate);

		if (null != mNote.getCalendar()) {
			dateView.setVisibility(View.VISIBLE);
			dateView.setText("Dua date: " + mNote.getDueDate());
		} else {
			dateView.setVisibility(View.GONE);
		}
	}

	/**
	 * Helper method to display tags
	 */
	private void uiUpdateTags() {
		TextView tagsView = (TextView) getView().findViewById(R.id.tags);

		List<Tag> tags = mNote.getTags();
		if (null != tags && tags.size() > 0) {
			tagsView.setVisibility(View.VISIBLE);
			StringBuilder tagsString = new StringBuilder("Tags: ");
			for (Tag tag : tags) {
				tagsString.append(tag.mName);
				if (tags.indexOf(tag) != (tags.size() - 1)) {
					tagsString.append(",");
				}
			}
			tagsView.setText(tagsString.toString());
		} else {
			tagsView.setVisibility(View.GONE);
		}
	}

	/**
	 * Disable view editing
	 */
	private void uiViewEditingDisable() {
		mUITitleView.setFocusable(false);
		mUITextView.setFocusable(false);
	}

	/**
	 * Enable view editing
	 */
	private void uiViewEditingEnable() {
		Log.d(TAG, "Enabling editing");
		mUITitleView.setFocusable(true);
		mUITitleView.setFocusableInTouchMode(true);
		mUITextView.setFocusable(true);
		mUITextView.setFocusableInTouchMode(true);
	}
}
