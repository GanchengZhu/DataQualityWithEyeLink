/*******************************************************************************
 * Copyright (C) 2023 Gancheng Zhu
 * Email: psycho@zju.edu.cn
 ******************************************************************************/

package org.gaze.eyetrackingtest.ui;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.gaze.eyetrackingtest.R;
import org.gaze.eyetrackingtest.ReceiverUtil;
import org.gaze.eyetrackingtest.SharedPreferencesUtils;
import org.gaze.eyetrackingtest.widget.BitmapSurfaceView;
import org.gaze.tracker.core.GazeTracker;
import org.gaze.tracker.helper.IOUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EyeLinkCaliGridFragment extends Fragment implements View.OnClickListener {
    private final long CLICK_INTERVAL_TIME = 300;
    BitmapSurfaceView bitmapSurfaceView;
    private long lastClickTime = 0;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        bitmapSurfaceView = new BitmapSurfaceView(getContext());
        bitmapSurfaceView.setBitmapResource(R.drawable.weblink_instruction);
        bitmapSurfaceView.setOnClickListener(this);
        return bitmapSurfaceView;
    }


    @Override public void onClick(View view) {
        long currentTimeMillis = SystemClock.uptimeMillis();
        if (currentTimeMillis - lastClickTime < CLICK_INTERVAL_TIME) {
            if (bitmapSurfaceView.getResourceId() == R.drawable.weblink_instruction) {
                bitmapSurfaceView.setBitmapResource(R.drawable.weblink_calibration);
                lastClickTime = 0;
                return;
            } else {
                Log.d("btn listener:", "btn is doubleClicked!");
                String subjectId = SharedPreferencesUtils.getString("user_id", "ABC");
                GazeTracker.getInstance().setSessionName(subjectId);
                String externalPath = getContext().getExternalFilesDir("").getAbsolutePath();
                String eyelinkDataPath = externalPath + "/eyelink/" + subjectId;
                IOUtils.createOrExistsDir(eyelinkDataPath);
                SimpleDateFormat fileNameDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss",
                        Locale.CHINA);
                String dataFilePath = eyelinkDataPath + "/" + fileNameDateFormat.format(
                        System.currentTimeMillis()) + ".tsv";
                ReceiverUtil.startReceive(dataFilePath);
                CalibrationFragment calibrationFragment = new CalibrationFragment();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = manager.beginTransaction();
                fragmentTransaction.replace(org.gaze.tracker.R.id.fragment_container, calibrationFragment).commit();
            }
        }
        lastClickTime = currentTimeMillis;
    }
}
