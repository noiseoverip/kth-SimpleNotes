package com.swampy.notes.adaptors;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.swampy.notes.NotesAbstractActivity;
import com.swampy.notes.R;

/**
 * 
 * Adaptor for displaying notes as list items in list view
 * 
 * @author Saulius Alisauskas
 *
 */
public class NotesListAdaptor extends BaseAdapter implements SectionIndexer {
	
	class NoteViewHolder {
		TextView date;
		TextView dueDate;
		TextView dueLabel;
		TextView text;
		TextView title;
	}
	
	public AlphabetIndexer alphaIndexer; // used for indexing by letters
	
	private SimpleDateFormat dateTimeFormat;

	private LayoutInflater mInflater;

	private Cursor notesCursor;
	
	private final String TAG = this.getClass().getSimpleName();

	public NotesListAdaptor(Activity a, Cursor cursor) {
		this.notesCursor = cursor;
		mInflater = LayoutInflater.from(a);
		notesCursor =  ((NotesAbstractActivity)a).getDao().getNotesCursor();

		alphaIndexer = new AlphabetIndexer(notesCursor, 1, " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		
		dateTimeFormat = new SimpleDateFormat("yy-MM-dd");
	}
	
	@Override
	public int getCount() {
		return (notesCursor != null) ? notesCursor.getCount() : 0;
	}	
	
	@Override
	public Object getItem(int position) {
		return position;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	/**
	 * 
	 * Get item id at provided position
	 * 
	 * @param position
	 * @return
	 */
	public int getItemIdAtPosition(int position) {
		notesCursor.moveToPosition(position);
		return notesCursor.getInt(0);
	}
	
	@Override
	public int getPositionForSection(int section) {
		return alphaIndexer.getPositionForSection(section);

	}

	// SectionIndexer methods
	@Override
	public int getSectionForPosition(int position) {

		return alphaIndexer.getSectionForPosition(position);

	}

	@Override
	public Object[] getSections() {

		return alphaIndexer.getSections();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		NoteViewHolder holder;

		if (convertView == null) {

			// Inflate view
			convertView = mInflater.inflate(R.layout.fragment_note_list_item_layout, null);

			// Instantiate holder class
			holder = new NoteViewHolder();

			// Bind views
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.date = (TextView) convertView.findViewById(R.id.date);			
			holder.dueDate = (TextView) convertView.findViewById(R.id.dueDate);
			holder.text = (TextView) convertView.findViewById(R.id.text);

			// Set holder as view tag
			convertView.setTag(holder);
		} else
			holder = (NoteViewHolder) convertView.getTag();

		try {

			if (notesCursor.moveToPosition(position)) {

				// Set title
				holder.title.setText(notesCursor.getString(1));
				holder.text.setText(notesCursor.getString(2));
				holder.date.setText(dateTimeFormat.format(new Date(notesCursor.getLong(3))));

				long dueTime = notesCursor.getLong(4);
				if (dueTime > 0) {					
					holder.dueDate.setVisibility(View.VISIBLE);
					holder.dueDate.setText(dateTimeFormat.format(new Date(dueTime)));
				} else {					
					holder.dueDate.setVisibility(View.GONE);
				}

			}

		} catch (Exception e) {
			Log.d(TAG, "ERROR in getView(): " + e.getMessage());
		}
		return convertView;
	}
	
	/**
	 * Set cursor
	 * 
	 * @param cursor
	 */
	public void setCursor(Cursor cursor) {
		this.notesCursor = cursor;
		this.notifyDataSetChanged();
	}

}
