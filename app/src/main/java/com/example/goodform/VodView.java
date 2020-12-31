package com.example.goodform;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class VodView extends VideoView {
    // this is a custom version of videoview which simply adds a sexFixedVideoSize method
    // to the class so we can resize it

    public VodView(Context context) {
        super(context);
    }

    public VodView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VodView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    /**
     * Resize video view by using SurfaceHolder.setFixedSize(...). See {@link android.view.SurfaceHolder#setFixedSize}
     * @param width
     * @param height
     * testing branch push
     */
    public void setFixedVideoSize(int width, int height)
    {
        getHolder().setFixedSize(width, height);
    }
}