package com.swampy.notes.utils;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.FrameLayout;

//TODO: should be some way to specify color from XML
/**
 * Checkable frame layout
 * 
 * @author Saulius Alisauskas
 *
 */
public class CheckableFrameLayout extends FrameLayout implements Checkable {
    private boolean mChecked;

    public CheckableFrameLayout(Context context) {
        super(context);
    }

    public CheckableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        setBackgroundDrawable(checked ? new ColorDrawable(0xffC7C7C7) : null);
    }

    public void toggle() {
        setChecked(!mChecked);
    }
}
