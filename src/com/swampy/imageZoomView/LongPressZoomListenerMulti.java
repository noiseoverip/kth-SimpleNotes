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
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.swampy.imageZoomView.BasicZoomControl.HitLimit;

/**
 * Listener for controlling zoom state through touch events
 */
public class LongPressZoomListenerMulti implements View.OnTouchListener {

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

    /** Time of tactile feedback vibration when entering zoom mode */
    private static final long VIBRATE_TIME = 50;

    /** Multitouch variables */
    
    public boolean hasMultitouch = true;

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
    private float oldDist = 1f;
    Matrix savedMatrix = new Matrix();
    
    PointF start = new PointF();

    /**
     * Creates a new instance
     * 
     * @param context Application context
     */
    public LongPressZoomListenerMulti(Context context) {
        mLongPressTimeout = ViewConfiguration.getLongPressTimeout();
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mVibrator = (Vibrator)context.getSystemService("vibrator");        
    }

    /** Show an event in the LogCat view, for debugging */
    
    private void dumpEvent(MotionEvent event) {
        // ...
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
            "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }
        sb.append("[");
        
        
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
            sb.append(";");
        }
        sb.append("]");
        Log.d("Zoom", sb.toString());
    }

    // implements View.OnTouchListener
    
    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        // ...
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
    
    public boolean onTouch(View v, MotionEvent event) {
        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();
        BasicZoomControl.HitLimit hitLimit;
        
        dumpEvent(event);       
        
//        v.getParent().requestDisallowInterceptTouchEvent(true);
        
        switch (action & MotionEvent.ACTION_MASK) {
            
        	case MotionEvent.ACTION_DOWN:
            	
            	if (!hasMultitouch)
            		v.postDelayed(mLongPressRunnable, mLongPressTimeout);
            	
                mDownX = x;
                mDownY = y;
                mX = x;
                mY = y;
                
               //float zoomState = mZoomControl.getZoomState().getZoom();
               //Static.d("Aspect:"+zoomState);            
               
               break;
                

            case MotionEvent.ACTION_MOVE:
                
            	final float dx = (x - mX) / v.getWidth();
                final float dy = (y - mY) / v.getHeight();
                
                //Do not parent to intercept this event (HorizontalScrollView)
                v.getParent().requestDisallowInterceptTouchEvent(true);
                
                if (mMode == Mode.ZOOM) {               	                	
                	
                	if (hasMultitouch){               	
                	
	                	float newDist = spacing(event);
	                    //Log.d(Static.TAG, "newDist=" + newDist);
	                    if (newDist > 10f) {           	  
	                       
	                    	mZoomControl.zoom((float)Math.pow(20, -(oldDist - newDist) / v.getHeight() ), mDownX / v.getWidth(), mDownY / v.getHeight());
	                        
	                        oldDist = newDist;
	                    }
	                    
	                }
                    else{                    	
                    	
                    	mZoomControl.zoom((float)Math.pow(20, -dy), mDownX / v.getWidth(), mDownY / v.getHeight());
                    	
                    }
                	
                	mX = x;
                    mY = y;
                    
                    return true;
                    
                    
                } else if (mMode == Mode.PAN) {
                	
                    hitLimit = mZoomControl.pan(-dx, -dy);                   
                    
                    if (hitLimit == HitLimit.PAN_RIGHT || hitLimit == HitLimit.PAN_LEFT){
                    	//Static.d("Hit:"+hitLimit);
                    	v.getParent().requestDisallowInterceptTouchEvent(false);                    	
                    	return false;
                    }
                    else {
                    	v.getParent().requestDisallowInterceptTouchEvent(true);
                    	mX = x;
                        mY = y;
                        return true;
                    }                  
                    
                } 
                
                else {
                	
                	//v.getParent().requestDisallowInterceptTouchEvent(false);        
                	
                    final float scrollX = mDownX - x;
                    final float scrollY = mDownY - y;

                    final float dist = (float)Math.sqrt(scrollX * scrollX + scrollY * scrollY);

                    if (dist >= mScaledTouchSlop) {
                        v.removeCallbacks(mLongPressRunnable);
                        mMode = Mode.PAN;
                    }
                    
                    return false;                   
                }
            
                
            
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mMode = Mode.ZOOM;                    
                }
                return true;
            
            case MotionEvent.ACTION_POINTER_UP:
            	
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
    
    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {
        // ...    	
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
    
}
