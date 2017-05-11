package com.example.ngfngf.myplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

/**
 * Created by ngfngf on 2017/4/9.
 */

public class MyVideoView extends VideoView {
    private int defualtWidth = 1920;
    private int defualtHeight = 1080;

    public MyVideoView(Context context) {
        super(context);
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getDefaultSize(defualtWidth, widthMeasureSpec);
        int height = getDefaultSize(defualtHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
