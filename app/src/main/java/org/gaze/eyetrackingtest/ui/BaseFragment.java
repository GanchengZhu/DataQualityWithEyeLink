/*******************************************************************************
 * Copyright (C) 2023 Gancheng Zhu
 * Email: psycho@zju.edu.cn
 ******************************************************************************/

package org.gaze.eyetrackingtest.ui;

import android.util.Log;

import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }
}
