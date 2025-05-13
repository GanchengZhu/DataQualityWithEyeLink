/*******************************************************************************
 * Copyright (C) 2023 Gancheng Zhu
 * Email: psycho@zju.edu.cn
 ******************************************************************************/

package org.gaze.eyetrackingtest.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import org.gaze.eyetrackingtest.R;

public class BitmapSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable  {

    Bitmap bitmap;
    boolean isDrawing;
    SurfaceHolder surfaceHolder;
    boolean drawBitmap;
    private int resourceId;

    public BitmapSurfaceView(Context context) {
        this(context, null);
    }

    public BitmapSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BitmapSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BitmapSurfaceView(Context context, AttributeSet attrs, int defStyleAttr,
                             int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    public void setBitmapResource(int resourceId){
        this.resourceId = resourceId;
        bitmap = BitmapFactory.decodeResource(this.getResources(), resourceId);
    }

    @Override public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        isDrawing = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        isDrawing = false;
    }

    @Override public void run() {
        while (isDrawing) {
            drawUI();
        }
    }

    private void drawUI() {
        Canvas mCanvas = null;
        try {
            mCanvas = surfaceHolder.lockCanvas();
            if (mCanvas != null) {
                // Draw Background
//                Log.i("SurfaceView", "w * h = " + mCanvas.getWidth() + " * " + mCanvas.getHeight());
                mCanvas.drawRGB(255, 255, 255);
                if (bitmap == null) return;
                mCanvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                        new Rect(0, 0, mCanvas.getWidth(), mCanvas.getHeight()), null);
//                drawBitmap = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (surfaceHolder != null && mCanvas != null) {
                    surfaceHolder.unlockCanvasAndPost(mCanvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getResourceId() {
        return resourceId;
    }
}
