/*
 * Copyright (c) 2010, Sony Ericsson Mobile Communication AB. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice, this 
 *      list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *    * Neither the name of the Sony Ericsson Mobile Communication AB nor the names
 *      of its contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.swampy.imageZoomView;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Listener for controlling zoom state through touch events
 */
public class LongPressZoomListener implements View.OnTouchListener {

	/**
     * Enum defining listener modes. Before the view is touched the listener is
     * in the UNDEFINED mode. Once touch starts it can enter either one of the
     * other two modes: If the user scrolls over the view the listener will
     * enter PAN mode, if the user lets his finger rest and makes a longpress
     * the listener will enter ZOOM mode.
     */
    private enum Mode {
        PAN, UNDEFINED, ZOOM
    }
    
	private static final String TAG = "LongPressZommListener";

    /** Time of tactile feedback vibration when entering zoom mode */
    private static final long VIBRATE_TIME = 50;

    /** Multitouch variables */
    
    public boolean hasMultitouch = false;

    //private float oldDist = 1f;
    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();

    /** X-coordinate of latest down event */
    private float mDownX;

    /** Y-coordinate of latest down event */
    private float mDownY;

    PointF mid = new PointF();

    /**
     * Runnable that enters zoom mode
     */
    private final Runnable mLongPressRunnable = new Runnable() {
        public void run() {
            mMode = Mode.ZOOM;
            mVibrator.vibrate(VIBRATE_TIME);
        }
    };

    /** Duration in ms before a press turns into a long press */
    private final int mLongPressTimeout;

    /** Current listener mode */
    private Mode mMode = Mode.UNDEFINED;

    /** Distance touch can wander before we think it's scrolling */
    private final int mScaledTouchSlop;
    
    /** Vibrator for tactile feedback */
    private final Vibrator mVibrator;
    
    /** X-coordinate of previously handled touch event */
    private float mX;
    /** Y-coordinate of previously handled touch event */
    private float mY;
    /** Zoom control to manipulate */
    private BasicZoomControl mZoomControl;
    Matrix savedMatrix = new Matrix();
    
    PointF start = new PointF();

    /**
     * Creates a new instance
     * 
     * @param context Application context
     */
    public LongPressZoomListener(Context context) {
        mLongPressTimeout = ViewConfiguration.getLongPressTimeout();
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mVibrator = (Vibrator)context.getSystemService("vibrator");        
    }

    // implements View.OnTouchListener
    public boolean onTouch(View v, MotionEvent event) {
        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();
        
        //dumpEvent(event);
        
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            	            	
            	v.postDelayed(mLongPressRunnable, mLongPressTimeout);
            	
                mDownX = x;
                mDownY = y;
                mX = x;
                mY = y;
                break;

            case MotionEvent.ACTION_MOVE: {
                final float dx = (x - mX) / v.getWidth();
                final float dy = (y - mY) / v.getHeight();

                if (mMode == Mode.ZOOM)                    	
                    mZoomControl.zoom((float)Math.pow(20, -dy), mDownX / v.getWidth(), mDownY / v.getHeight());
                   
                else if (mMode == Mode.PAN)
                    mZoomControl.pan(-dx, -dy);
                 
                else {
                    final float scrollX = mDownX - x;
                    final float scrollY = mDownY - y;

                    final float dist = (float)Math.sqrt(scrollX * scrollX + scrollY * scrollY);

                    if (dist >= mScaledTouchSlop) {
                        v.removeCallbacks(mLongPressRunnable);
                        mMode = Mode.PAN;
                    }
                }

                mX = x;
                mY = y;
                break;
            }           
            
            case MotionEvent.ACTION_POINTER_UP:
            	Log.d(TAG, "mode = undefined");
            	mMode = Mode.UNDEFINED;
                
                break;
                
            default:
                v.removeCallbacks(mLongPressRunnable);
                mMode = Mode.UNDEFINED;
                break;

        }

        return true;
    }

    /**
     * Sets the zoom control to manipulate
     * 
     * @param control Zoom control
     */
    public void setZoomControl(BasicZoomControl control) {
        mZoomControl = control;
    }  
   
}
