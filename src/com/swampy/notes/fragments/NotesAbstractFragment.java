package com.swampy.notes.fragments;

import com.actionbarsherlock.app.SherlockFragment;
import com.swampy.notes.dao.NotesDao;
import com.swampy.notes.dao.NotesDao.DaoProvider;

/**
 * Abstract fragment to be implemented by all fragments
 * 
 * @author Saulius Alisauskas
 *
 */
public abstract class NotesAbstractFragment extends SherlockFragment {	
	
	/**
	 * Get data object provider
	 * 
	 * @return
	 */
	public NotesDao getDao() {
		return ((DaoProvider) getActivity()).getDao();
	}
}
