/**
 * Copyright 2019 The Google Research Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gaze.eyetrackingtest.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

/**
 * A {@link SurfaceView} that can be adjusted to a specified aspect ratio.
 */
public class AutoFitSurfaceView extends SurfaceView {

    private static final String TAG = "AutoFitSurfaceView";

    private int ratioWidth = 0;
    private int ratioHeight = 0;

    public AutoFitSurfaceView(Context context) {
        this(context, null);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter,
     * that is,
     * calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        ratioWidth = width;
        ratioHeight = height;
        requestLayout();
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        float viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        int width, height;
        if (viewWidth > viewHeight) { // landscape
            width = (int) (MeasureSpec.getSize(widthMeasureSpec));
            height = (int) (0.8F * MeasureSpec.getSize(heightMeasureSpec));
        } else {
            width = (int) (0.8F * MeasureSpec.getSize(widthMeasureSpec));
            height = (int) (MeasureSpec.getSize(heightMeasureSpec));
        }
        if (0 == ratioWidth || 0 == ratioHeight) {
            Log.d(TAG, String.format("aspect ratio is 0 x 0 (uninitialized), setting measured" +
                    " dimension to: %d x %d", width, height));
            setMeasuredDimension(width, height);
        } else {
            if (width < height * ratioWidth / ratioHeight) {
                Log.d(TAG, String.format("setting measured dimension to %d x %d", width, height));
                setMeasuredDimension(width, width * ratioHeight / ratioWidth);
            } else {
                Log.d(TAG, String.format("setting measured dimension to %d x %d", width, height));
                setMeasuredDimension(height * ratioWidth / ratioHeight, height);
            }
        }
    }
}