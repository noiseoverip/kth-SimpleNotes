package com.swampy.notes.adaptors;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.swampy.notes.entity.Note;
import com.swampy.notes.entity.NoteMedia;
import com.swampy.notes.utils.BitmapHelper;

/**
 * Adapter for displaying images in Gallery
 * 
 * @author Saulius Alisauskas
 *
 */
public class NoteGalleryAdapter extends BaseAdapter {
	private static final String TAG = "NoteGalleryAdapter";
	private Context mContext;	
	private List<NoteMedia> media;
	

	public NoteGalleryAdapter(Context context, Note note) {
		this.mContext = context;
		this.media = note.getMedia();	
		Log.d(TAG, "Adapter inited");
	}

	public int getCount() {
		int count=0;
		for (NoteMedia item : media){
			if (item.getType().equals(NoteMedia.Type.IMG.toString())){
				count++;
			}
		}
		Log.d(TAG, "Found images:"+count);
		return count;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG, "GetView position:"+position);
		ImageView imageView = new ImageView(mContext);
		
		try {		

			// imageView.setPadding(1, 1, 1, 1);
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			imageView.setLayoutParams(new Gallery.LayoutParams((int) (150 * BitmapHelper.mCcaling), (int) (150 *  BitmapHelper.mCcaling)));

			imageView.setImageBitmap((Bitmap)media.get(position).getData());			

		} catch (Exception e) {
			Log.e(TAG, "Some exception occured",e);
		}

		return imageView;
	}

}