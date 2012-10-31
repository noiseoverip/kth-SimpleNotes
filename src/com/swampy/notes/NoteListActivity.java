package com.swampy.notes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.swampy.notes.dao.NotesDao;
import com.swampy.notes.fragments.NoteViewFragment;
import com.swampy.notes.fragments.NoteViewFragmentEmpty;
import com.swampy.notes.fragments.NotesListFragment;

/**
 * 
 * Main activity. Manages notes list and detail view fragments when in tablet mode
 * 
 * @author Saulius Alisauskas
 * 
 */
public class NoteListActivity extends NotesAbstractActivity  implements NotesListFragment.Callback,
		NoteViewFragment.Callback, NotesDao.DaoProvider {

	private static final String TAG = "NoteListActivity";

	private NotesListFragment listFragment;

	/**
	 * Indicator for two pain or single view
	 */
	boolean twoPane = false;

	@Override
	public void onComposeNewComment() {
		Log.d(TAG, "onComposeNewComment()");

		Bundle arguments = new Bundle();
		arguments.putInt(NoteViewFragment.ARG_NOTE_ID, NoteViewFragment.ARG_NOTE_ID_VAL_NEWNOTE);

		if (twoPane) {
			NoteViewFragment fragment = new NoteViewFragment();
			fragment.setArguments(arguments);
			fragment.setCallback(this);
			getSupportFragmentManager().beginTransaction().replace(R.id.detail, fragment).commit();
		} else {
			Intent intent = new Intent(this, NoteActivity.class);
			intent.putExtras(arguments);
			startActivity(intent);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		// Set theme
		setTheme(R.style.Theme_Sherlock_Light);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notes_list);		

		// Set action bar settings
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(false);

		// Set menu as navigation and ad OnNavigationListener
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		listFragment = ((NotesListFragment) getSupportFragmentManager().findFragmentById(R.id.list));
		listFragment.setCallback(this);

		if (null != this.findViewById(R.id.detail)) {
			twoPane = true;
			// Set mode for marking a clicked list item
			listFragment.setActivateOnItemClick(true);
			// Attach empty detail fragment
			NoteViewFragmentEmpty fragment = new NoteViewFragmentEmpty();
			getSupportFragmentManager().beginTransaction().replace(R.id.detail, fragment).commit();
		}
	}

	@Override
	public void onListItemClick(int positionInList, int itemId) {
		Log.d(TAG, "onListItemClick " + positionInList + " " + itemId);

		if (twoPane) {
			listFragment.setActivateOnItemClick(true);
			listFragment.setActivatedPosition(positionInList);

			NoteViewFragment fragment = new NoteViewFragment();
			Bundle arguments = new Bundle();
			arguments.putInt(NoteViewFragment.ARG_NOTE_ID, itemId);
			fragment.setArguments(arguments);
			fragment.setCallback(this);
			getSupportFragmentManager().beginTransaction().replace(R.id.detail, fragment).commit();

		} else {
			Intent intent = new Intent(this, NoteActivity.class);
			intent.putExtra("noteId", itemId);
			startActivity(intent);
		}
	}

	@Override
	public void onNoteDeleted() {
		// Switch fragment to empty fragment
		NoteViewFragmentEmpty fragment = new NoteViewFragmentEmpty();
		getSupportFragmentManager().beginTransaction().replace(R.id.detail, fragment).commit();
		listFragment.refreshList();
	}
	
	@Override
	public void onNoteEditFinished() {
		listFragment.refreshList();
	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause()");
		super.onPause();		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()");		
		
		if (!twoPane) {
			listFragment.refreshList();
		}
	}

	@Override
	protected void onStart() {		
		super.onStart();
		Log.d(TAG, "onStart");
	}	
}