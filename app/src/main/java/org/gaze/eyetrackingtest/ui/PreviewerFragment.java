/*******************************************************************************
 * Copyright (C) 2023 Gancheng Zhu
 * Email: psycho@zju.edu.cn
 ******************************************************************************/

package org.gaze.eyetrackingtest.ui;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.gaze.eyetrackingtest.widget.AutoFitSurfaceView;
import org.gaze.eyetrackingtest.widget.FaceView;
import org.gaze.tracker.R;
import org.gaze.tracker.core.GazeTracker;
import org.gaze.tracker.listener.PreviewerCallback;

public class PreviewerFragment extends BaseFragment
        implements SurfaceHolder.Callback, PreviewerCallback {
    private final String TAG = getClass().getSimpleName();
    private AutoFitSurfaceView surfaceView;
    private FaceView faceView;
    private GazeTracker tracker;
    private Button nextAppCompatButton;
    private MediaPlayer faceInCircleMediaPlayer;


    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        faceInCircleMediaPlayer = MediaPlayer.create(getContext(), R.raw.face_in_circle);
        View view = inflater.inflate(R.layout.fragment_previewer, container, false);
        surfaceView = view.findViewById(R.id.previewer_auto_fit_surface_view);
        if (getScreenOrientation(getContext()) ==
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) surfaceView.getLayoutParams();
            // 设置左边距
            layoutParams.rightMargin = 200;
            surfaceView.setLayoutParams(layoutParams);
        }

        faceView = view.findViewById(R.id.face_circle_view);
        nextAppCompatButton = view.findViewById(R.id.next_ac_button);
        nextAppCompatButton.setOnClickListener(v -> {
            faceInCircleMediaPlayer.stop();
            tracker.stopPreviewer();
            tracker.removeCallbacks(this);
            tracker.removePreviewSurface();
            EyeLinkCaliGridFragment eyeLinkCaliGridFragment = new EyeLinkCaliGridFragment();
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = manager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, eyeLinkCaliGridFragment).commit();
        });

        surfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        surfaceView.getHolder().addCallback(this);
        tracker = GazeTracker.getInstance();
        tracker.addCallbacks(this);
        return view;
    }

    @Override public void onStart() {
        super.onStart();
    }

    public int getScreenOrientation(Context context) {
        WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            int orientation = display.getRotation();
            switch (orientation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    return android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    return android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                default:
                    return android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
            }
        }
        return android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    @Override public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        WindowManager windowManager =
                (WindowManager) (getContext().getSystemService(Context.WINDOW_SERVICE));
        int degree = 0;
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            switch (display.getRotation()) {
                case Surface.ROTATION_0:
                    degree = 0; // portrait
                    break;
                case Surface.ROTATION_90:
                    degree = 90; // landscape right
                    break;
                case Surface.ROTATION_180:
                    degree = 180; // portrait upsize
                    break;
                case Surface.ROTATION_270:
                    degree = 270; // landscape left
                    break;
            }
        }
        if (degree == 0 || degree == 180) surfaceView.setAspectRatio(480, 640);
        else {
            surfaceView.setAspectRatio(640, 480);
        }

        surfaceView.post(() -> {
            if (tracker != null) {
                tracker.startPreviewer();
                tracker.setPreviewSurface(surfaceView.getHolder().getSurface());
                faceInCircleMediaPlayer.start();
            }
        });
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @Override public void onDestroy() {
        super.onDestroy();

        // 释放 MediaPlayer 资源
        if (faceInCircleMediaPlayer != null) {
            faceInCircleMediaPlayer.release();
            faceInCircleMediaPlayer = null;
        }
    }

    @Override public void onMessage(String message) {
        if (null != message && !message.isEmpty()) {
            if (faceView != null) {
                Log.i(TAG, "receive message");
                faceView.setMessage(message);
                faceView.postInvalidate();
            }
        }
    }
}
