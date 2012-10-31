package com.swampy.notes.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

/**
 * Alert dialog with two buttons and text input field
 * 
 * @author Saulius Alisauskas
 *
 */
public class OneEntryDialog {
	
	/**
	 * Callback interface
	 * 
	 * @author Saulius Alisauskas
	 * 
	 */
	public interface Callback {
		/**
		 * Negative button has been selected
		 * 
		 * @param entryText
		 */
		public void selectedNegative(DialogInterface dialog, String entryText);

		/**
		 * Positive button has been selected
		 * 
		 * @param entryText
		 */
		public void selectedPositive(DialogInterface dialog, String entryText);
	}

	private Callback mCallback;
	private Context mContext;

	private EditText mEditText;
	
	/**
	 * Main contructor
	 * 
	 * @param context
	 * @param callback
	 */
	public OneEntryDialog(Context context, Callback callback) {
		this.mContext = context;
		this.mCallback = callback;
	}
	
	/**
	 * Create dialog with specified object titles
	 * 
	 * @param title
	 * @param positiveName - positive button title
	 * @param negativeName - negative button title
	 * @return
	 */
	public AlertDialog create(String title, String positiveName, String negativeName) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(title);
		builder.setPositiveButton(positiveName, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mCallback.selectedPositive(dialog, mEditText.getText().toString());
			}
		});

		builder.setNegativeButton(negativeName, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mCallback.selectedNegative(dialog, mEditText.getText().toString());
			}
		});

		mEditText = new EditText(mContext);
		builder.setView(mEditText);

		return builder.create();
	}

}
