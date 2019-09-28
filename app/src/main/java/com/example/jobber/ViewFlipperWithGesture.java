package com.example.jobber;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ViewFlipper;

public class ViewFlipperWithGesture extends ViewFlipper {
    private float x1,x2;
    private static final int MIN_DISTANCE = 100;

    public ViewFlipperWithGesture(Context context) {
        super(context);
    }

    public ViewFlipperWithGesture(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            x1 = event.getX();
            Log.d("action DOWN", " down " + x1);
        }
        if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            x2 = event.getX();
            Log.d("action UP", " up " + x2);
            float deltaX = x2 - x1;
            Log.d("action TOTAL", " total " + deltaX);
            if (Math.abs(deltaX) > MIN_DISTANCE) {
                showNext();
                return true;
            } else {
                return false;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        return onTouchEvent(ev);
        //return false;
    }
}
