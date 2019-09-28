package com.example.jobber;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.List;

class JobHorizontalScrollView extends HorizontalScrollView {

    private GestureDetector gestureDetector;
    private int activePhoto = 0;
    private int nPhotos = 0;
    static final int SWIPE_MIN_DISTANCE = 5;
    static final int SWIPE_THRESHOLD_VELOCITY = 300;

    Point displaySize;

    public JobHorizontalScrollView(Context context, AttributeSet attr, int defStyle)
    {super(context, attr, defStyle); setup();}
    public JobHorizontalScrollView(Context context, AttributeSet attr)
    {super(context, attr); setup();}
    public JobHorizontalScrollView(Context context)
    {super(context); setup();}

    void setup()
    {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();

        displaySize = new Point();
        d.getSize(displaySize);

        setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if( gestureDetector.onTouchEvent(event) )
                {
                    return true;
                }
                else if( event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL )
                {
                    int scrollX = getScrollX();
                    int photoWidth = v.getMeasuredWidth();
                    activePhoto = ( (scrollX + (photoWidth/2)) / photoWidth );
                    smoothScrollTo( activePhoto*photoWidth, 0 );
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });

        gestureDetector = new GestureDetector( getContext(), new MyGestureDetector() );
    }

    private void setBitmap(ImageView imageView, String imageFile )
    {
        int tWidth = displaySize.x;
        int tHeight = displaySize.y;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile( imageFile, bmOptions );

        int sWidth = bmOptions.outWidth;
        int sHeight = bmOptions.outHeight;

        int scaleFactor = Math.min( sWidth/tWidth, sHeight/tHeight );

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile( imageFile, bmOptions );
        imageView.setImageBitmap(bitmap);
    }

    public void addPhoto(String filePath)
    {
        LinearLayout internalWrapper = (LinearLayout) this.getChildAt(0);

        ImageView i = new ImageView(getContext());


        //i.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        //i.setScaleType(ImageView.ScaleType.FIT_CENTER);

        //setBitmap(i, filePath);

        internalWrapper.addView( i, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT) );

        Bitmap image_bmp= BitmapFactory.decodeFile(filePath);
        i.setImageBitmap(image_bmp);
        i.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        //internalWrapper.addView( i, new LinearLayout.LayoutParams(displaySize.x, displaySize.y) );
        fullScroll(FOCUS_RIGHT);

        nPhotos += 1;
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velX, float velY)
        {
            try{
                if( e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs( velX ) > SWIPE_THRESHOLD_VELOCITY )
                {
                    int photoWidth = getMeasuredWidth();
                    if(activePhoto < (nPhotos -1))
                    {
                        activePhoto = activePhoto +1;
                        smoothScrollTo( activePhoto*photoWidth, 0 );
                    }
                    else
                    {
                        activePhoto = nPhotos -1;
                        smoothScrollTo( activePhoto*photoWidth, 0 );
                        //mainAct.photo(IMAGE_RIGHT);
                    }
                    return true;
                }
                else if( e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs( velX ) > SWIPE_THRESHOLD_VELOCITY )
                {
                    int photoWidth = getMeasuredWidth();
                    if(activePhoto > 0)
                    {
                        activePhoto = activePhoto - 1;
                        smoothScrollTo( activePhoto*photoWidth, 0 );
                    }
                    else
                    {
                        activePhoto = 0;
                        smoothScrollTo( activePhoto*photoWidth, 0 );
                        //mainAct.photo(IMAGE_LEFT);
                    }
                    return true;
                }
            } catch (Exception e){
                Log.e("Fling", e.toString());
            }
            return false;
        }
    }
}
