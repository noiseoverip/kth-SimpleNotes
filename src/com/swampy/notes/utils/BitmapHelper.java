package com.swampy.notes.utils;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Utility class for operations with bitmaps
 * 
 * @author Saulius Alisauskas
 *
 */
public class BitmapHelper {
	
	public static float mCcaling;
	
	private static final String TAG = "BitmapHelper";	
	
	/**
	 * Scale and chop the bitmap
	 * 
	 * @param bitmap
	 * @param dst_width
	 * @param dst_height
	 * @return
	 */
	public static Bitmap decodeBitmap(Bitmap bitmap, int dst_width, int dst_height){
    	
    	if (bitmap == null)
    		return null;
    	
    	//fix destination width and height according to screen scaling
    	dst_width *= mCcaling;
    	dst_height *= mCcaling;
    	
    	try {
        	
    		//Find the correct scale value. It should be the power of 2.
            int src_width=bitmap.getWidth(), src_height=bitmap.getHeight();           
            
            if (src_width == dst_width && src_height == dst_height){
            	Log.d(TAG, "imageLoader::decodeBitmap:: image already of right size");
            	return bitmap;
            }
            	
            Log.d(TAG, "Resizing bitmap w:"+src_width + " h:"+src_height+" to w:"+dst_width + " h:"+dst_height);
            
            float srcRatio = (float) src_width / src_height;
            float dstRatio = (float) dst_width / dst_height;        
            
            //Calculate new scale: use coordinate which is longer then 
			float scale = (srcRatio <= dstRatio) ? (float) src_width / dst_width : (float) src_height / dst_height; 	
			Log.d(TAG, "Scale: "+scale+" new width:"+(int)(src_width / scale)+" new height:"+(int)(src_height / scale));
			
            /**SCALING*/
            Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, (int)Math.round((src_width / scale)), (int)Math.round(src_height / scale), true);
            bitmap.recycle();
            
            //Static.d("Resized bitmap1: "+f.getPath() +" to w:"+bitmap1.getWidth() + " h:"+bitmap1.getHeight() + " dx:"+dX+" dY:"+dY);
            
            /**CROPPING*/
            
            if (bitmap1.getWidth() == dst_width && bitmap1.getHeight() == dst_height)
            	return bitmap1;
            
            float dX = ((src_width / scale)  - dst_width) / 2; 	//x to move while cropping
			float dY = ((src_height / scale) - dst_height) / 2;	//y to move while cropping
			
            Bitmap bitmap2 = Bitmap.createBitmap(bitmap1, (int)dX, (int)dY, dst_width, dst_height);
            bitmap1.recycle();           
            
            Log.d(TAG, "Resized bitmap2 to w:"+bitmap2.getWidth() + " h:"+bitmap2.getHeight() + " scale:"+(int)scale);                      
            
            return bitmap2;
            
        } catch (Exception e) {Log.d(TAG, "decodeImages::"+e.toString());}
        
        return null;
    }
}
