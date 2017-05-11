package com.example.ngfngf.myplayer.util;

import android.content.Context;

/**
 * Created by ngfngf on 2017/4/8.
 */

public class DimensionTransition {
    public static int dip2px(Context context, float dpVule) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpVule*scale+0.5f);
    }
    public static int px2dip(Context context, float dpVule) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpVule/scale+0.5f);
    }
}
