package com.swampy.notes;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.swampy.notes.dao.NotesDao;

/**
 * Abstract class for app activities for common functionality
 * 
 * @author Saulius Alisauskas
 *
 */
public abstract class NotesAbstractActivity extends SherlockFragmentActivity implements NotesDao.DaoProvider {
	
	private NotesDao dao;	

	@Override
	public synchronized NotesDao getDao() {
		if (null == dao) {
			dao = new NotesDao();
			dao.init(this);
		}
		return dao;
	}
	
	@Override
	protected void onStop() {		
		super.onStop();
		if (null != dao) {
			dao.close();
			dao = null;
		}
	}
}
