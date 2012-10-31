package com.swampy.notes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.swampy.notes.R;

/**
 * Empty fragment to show when no "Details" view has been selected
 * 
 * @author Saulius Alisauskas
 * 
 */
public class NoteViewFragmentEmpty extends SherlockFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_note_layout_empty, container, false);
	}
}
