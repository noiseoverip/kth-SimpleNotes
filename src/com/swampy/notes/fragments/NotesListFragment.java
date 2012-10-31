package com.swampy.notes.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.swampy.notes.R;
import com.swampy.notes.adaptors.NotesListAdaptor;
import com.swampy.notes.entity.Tag;
import com.swampy.notes.utils.BitmapHelper;
import com.swampy.notes.utils.Static;

/**
 * Fragment for displaying list of notes.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Filtering by tags</li>
 * </ul>
 * </p>
 * 
 * @author Saulius Alisauskas
 * 
 */
public class NotesListFragment extends NotesAbstractFragment {

	/**
	 * Callback interface
	 * 
	 * @author Saulius Alisauskas
	 * 
	 */
	public interface Callback {
		void onComposeNewComment();

		void onListItemClick(int positionInList, int itemId);
	}

	private static final String TAG = "NotesListFragment";

	private int mActivatedPosition = ListView.INVALID_POSITION;
	private Callback mCallback;
	private NotesListAdaptor mListAdapter;

	/**
	 * List item click listener
	 */
	private OnItemClickListener mListItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			mCallback.onListItemClick(position, mListAdapter.getItemIdAtPosition(position));
		}
	};

	private ListView mListView;

	private boolean[] mSelectedTags;
	private Integer[] mTagIdsArray;
	private List<Tag> mTags;

	private boolean[] getSelectedTags() {
		return mSelectedTags;
	}

	private CharSequence[] getTagsWithCounts() {
		mTags = getDao().getAllTagsWithCounts();

		if (mTags == null) {
			return null;
		}

		String[] array = new String[mTags.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = mTags.get(i).mName + " (" + mTags.get(i).mCount + ")";
		}
		return array;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Find out what scaling to use for images
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		BitmapHelper.mCcaling = dm.density;

		setHasOptionsMenu(true);

		Cursor cursor = getDao().getNotesCursor();
		if (null != cursor && cursor.getCount() == 0) {
			mCallback.onComposeNewComment();
		}

		mListAdapter = new NotesListAdaptor(getActivity(), cursor);
		mListView.setAdapter(mListAdapter);
	}

	/**
	 * Define action items
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(0, Static.ACTION_NOTES_LIST_SHOW_TAGS, 0, "Tags").setIcon(R.drawable.tags)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(0, Static.ACTION_NOTES_LIST_NEW_NOTE, 0, "New Note").setIcon(R.drawable.newnote)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.fragment_note_list_layout, container, false);
		mListView = (ListView) view.findViewById(android.R.id.list);
		mListView.setOnItemClickListener(mListItemClickListener);
		return view;
	}

	/**
	 * Handle taps on option items
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case Static.ACTION_NOTES_LIST_SHOW_TAGS:
			showTagSelectionDialog();
			break;
		case Static.ACTION_NOTES_LIST_NEW_NOTE:
			mCallback.onComposeNewComment();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause(");
		super.onPause();
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart()");
		super.onStart();
	}

	/**
	 * Refresh list
	 */
	public void refreshList() {
		Log.d(TAG, "Refresh list");
		updateCursor();
	}

	/**
	 * Highligh selected list item
	 * 
	 * @param position
	 */
	public void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			mListView.setItemChecked(mActivatedPosition, false);
		} else {
			mListView.setItemChecked(position, true);
		}
		mActivatedPosition = position;
	}

	/**
	 * Set choice mode
	 * 
	 * @param activateOnItemClick
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		mListView.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
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
	 * Display Tag selection dialog
	 */
	private void showTagSelectionDialog() {
		final CharSequence[] tagsArray = getTagsWithCounts();
		if (tagsArray == null || tagsArray.length < 1) {
			Toast.makeText(getActivity(), "You have not created any tags yet", Toast.LENGTH_LONG).show();
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Choose multiple tags");
		builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				// renew cursor
				Log.d(TAG, "Selected tags:" + mSelectedTags);
				List<Integer> tagIds = new ArrayList<Integer>();
				for (int i = 0; i < mSelectedTags.length; i++) {
					if (mSelectedTags[i]) {
						tagIds.add(mTags.get(i).mId);
					}
				}

				if (tagIds.size() > 0) {
					mTagIdsArray = (Integer[]) tagIds.toArray(new Integer[tagIds.size()]);
					updateCursor();
				}
			}
		});

		builder.setNegativeButton("Clear", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if (mSelectedTags != null) {
					Arrays.fill(mSelectedTags, false);
					mTagIdsArray = null;
					updateCursor();
				}
			}
		});

		builder.setMultiChoiceItems(tagsArray, getSelectedTags(), new DialogInterface.OnMultiChoiceClickListener() {
			public void onClick(DialogInterface dialog, int item, boolean isChecked) {
				if (mSelectedTags == null) {
					mSelectedTags = new boolean[tagsArray.length];
					Arrays.fill(mSelectedTags, false);
				}

				mSelectedTags[item] = isChecked;
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}

	private void updateCursor() {
		if (null != mTagIdsArray) {
			mListAdapter.setCursor(getDao().getNotesCursor(mTagIdsArray));
		} else {
			mListAdapter.setCursor(getDao().getNotesCursor());
		}
	}
}
