/*******************************************************************************
 * Copyright (C) 2023 Gancheng Zhu
 * Email: psycho@zju.edu.cn
 ******************************************************************************/

package org.gaze.eyetrackingtest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Gainmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.gaze.eyetrackingtest.widget.BitmapSurfaceView;
import org.gaze.tracker.core.GazeTracker;
import org.gaze.tracker.helper.IOUtils;
import org.gaze.tracker.ui.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends BaseActivity {
    BitmapSurfaceView surfaceView;


    private static final long CLICK_INTERVAL_TIME = 300;
    private static long lastClickTime = 0;
    @Override public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surfaceView = findViewById(R.id.surface_view);
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                //获取系统当前毫秒数，从开机到现在的毫秒数(手机睡眠时间不包括在内)
                long currentTimeMillis = SystemClock.uptimeMillis();
                //两次点击间隔时间小于300ms代表双击
                if (currentTimeMillis - lastClickTime < CLICK_INTERVAL_TIME) {
                    Log.d("btn listener:", "btn is doubleClicked!");
                    String subjectId = SharedPreferencesUtils.getString("user_id", "ABC");
                    String externalPath = getExternalFilesDir("").getAbsolutePath();
                    String eyelinkDataPath = externalPath + "/eyelink/" + subjectId;
                    IOUtils.createOrExistsDir(eyelinkDataPath);
                    SimpleDateFormat fileNameDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss",
                            Locale.CHINA);
                    String dataFilePath = eyelinkDataPath + "/" + fileNameDateFormat.format(
                            System.currentTimeMillis()) +".tsv";
                    ReceiverUtil.startReceive(dataFilePath);
//                    GazeTracker.getInstance().setErrorBarVisible(true);
//                    GazeTracker.getInstance().drawCalibrationUI(null);
                }
                lastClickTime = currentTimeMillis;
                Log.d("btn listener:", "btn is clicked!");
            }
        });
    }




}
