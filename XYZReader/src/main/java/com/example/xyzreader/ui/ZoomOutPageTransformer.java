package com.example.xyzreader.ui;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.View;

import static com.example.xyzreader.util.Utils.TRANSPARENT;

/**
 * Based on <a href="https://developer.android.com/training/animation/screen-slide.html#pagetransformer">Zoom-out page transformer</a>
 */

public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.85f;
    private static final float MIN_ALPHA = 0.5f;

    public void transformPage(@NonNull View view, float position) {
        float alpha = TRANSPARENT;

        // [-Infinity,-1) page is way off-screen to the left.
        // (1,+Infinity] page is way off-screen to the right.

        if ((position >= -1) && (position <= 1)) { // [-1,1]
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            // Modify the default slide transition to shrink the page as well
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
            if (position < 0) {
                view.setTranslationX(horzMargin - vertMargin / 2);
            } else {
                view.setTranslationX(-horzMargin + vertMargin / 2);
            }

            // Scale the page down (between MIN_SCALE and 1)
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

            // Fade the page relative to its size.
            alpha = MIN_ALPHA + (scaleFactor - MIN_SCALE) /
                            (1 - MIN_SCALE) * (1 - MIN_ALPHA);

        }
        view.setAlpha(alpha);
    }
}
