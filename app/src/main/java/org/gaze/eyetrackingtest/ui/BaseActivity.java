package org.gaze.eyetrackingtest.ui;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.gaze.tracker.helper.CameraPermissionUtils;

public abstract class BaseActivity extends AppCompatActivity {
    protected final String TAG = getClass().getSimpleName();

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }

//        setOrientation();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!CameraPermissionUtils.hasCameraPermission(this)) {
            CameraPermissionUtils.requestCameraPermission(this);
        }else{
            onPermissionsSuccess();
        }
        setContentView(getLayoutId());
    }

//    public void setOrientation(){
//        if (GazeTracker.getInstance().getConfig().orientationType == OrientationType.PORTRAIT){
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }else {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        }
//    }

    public abstract int getLayoutId();

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CameraPermissionUtils.REQUEST_CODE_PERMISSIONS) {
            if (!CameraPermissionUtils.hasCameraPermission(this)) {
                onPermissionsDeny();
                finish();
            } else {
                onPermissionsSuccess();
            }
        }
    }

    public void onPermissionsSuccess() {
    }

    public void onPermissionsDeny() {
        Toast.makeText(getBaseContext(), "请您打开必要权限", Toast.LENGTH_SHORT).show();
    }
}
