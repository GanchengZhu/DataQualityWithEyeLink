package org.gaze.eyetrackingtest.widget;

import com.kongzue.dialogx.dialogs.MessageDialog;

public class CalibrationDialogue {

    private String title;
    private String message;
    private OnClickListener onClickListener;
    private MessageDialog messageDialog;

    //
//(R.string.setting_title,
//    R.string.setting_introduce, R.string.calibrate,
//    R.string.setting_exit, R.string.validate)
    CalibrationDialogue() {
        this("眼动校准和验证", "", "校准", "退出", "验证");

    }

    CalibrationDialogue(String title, String message, String calibration,
                        String exit, String validation) {
        messageDialog = new MessageDialog(title, message, calibration, exit, validation);
        // this.context = new WeakReference<>(context);
        this.title = title;
        this.message = message;
    }

    public void show() {


        messageDialog.setOkButtonClickListener((dialog, v) -> {
            messageDialog.dismiss();
            if (onClickListener != null) {
                onClickListener.onCalibrationButtonClick();
            }
            return true;
        });

        messageDialog.setOtherButtonClickListener((dialog, v) -> {
            messageDialog.dismiss();
            if (onClickListener != null) {
                onClickListener.onValidationButtonClick();
            }
            return true;
        });

        messageDialog.setCancelButtonClickListener((dialog, v) -> {
            if (onClickListener != null) {
                onClickListener.onCancelButtonClick();
            }
            return true;
        });
        messageDialog.setTitle(title);
        messageDialog.setMessage(message);
        messageDialog.setCancelable(false);
        messageDialog.show();
    }


    public String getTitle() {
        return title;
    }

    public CalibrationDialogue setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public CalibrationDialogue setMessage(String message) {
        this.message = message;
        return this;
    }

    public OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public CalibrationDialogue setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public interface OnClickListener {
        void onCalibrationButtonClick();

        void onValidationButtonClick();

        void onCancelButtonClick();
    }

}
