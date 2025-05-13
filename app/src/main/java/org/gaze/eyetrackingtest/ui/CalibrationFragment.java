/*******************************************************************************
 * Copyright (C) 2023 Gancheng Zhu
 * Email: psycho@zju.edu.cn
 ******************************************************************************/

package org.gaze.eyetrackingtest.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.gaze.eyetrackingtest.R;
import org.gaze.eyetrackingtest.widget.CalibrationSurfaceView;

public class CalibrationFragment extends Fragment {
    CalibrationSurfaceView calibrationSurfaceView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calibration, container, false);
        calibrationSurfaceView = view.findViewById(R.id.calibration_surface_view);
        calibrationSurfaceView.setFragment(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    public void finishActivity(){
        requireActivity().finish();
    }
}
