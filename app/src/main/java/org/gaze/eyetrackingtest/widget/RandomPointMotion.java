package org.gaze.eyetrackingtest.widget;

import android.content.Context;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RandomPointMotion {
    private final int mTargetWidth;
    private final int mTargetHeight;
    private final int mViewWidth;
    private final int mViewHeight;
    private final long preparationTime = 2;
    private final long startTimestamp;
    private final float dwellingTime = 2;
    private final float movingTime = 1.5F;
    public float x1, x2, y1, y2 = 0;
    protected List<PointF> positionList = new ArrayList<>();
    protected Context context;
    protected float showGazeStartTime = 0.3F;
    protected float showGazeEndTime = 1.7F;
    private boolean showGaze = false;
    private float x;
    private float y;
    private boolean motionEnd = false;
    private int idx;


    public RandomPointMotion(int viewWidth, int viewHeight, int targetWidth, int targetHeight) {
        this.mViewWidth = viewWidth;
        this.mViewHeight = viewHeight;
        this.mTargetWidth = targetWidth;
        this.mTargetHeight = targetHeight;
        initRandomPoint(24);
        startTimestamp = System.nanoTime();
    }

    private void initRandomPoint(int nPoints) {
        for (int i = 0; i < nPoints; i++) {
            positionList.add(new PointF(
                    new Random().nextFloat() * (mViewWidth - mTargetWidth),
                    new Random().nextFloat() * (mViewHeight - mTargetHeight)
            ));
        }
    }


    public float getPosX() {
        long timeElapsed = System.nanoTime() - startTimestamp;
        float timeElapsedSecond = timeElapsed * 1e-9f;
        if (timeElapsedSecond < preparationTime) {
            // for center dwelling stimulus
            showGaze = false;
            x = 0.5F * (mViewWidth - mTargetWidth);
            y = 0.5F * (mViewHeight - mTargetHeight);
        } else {
            timeElapsedSecond -= preparationTime;
            idx = (int) (timeElapsedSecond / (dwellingTime + movingTime));
            PointF curPoint2F;
            PointF prePoint2F;

            if (idx == positionList.size()) {
                motionEnd = true;
                showGaze = false;
                return x;
            }

            if (idx == 0) {
                prePoint2F = new PointF(0.5F * (mViewWidth - mTargetWidth),
                        0.5F * (mViewHeight - mTargetHeight));
            } else {
                prePoint2F = positionList.get(idx - 1);
            }
            curPoint2F = positionList.get(idx);

            float trialTime = timeElapsedSecond - (dwellingTime + movingTime) * idx;
//            Log.i("PointMotion", "trialTime: " + trialTime);
            // moving
            if (trialTime <= movingTime) {
                float percent = (float) trialTime / (float) movingTime;
                x = percent * (curPoint2F.x - prePoint2F.x) + prePoint2F.x;
                y = percent * (curPoint2F.y - prePoint2F.y) + prePoint2F.y;
                showGaze = false;
            } else {
                // dwelling
                x = curPoint2F.x;
                y = curPoint2F.y;
//                Log.i("PointMotion", "trialTime: " + trialTime);
                float currentDwellingTime = trialTime - movingTime;
                // skip frame
                showGaze = (currentDwellingTime > showGazeStartTime) &&
                        (currentDwellingTime < showGazeEndTime);
            }
        }
        return x;
    }

    public float getPosY() {
        return y;
    }

    public boolean isShowGaze() {
        return showGaze;
    }

    public boolean isMotionEnd() {
        return motionEnd;
    }

    public int getIdx() {
        return idx;
    }
}
