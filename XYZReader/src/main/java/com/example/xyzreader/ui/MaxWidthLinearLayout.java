/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.xyzreader.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.example.xyzreader.R;

/**
 * A simple {@link LinearLayout} subclass that has a maxWidth
 */
public class MaxWidthLinearLayout extends LinearLayout {

    private static final int[] ATTRS = {
        android.R.attr.maxWidth
    };
    private static final int MAX_WIDTH_IDX = 0;

    private int mMaxWidth;
    private int mMatchParent;
    private int mWrapContent;

    public MaxWidthLinearLayout(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public MaxWidthLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public MaxWidthLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaxWidthLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);
        mMatchParent = context.getResources().getDimensionPixelSize(R.dimen.app_match_parent);
        mWrapContent = context.getResources().getDimensionPixelSize(R.dimen.app_wrap_content);
        mMaxWidth = a.getDimensionPixelSize(MAX_WIDTH_IDX, mWrapContent);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int newSpecWidth = widthMeasureSpec;
        if ((mMaxWidth != mMatchParent) && (mMaxWidth != mWrapContent)) {
            newSpecWidth = Math.min(MeasureSpec.getSize(widthMeasureSpec), mMaxWidth);
            newSpecWidth = MeasureSpec.makeMeasureSpec(newSpecWidth, MeasureSpec.getMode(widthMeasureSpec));
        }
        super.onMeasure(newSpecWidth, heightMeasureSpec);
    }
}