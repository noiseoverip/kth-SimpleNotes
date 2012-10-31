package com.swampy.notes;


import java.io.ByteArrayInputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.swampy.imageZoomView.BasicZoomControl;
import com.swampy.imageZoomView.ImageZoomView;
import com.swampy.imageZoomView.LongPressZoomListenerMulti;
import com.swampy.swipeview.PageControl;
import com.swampy.swipeview.SwipeView;
import com.swampy.swipeview.SwipeView.OnPageChangedListener;

/**
 * Image gallery activity with zoom controls
 * 
 * @author Saulius Alisauskas
 *
 */
public class ImageZoomActivity extends NotesAbstractActivity {

	/**
	 * Class for loading views while swiping
	 * 
	 * @author Saulius Alisauskas
	 * 
	 */
	private class SwipeImageLoader implements OnPageChangedListener {
		private Context mContext;

		public SwipeImageLoader(Context mContext) {
			this.mContext = mContext;
		}

		private void addView(int index) {
			ImageZoomView imageView = new ImageZoomView(mContext, null);
			BasicZoomControl mZoomControl;
			View.OnTouchListener mZoomListener;
			mZoomControl = new BasicZoomControl();			
			
			mZoomListener = new LongPressZoomListenerMulti(getApplicationContext());
			((LongPressZoomListenerMulti) mZoomListener).setZoomControl(mZoomControl);			

			imageView.setZoomState(mZoomControl.getZoomState());
			mCursor.moveToPosition(index);
			byte[] imageByteArray = mCursor.getBlob(1);
			if (imageByteArray != null) {
				// Get image from database
				ByteArrayInputStream imageStream = new ByteArrayInputStream(imageByteArray);
				Bitmap bmp = BitmapFactory.decodeStream(imageStream);

				imageView.setImage(bmp);

				imageView.setOnTouchListener(mZoomListener);

				mZoomControl.setAspectQuotient(imageView.getAspectQuotient());

				resetZoomState(mZoomControl);

				((ViewGroup) mSwipeView.getChildContainer().getChildAt(index)).addView(imageView);
			}
		}

		public void onPageChanged(int oldPage, int newPage) {
			Log.d(TAG, "SwipeImageLoader::onPageChanges old:" + oldPage + " new:" + newPage);

			if (newPage > oldPage) { // going forwards			
				if (newPage != (mSwipeView.getPageCount() - 1)) { // if at the end, don't load one page after the end
					this.addView(newPage + 1);
				}

				if (oldPage != 0) {// if at the beginning, don't destroy one before the beginning				
					ViewGroup child = ((ViewGroup) mSwipeView.getChildContainer().getChildAt(oldPage - 1));
					if (child != null)
						child.removeAllViews();
				}

			} else { // going backwards			
				if (newPage != 0) {// if at the beginning, don't load one before the beginning				
					this.addView(newPage - 1);
				}

				if (oldPage != (mSwipeView.getPageCount() - 1)) {// if at the end, don't destroy one page after the end				
					ViewGroup child = ((ViewGroup) mSwipeView.getChildContainer().getChildAt(oldPage + 1));
					if (child != null)
						child.removeAllViews();
				}
			}

		}

		/**
		 * Reset zoom state and notify observers
		 */
		private void resetZoomState(BasicZoomControl mZoomControl) {
			mZoomControl.getZoomState().setPanX(0.5f);
			mZoomControl.getZoomState().setPanY(0.5f);
			mZoomControl.getZoomState().setZoom(1f);
			mZoomControl.getZoomState().notifyObservers();
		}

	}

	private Cursor mCursor;	
	private SwipeView mSwipeView;
	private final String TAG = this.getClass().getSimpleName();

	@Override
	public void onBackPressed() {
		this.finish();
		// Show animation on when closing		
		this.overridePendingTransition(R.anim.zoom_enter_back, R.anim.zoom_exit_back);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock_Light);
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		setContentView(R.layout.image_zoom_view);

		// Bind views
		mSwipeView = (SwipeView) findViewById(R.id.swipe_view);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Log.d(TAG, "Note id not provided");
			this.finish();
		}

		int noteId = extras.getInt("noteId");		
		
		mCursor = getDao().getNoteImagesCursor(noteId);
		int viewsNum = mCursor.getCount();

		for (int i = 0; i < viewsNum; i++) {
			FrameLayout frame = new FrameLayout(this);
			mSwipeView.addView(frame);
		}

		SwipeImageLoader mSwipeImageLoader = new SwipeImageLoader(this);
		mSwipeImageLoader.addView(0); // Should be atleast one view
		// Setup swiping through pages only if > 1 views to show
		if (viewsNum > 1) {
			mSwipeImageLoader.addView(1);
			mSwipeView.setOnPageChangedListener(mSwipeImageLoader);
			mSwipeView.setPageControl((PageControl) findViewById(R.id.page_control));
		}

	}	

	@Override
	protected void onPause() {		
		super.onPause();		
		Log.d(TAG, "onPause()");
		
	}
	
	@Override
	protected void onResume() {		
		super.onResume();
		Log.d(TAG, "onResume()");		
	}

}
