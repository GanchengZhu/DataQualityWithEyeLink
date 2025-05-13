/*******************************************************************************
 * Copyright (C) 2023 Gancheng Zhu
 * Email: psycho@zju.edu.cn
 ******************************************************************************/

package org.gaze.eyetrackingtest.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.gaze.tracker.R;
import org.gaze.tracker.bean.GazeSample;
import org.gaze.tracker.core.GazeTracker;
import org.gaze.tracker.enumeration.TrackingState;
import org.gaze.tracker.listener.GazeCallback;

public class ExampleActivity extends BaseActivity implements GazeCallback, Runnable, SurfaceHolder.Callback {
    SurfaceView surfaceView;
    private int mViewWidth;
    private int mViewHeight;
    private GazeTracker gazeTracker;
    private boolean isDraw;
    private float x;
    private float y;
    private SurfaceHolder holder;
    private Paint gazePaint;
    private Canvas mCanvas;
    private boolean isPointShow = false;

    @Override
    public int getLayoutId() {
        return R.layout.activity_example;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surfaceView = findViewById(R.id.example_surface_view);
        holder = surfaceView.getHolder();
        holder.addCallback(this);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int stroke = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
        gazePaint = new Paint();
        gazePaint.setAntiAlias(true);
        gazePaint.setDither(true);
        gazePaint.setColor(Color.GREEN);
        gazePaint.setStrokeWidth(stroke);
        gazePaint.setStyle(Paint.Style.STROKE);

        gazeTracker = GazeTracker.getInstance();
        gazeTracker.addCallbacks(this);

        surfaceView.post(() -> {
            mViewWidth = surfaceView.getWidth();
            mViewHeight = surfaceView.getHeight();

            gazeTracker.startSampling();
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {

        }
    }

    @Override
    public void onGaze(GazeSample gazeSample) {
//        Log.i(TAG, "gazeSample.getTrackingState(): " + gazeSample.getTrackingState());
        if (gazeSample.getTrackingState() == TrackingState.SUCCESS) {
            isPointShow = true;
            x = gazeSample.getFilteredX();
            y = gazeSample.getFilteredY();
//            Log.i(TAG, "Calibrated: " + gazeSample.isHasCalibrated());
//            Log.i(TAG, String.format("x: %.2f, y: %.2f", x, y));
        } else {
            isPointShow = false;
        }
    }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        new Thread(this).start();
        isDraw = true;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void run() {
        while (isDraw) {
            try {
                assert holder != null;
                mCanvas = holder.lockCanvas();
//            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                if (mCanvas != null) {
                    mCanvas.drawRGB(255, 255, 255);
                    mCanvas.drawCircle(x, y, 10, gazePaint);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (holder != null) {
                        holder.unlockCanvasAndPost(mCanvas);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gazeTracker.removeCallbacks(this);
        gazeTracker.destroy();
    }
}
