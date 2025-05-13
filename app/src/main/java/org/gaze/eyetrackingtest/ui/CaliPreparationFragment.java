/*******************************************************************************
 * Copyright (C) 2023 Gancheng Zhu
 * Email: psycho@zju.edu.cn
 ******************************************************************************/

package org.gaze.eyetrackingtest.ui;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.gaze.tracker.R;

public class CaliPreparationFragment extends BaseFragment {

    private Button nextAppCompatButton;
    private ImageView secondImageView;
    private MediaPlayer countDownMediaPlayer;
    int count = 9;
    private final int [] resList = {
            R.drawable.figure_0,
            R.drawable.figure_1,
            R.drawable.figure_2,
            R.drawable.figure_3,
            R.drawable.figure_4,
            R.drawable.figure_5,
            R.drawable.figure_6,
            R.drawable.figure_7,
            R.drawable.figure_8,
            R.drawable.figure_9,
    };
    private boolean isJumped = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cali_preparation, container, false);
        countDownMediaPlayer = MediaPlayer.create(getContext(), R.raw.prepare_tip);
        nextAppCompatButton = view.findViewById(R.id.next_ac_button);
        secondImageView = view.findViewById(R.id.second_tv);
        secondImageView.setImageResource(R.drawable.figure_9);

        nextAppCompatButton.setOnClickListener(v -> {
            jumpToCalibrationUI();
        });
        countDownMediaPlayer.start();

        CountDownTimer countDownTimer = new CountDownTimer(10 * 1000,
                1000) {
            @Override
            public void onTick(long millisUntilFinished) {
//                String value = String.valueOf((int) (millisUntilFinished / 1000));
//                mTvValue.setText(value);
                secondImageView.setImageResource(resList[count]);
                count--;
            }

            @Override
            public void onFinish() {
                secondImageView.setVisibility(View.GONE);
                jumpToCalibrationUI();
            }
        };
        countDownTimer.start();
        return view;
    }

    private synchronized void jumpToCalibrationUI() {
        if(!isJumped) {
            countDownMediaPlayer.stop();
            CalibrationFragment calibrationFragment = new CalibrationFragment();
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = manager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, calibrationFragment).commit();
//            Intent intent = new Intent(getContext(), CalibrationActivity.class);
//            startActivity(intent);
//            getActivity().finish();
        }
        isJumped = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownMediaPlayer != null) {
            countDownMediaPlayer.release();
            countDownMediaPlayer = null;
        }
    }
}



