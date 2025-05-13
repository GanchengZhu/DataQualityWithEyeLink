/*******************************************************************************
 * Copyright (C) 2023 Gancheng Zhu
 * Email: psycho@zju.edu.cn
 ******************************************************************************/

package org.gaze.eyetrackingtest.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.kongzue.dialogx.dialogs.WaitDialog;

import org.gaze.eyetrackingtest.ui.CalibrationActivity;
import org.gaze.eyetrackingtest.ui.CalibrationFragment;
import org.gaze.tracker.R;
import org.gaze.tracker.bean.GazeSample;
import org.gaze.tracker.core.GazeTracker;
import org.gaze.tracker.enumeration.TrackingState;
import org.gaze.tracker.helper.DimensionUtils;
import org.gaze.tracker.helper.IOUtils;
import org.gaze.tracker.listener.CalibrationCallback;
import org.gaze.tracker.listener.GazeCallback;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class CalibrationSurfaceView extends SurfaceView
        implements Runnable, CalibrationCallback, CalibrationDialogue.OnClickListener,
        GazeCallback, /*FaceCallback,*/
        SurfaceHolder.Callback {
    private final String TAG = getClass().getSimpleName();
    private final StringBuffer validationStringBuffer = new StringBuffer();
    protected float calibrationPhysicalMarkerSize = 0.7f; // Constant 半径是0.5厘米
    protected float validationPhysicalMarkerSize = 1.0f;
    private Canvas mCanvas;
    private SurfaceHolder surfaceHolder;
    private volatile boolean isDraw = true;// 控制绘制的开关
    private float x, y = 0;
    private Bitmap targetBitmap;
    private int targetWidth, targetHeight;
    private GazeTracker gazeTracker;
    private boolean calibrationFinished = true;
    private Handler threadHandler;
    private CalibrationDialogue calibrationDialogue;
    private WaitDialog waitDialogue;
    private GazeSample gazeSample;
    private boolean validationFinished = true;
    private RandomPointMotion randomPointMotion;
    private int mViewWidth, mViewHeight;
    private boolean validating = false;
    private Paint gazePaint;
    private Paint textPaint;
    private boolean errorBarVisible;
    private Map<Integer, List<Float>> validationErrorMap;
    private String validationSaveDir;
    private String validationSaveFile;
    private WeakReference<CalibrationFragment> calibrationFragment;

    private float xDpi, yDpi;

    public CalibrationSurfaceView(Context context) {
        this(context, null);
    }

    public CalibrationSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalibrationSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }


    public CalibrationSurfaceView(Context context, AttributeSet attrs, int defStyleAttr,
                                  int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        DisplayMetrics dm = new DisplayMetrics();
        ((CalibrationActivity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        xDpi = dm.xdpi;
        yDpi = dm.ydpi;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        targetBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.target);
        int size = cm2px(calibrationPhysicalMarkerSize);
        targetBitmap = scaleBitmap(targetBitmap, size, size);
        targetWidth = targetBitmap.getWidth();
        targetHeight = targetBitmap.getHeight();
        gazeTracker = GazeTracker.getInstance();
        gazeTracker.addCallbacks(this);
        errorBarVisible = gazeTracker.isErrorBarVisible();
        threadHandler = new Handler(Looper.getMainLooper());
        waitDialogue = WaitDialog.getInstance();
        waitDialogue.setMessageContent("正在校准...");
        calibrationDialogue = new CalibrationDialogue();
        calibrationDialogue.setOnClickListener(this).show();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int stroke = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
        gazePaint = new Paint();
        gazePaint.setAntiAlias(true);
        gazePaint.setDither(true);
        gazePaint.setColor(Color.GREEN);
        gazePaint.setStrokeWidth(stroke);
        gazePaint.setStyle(Paint.Style.STROKE);

        int strokeText = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics);
        textPaint = new Paint();
        textPaint.setTextSize(16);
        textPaint.setAntiAlias(true); //抗锯齿
        textPaint.setDither(true);//防抖动
        textPaint.setStrokeWidth(strokeText);//设置线条宽度（单位px）
        textPaint.setColor(Color.WHITE);

        validationSaveDir = getContext().getExternalFilesDir("").getAbsolutePath();
        validationSaveDir += "/validation";
        IOUtils.createOrExistsDir(validationSaveDir);
        Log.d(TAG, validationSaveDir);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


        mViewWidth = getMeasuredWidth();
        mViewHeight = getMeasuredHeight();

        Log.d(TAG, "onMeasure ViewWidth * ViewHeight = " + mViewWidth + " * " + mViewHeight);

        int measuredWidth = (int) (mViewWidth - targetWidth);
        int measuredHeight = (int) (mViewHeight - targetHeight);

        x = measuredWidth / 2f - targetWidth / 2f;
        y = measuredHeight / 2f - targetHeight / 2f;
        gazeTracker.setTrackingRegion(measuredWidth, measuredHeight);
    }

    private void calibration() {
        targetBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.target);
        int size = cm2px(calibrationPhysicalMarkerSize);
        targetBitmap = scaleBitmap(targetBitmap, size, size);
        targetWidth = targetBitmap.getWidth();
        targetHeight = targetBitmap.getHeight();
        gazeTracker.setCalibrationMarkSize(targetWidth, targetHeight);
        gazeTracker.startCalibrating();
        calibrationFinished = false;
    }

    @Override public void run() {
        while (isDraw) {
            drawUI();
        }
    }

    private void drawUI() {
        try {
//            Log.i(TAG, "draw UI");
            mCanvas = surfaceHolder.lockCanvas();
            mCanvas.drawColor(Color.WHITE);

            if (validating) {
                x = randomPointMotion.getPosX();
                y = randomPointMotion.getPosY();
            }

            if (!calibrationFinished || !validationFinished) {
                mCanvas.drawBitmap(targetBitmap, x, y, null);
            }
            if (validating) {
                Log.i(TAG, "randomPointMotion.isShowGaze: " + randomPointMotion.isShowGaze());
            }
            if (validating && randomPointMotion.isShowGaze()) {
                // Draw Gaze Position and error bar
                if (gazeSample.getTrackingState() == TrackingState.SUCCESS) {
                    // calculate error
                    float error = calculateEuclidean(x + targetWidth / 2f, y + targetHeight / 2f,
                            gazeSample.getFilteredX(), gazeSample.getFilteredY());

                    if (errorBarVisible) {
                        mCanvas.drawCircle(gazeSample.getFilteredX(), gazeSample.getFilteredY(), 10,
                                gazePaint);
                        mCanvas.drawLine(x + targetWidth / 2f, y + targetHeight / 2f,
                                gazeSample.getFilteredX(), gazeSample.getFilteredY(), gazePaint);
                        mCanvas.drawText(String.format(Locale.CHINA, "%.2f mm", error * 10),
                                x + targetWidth / 2f - 12, y + targetHeight / 2f + 8, textPaint);
                    }
                }
            }

            if (validating && randomPointMotion.isMotionEnd()) {
                validating = false;
                validationFinished = true;
                onValidationFinished();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (mCanvas != null) {
                    surfaceHolder.unlockCanvasAndPost(mCanvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override public void onCalibrationProgress(float percent) {

    }

    @Override public void onCalibrationFinished() {
        if (!calibrationFinished) {
            calibrationFinished = true;
            Log.i(TAG, "onCalibrationFinished");
            new Thread(() -> gazeTracker.stopCalibrating()).start();
            // show dialogue
            threadHandler.post(() -> waitDialogue.show());
        }
    }

    public void onValidationFinished() {
        gazeTracker.stopSampling();
        Set<Integer> key = validationErrorMap.keySet();
        List<Float> avgErrorList = new ArrayList<>();
        for (Integer i : key) {
            float minError = Float.MAX_VALUE;
            List<Float> errorList = validationErrorMap.get(i);
            Log.i(TAG, "errorList size: " + errorList.size());
            if (null == errorList || errorList.size() < 8) {
                continue;
            } else {
                for (int j = 0; j < errorList.size() - 7; j++) {
                    float error = (errorList.get(j) + errorList.get(j + 1) + errorList.get(j + 2) +
                            errorList.get(j + 3) + errorList.get(j + 4) + errorList.get(j + 5) +
                            errorList.get(j + 6) + errorList.get(j + 7)) / 8;
                    if (minError > error) {
                        minError = error;
                        Log.i(TAG, "update min error");
                    }
                }
            }

            if (minError != Float.MAX_VALUE) {
                avgErrorList.add(minError);
            }
        }

        threadHandler.post(() -> {
            IOUtils.stringToFile(validationStringBuffer.toString(), validationSaveFile);
            String title = "验证完成";
            String message;
            Log.i(TAG, "avgErrorList size: " + avgErrorList.size());
            if (avgErrorList.isEmpty()) {
                message = "无可用数据";
            } else {
                float sum = 0;
                for (float f : avgErrorList) {
                    sum += f;
                }

                message = String.format(Locale.CHINA,
                        "可用的验证点位：" + avgErrorList.size() + "\n" +
//                                avgErrorList +
                                "误差为：%.2f cm",
                        sum / avgErrorList.size());
            }

            calibrationDialogue.setTitle(title).setMessage(message).show();
        });
    }

    @Override public void onCalibrationNextPoint(float x, float y) {
//        Log.i(TAG, "onCalibrationNextPoint(x: " + x  + ", y: " + y + ")");
        this.x = x;
        this.y = y;
    }

    @Override
    public void onCalibrationResultMessage(int status, float meanEuclidean, int allSampleSize,
                                           int availableSampleSize, String tipText) {
        Log.i(TAG, "onCalibrationResultMessage");
        threadHandler.post(() -> {
            waitDialogue.doDismiss();
            // show dialogue
            String title;
            String message;
            if (status == 1) {
                title = "calibration succeed";
                message =
                        String.format(Locale.CHINA, "Fitting mean euclidean: %.2f", meanEuclidean);
            } else {
                title = "calibration failed";
                message = tipText;
            }

            calibrationDialogue.setTitle(title).setMessage(message).show();
        });
    }

    private void validation() {
        randomPointMotion =
                new RandomPointMotion(mViewWidth, mViewHeight, targetWidth, targetHeight);
        validationFinished = false;
        validating = true;
        validationErrorMap = new HashMap<>();

        SimpleDateFormat fileNameDateFormat =
                new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CHINA);

        IOUtils.createOrExistsDir(validationSaveDir);
        String validationDirWithSessionName =
                validationSaveDir + "/" + gazeTracker.getSessionName();
        IOUtils.createOrExistsDir(validationDirWithSessionName);
        validationSaveFile = validationDirWithSessionName + "/" +
                fileNameDateFormat.format(System.currentTimeMillis()) + ".txt";
        Log.d(TAG, validationSaveFile);
        validationStringBuffer.setLength(0);
        validationStringBuffer.append("timestamp").append(",").append("trackingState").append(",")
                .append("hasCalibrated").append(",").append("gtX").append(",").append("gtY")
                .append(",").append("rawX").append(",").append("rawY").append(",")
                .append("calibratedX").append(",").append("calibratedY").append(",")
                .append("filteredX").append(",").append("filteredY").append(",")
                .append("leftDistance").append(",").append("rightDistance").append(",")
                .append("showGaze").append(",").append("positionID").append("\n");
        targetBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.target);
        int size = cm2px(validationPhysicalMarkerSize);
        targetBitmap = scaleBitmap(targetBitmap, size, size);
        targetWidth = targetBitmap.getWidth();
        targetHeight = targetBitmap.getHeight();

        gazeTracker.startSampling();
    }

    public Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (!origin.isRecycled()) {
            origin.recycle();
        }
        return newBM;
    }

    public int cm2px(float cm) {
        float dpi = getContext().getResources().getDisplayMetrics().densityDpi;
        return Math.round(dpi * cm / 2.54f);
    }

    @Override public void surfaceCreated(@NonNull SurfaceHolder holder) {
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        isDraw = false;
//        gazeTracker.stopCalibrating();
        gazeTracker.removeCallbacks(this);
    }

    @Override public void onCalibrationButtonClick() {
        calibration();
    }

    @Override public void onValidationButtonClick() {
        validation();
    }

    @Override public void onCancelButtonClick() {
        gazeTracker.onCalibrationUIExit();
        calibrationFragment.get().finishActivity();
    }

    @Override public void onGaze(GazeSample gazeSample) {
        this.gazeSample = gazeSample;
        if (validating) {
            // ADD TO FILE
            validationStringBuffer.append(gazeSample.getTimestamp()).append(",")
                    .append(gazeSample.getTrackingState().getValue()).append(",")
                    .append(gazeSample.isHasCalibrated() ? 1 : 0).append(",")
                    .append(x + targetWidth / 2f).append(",").append(y + targetHeight / 2f)
                    .append(",").append(gazeSample.getRawX()).append(",")
                    .append(gazeSample.getRawY()).append(",").append(gazeSample.getCalibratedX())
                    .append(",").append(gazeSample.getCalibratedY()).append(",")
                    .append(gazeSample.getFilteredX()).append(",").append(gazeSample.getFilteredY())
                    .append(",").append(gazeSample.getLeftDistance()).append(",")
                    .append(gazeSample.getRightDistance()).append(",")
                    .append(randomPointMotion.isShowGaze() ? 1 : 0).append(",")
                    .append(randomPointMotion.getIdx()).append("\n");

            if (gazeSample.getTrackingState() == TrackingState.SUCCESS &&
                    randomPointMotion.isShowGaze()) {
                float error = calculateEuclidean(x + targetWidth / 2f, y + targetHeight / 2f,
                        gazeSample.getFilteredX(), gazeSample.getFilteredY());
                int idx = randomPointMotion.getIdx();
                if (!validationErrorMap.containsKey(idx)) {
                    List<Float> floats = new ArrayList<>();
                    floats.add(error);
                    validationErrorMap.put(idx, floats);
                } else {
                    List<Float> floats = validationErrorMap.get(idx);
                    floats.add(error);
                }
            }
        }
    }

    public float calculateEuclidean(float gtX, float gtY, float esX, float esY) {
        float errorMarkerGazePx =
                (float) Math.sqrt(Math.pow((gtX - esX), 2) + Math.pow((gtY - esY), 2));
        return DimensionUtils.px2cm(getContext(), errorMarkerGazePx);
    }


    public void setFragment(CalibrationFragment calibrationFragment) {
        this.calibrationFragment = new WeakReference<>(calibrationFragment);
    }

//    @Override
//    public void onFace(FaceSample faceSample) {
//        Log.i(TAG, faceSample.toString());
//    }
}
