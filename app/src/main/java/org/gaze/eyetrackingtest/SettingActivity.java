package org.gaze.eyetrackingtest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.gaze.eyetrackingtest.ui.CalibrationActivity;
import org.gaze.tracker.core.GazeTracker;
import org.gaze.tracker.ui.BaseActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class SettingActivity extends BaseActivity {

    private TextInputEditText editTextId, editTextIP;
    private Button buttonStartExperiment, buttonConnectTest;
    private SwitchCompat switchCompat, debugSwitchCompat;
    private TextView pingResultTextView;
    private AlertDialog alertDialog;
    private Handler bgHandler;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferencesUtils.init(this);
        // 初始化视图组件
        editTextId = findViewById(R.id.text_input_edit_text_id);
        editTextIP = findViewById(R.id.text_input_edit_text_ip);
        buttonStartExperiment = findViewById(R.id.btn_start_exp);
        buttonConnectTest = findViewById(R.id.btn_connect_test);
        pingResultTextView = findViewById(R.id.tv_connect_result);
        switchCompat = findViewById(R.id.ip_modify_switch);

        debugSwitchCompat = findViewById(R.id.debug_switch);

        bgHandler = new Handler();

        switchCompat.setChecked(SharedPreferencesUtils.getBoolean("enable_debug", true));
        editTextIP.setEnabled(SharedPreferencesUtils.getBoolean("enable_debug", true));
        switchCompat.setOnCheckedChangeListener((compoundButton, b) -> {
            SharedPreferencesUtils.setBoolean("enable_debug", b);
            editTextIP.setEnabled(b);
            SharedPreferencesUtils.setString("ip_address", editTextIP.getText().toString());
        });

        debugSwitchCompat.setChecked(SharedPreferencesUtils.getBoolean("debug", true));
        debugSwitchCompat.setOnCheckedChangeListener((compoundButton, b) -> {
            SharedPreferencesUtils.setBoolean("debug", b);
        });

//        editTextId.setText(SharedPreferencesUtils.getString("subject_id", "ABC"));
        editTextIP.setText(SharedPreferencesUtils.getString("ip_address", "127.0.0.1"));

        buttonConnectTest.setOnClickListener(v -> {
            AlertDialog.Builder build = new AlertDialog.Builder(this);
            alertDialog = build.create();
            alertDialog.setCancelable(false);
            alertDialog.setTitle("正在测试连接>>>请稍后");
            alertDialog.show();
            new Thread(() -> {
                String result = ping(editTextIP.getText().toString());
                bgHandler.post(() -> {
                    alertDialog.dismiss();
                    pingResultTextView.setText(result);
                });
            }).start();
        });


        buttonStartExperiment.setOnClickListener(v -> {
            // 设置点击事件
            // 获取EditText和Spinner的值
            String id = editTextId.getText().toString().trim(); // 去除空格
            // 检查EditText和Spinner的值是否为空
            if (id.isEmpty()) {
                // 如果有任何一个为空，显示提示消息
                Toast.makeText(this, "仔细检查所有的字段是否都填写了！", Toast.LENGTH_SHORT).show();
                return;
            }

            // 检查id是否包含文件路径不允许的字符
            if (!isValidId(id)) {
                Toast.makeText(this, "被试ID只允许存在字母、数字、下划线", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferencesUtils.setString("user_id", id);
            SharedPreferencesUtils.setString("ip_address", editTextIP.getText().toString());

            Intent intent = new Intent(this, CalibrationActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // 验证id是否包含文件路径不允许的字符的方法
    private boolean isValidId(String id) {
        // 此正则表达式允许字母、数字、下划线
        String regex = "^[a-zA-Z0-9_]+$";
        return id.matches(regex);
    }

    @Override public int getLayoutId() {
        return R.layout.activity_setting;
    }


    public String ping(String str) {
        String result = "";
        Process p;
        try {
            //ping -c 3 -w 100  中  ，-c 是指ping的次数 3是指ping 3次 ，-w 100  以秒为单位指定超时间隔，是指超时时间为100秒
            p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + str);
            int status = p.waitFor();

            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }


            if (status == 0) {
                result = str + " create connection success";
            } else {
                result = str + " create connection failed";
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }

    @Override public void onPermissionsSuccess() {
        super.onPermissionsSuccess();
        GazeTracker.create(this, null);
    }

    @Override public void onPermissionsDeny() {
        super.onPermissionsDeny();
        Toast.makeText(this, "请打开相机权限！", Toast.LENGTH_SHORT).show();
    }
}