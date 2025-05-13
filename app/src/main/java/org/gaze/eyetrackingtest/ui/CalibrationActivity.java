/*******************************************************************************
 * Copyright (C) 2023 Gancheng Zhu
 * Email: psycho@zju.edu.cn
 ******************************************************************************/

package org.gaze.eyetrackingtest.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.style.IOSStyle;

import org.gaze.eyetrackingtest.ReceiverUtil;
import org.gaze.eyetrackingtest.SharedPreferencesUtils;
import org.gaze.tracker.R;
import org.gaze.tracker.core.GazeTracker;

public class CalibrationActivity extends BaseActivity {
    PreviewerFragment previewerFragment;
    GazeTracker gazeTracker;

    @Override public int getLayoutId() {
        return R.layout.activity_calibration_preparation;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialogX.init(this);
        DialogX.globalStyle = new IOSStyle();
        previewerFragment = new PreviewerFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, previewerFragment).commit();
        gazeTracker = GazeTracker.getInstance();
        gazeTracker.setErrorBarVisible(SharedPreferencesUtils.getBoolean("debug", true));
    }


    @Override public void onDestroy() {
        super.onDestroy();
        ReceiverUtil.stopReceive();
//        switch (gazeTracker.runningStatus()){
//            case CALIBRATING:
//                gazeTracker.stopCalibrating();
//                break;
//            case PREVIEWING:
//                gazeTracker.stopPreviewer();
//                break;
//            case SAMPLING:
//                gazeTracker.stopSampling();
//                break;
//            case CLOSING:
//                break;
//        }
    }
}
