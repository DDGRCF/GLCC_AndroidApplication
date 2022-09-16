package com.glcc.client;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

public class VideoShowRelativeLayout extends RelativeLayout {
    private double layoutRatio = 4 / 3.;

    public static final int LANDSCAPE = 0;
    public static final int PORTRAIT = 1;

    private int rotateMode = PORTRAIT;

    public VideoShowRelativeLayout(Context context) {
        super(context);
    }

    public VideoShowRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoShowRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VideoShowRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void setRotateMode(int rotateMode) {
        this.rotateMode = rotateMode;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (rotateMode == PORTRAIT) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            if (layoutRatio > 0) {
                double height = width / layoutRatio;
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) height, MeasureSpec.EXACTLY);
            }
        } else if (rotateMode == LANDSCAPE) {
            int height = MeasureSpec.getSize(heightMeasureSpec);
            if (layoutRatio > 0) {
                double width = height * layoutRatio;
                widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) width, MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
