package com.swampy.notes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.swampy.notes.fragments.NoteViewFragment;

/**
 * 
 * 
 * @author Saulius Alisauskas
 * 
 */
public class NoteActivity extends NotesAbstractActivity  implements NoteViewFragment.Callback {

	private final String TAG = this.getClass().getSimpleName();	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock_Light);
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");	

		// Set action bar settings
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(true);

		// Set menu as navigation and ad OnNavigationListener
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		if (savedInstanceState == null) {
			// During initial setup, plug in the details fragment.
			NoteViewFragment fragment = new NoteViewFragment();
			fragment.setArguments(getIntent().getExtras());
			fragment.setCallback(this);
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
		}

	}

	@Override
	public void onNoteDeleted() {
		this.finish();

	}

	@Override
	public void onNoteEditFinished() {
		// do nothing here
	}

	/**
	 * Handle taps on option items
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case android.R.id.home:
			// App icon in action bar clicked go home
			Intent intent = new Intent(this, NoteListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause()");
		super.onPause();		
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onStart()");
		super.onResume();
	}
}
