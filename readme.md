# Smartphone Eye Tracking Data Quality Assessed with EyeLink and Weblink

## Accessing the Smartphone Eye Tracking SDK

To access the smartphone eye-tracking SDK reported in this paper, please send a request to 
zhiguo@zju.edu.cn. Please note that the smartphone eye-tracking SDK is intended for 
academic use only. You will need to sign an end-user agreement before we share the 
smartphone eye-tracking SDK.

### Email Prompt

Please use the following email template for your request. Please keep the subject line unchanged:

```
Subject: Request for Accessing the Smartphone Eye Tracking SDK

Dear Prof. Zhiguo Wang,

I hope this message finds you well.

My name is [Your Name], and I am a [student/researcher] at [Your Affiliation]. I am writing to request the Smartphone Eye Tracking SDK.

We acknowledge that the use of this SDK is subject to certain restrictions. We will use this SDK solely for academic and research purposes, and we will not utilize it for commercial activities or disseminate it to others.

Thank you for considering my request. I look forward to receiving access to the SDK.

Best regards,
[Your Name]
```

### How to use the SDK

The SDK you will need is `lib-gaze-tracker-release.aar`. 
Please use the following commands to add the SDK to your project.

```
git clone https://github.com/GanchengZhu/DataQualityWithEyeLink
cd DataQualityWithEyeLink
mv {sdk folder}/lib-gaze-tracker-release.aar ./lib-gaze-tracker/
```

The SDK Documentation please refers to [this page](https://github.com/GanchengZhu/eye_tracking_data_quality_analysis/).


## Software Preparation

- Android Studio Iguana | 2023.2.1 Patch 1
- Weblink 2.2.161.0 (64-bit)

## How to install the App on the phone

Method 1: Direct Deployment via Android Studio

- Use the USB debugging mode to deploy the app directly to your phone through Android Studio.
- For details, refer to the official guide: https://developer.android.com/studio/run.

Method 2: Build APK and Install Manually

- In the Android Studio, go to `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`.
![screenshots/img.png](screenshots/img.png)
- Locate the generated APK file (usually in app/build/outputs/apk/debug/).
- Transfer the APK to your phone, open it in your file manager, and follow the prompts to install.

## How to conduct (replicate) the data quality experiment (Experiment 1)

- 1. Configure the smartphone and EyeLink Portable Duo according to the instructions in the paper. Open WebLink and set up the video capture card,  the calibration points on the phone screen, and the UDP port (recommended: 50880). Please refer to the WebLink official documentation for more details. The relevant code can be found in `app/src/main/java/org/gaze/eyetrackingtest/ReceiverUtil.java`.

- 2. Launch the App. Ensure that all necessary permissions are granted, including video recording, camera access, microphone access, and any other relevant permissions.

     ![screenshots/Permission_controller.jpg](screenshots/Permission_controller.jpg)

- 3. Enter the participant ID (ID must consist of English letters A-Z, a-z, numbers 0-9, or underscores). Toggle the `修改IP` (Modify IP Address) switch to enable IP modification, then input the IP address of your WebLink Host PC. After entering, click `Connection Test` to verify communication between the smartphone and WebLink Host PC.  
     
     ![screenshots/Screenshot_20240709_144517_EyeTrackingTest.jpg](screenshots/Screenshot_20240709_144517_EyeTrackingTest.jpg)

- 4. Click `Next` to proceed to the instruction screen. The instruction reads:  
     `屏幕上会出现5个带有数字的圆圈，请按照指导人员的要求，依次注视它们。`  
     (English: "Five numbered circles will appear on the screen. Please follow the experimenter's instructions to gaze at them sequentially.")")  
     
     ![screenshots/Screenshot_20240709_152053_EyeTrackingTest.jpg](screenshots/Screenshot_20240709_152053_EyeTrackingTest.jpg)

- 5. Once the participant understands the instructions, double-tap the screen to initiate Portable Duo calibration via WebLink. The experimenter should guide the participant to fixate on specific points, based on the current calibration point shown on the EyeLink Host PC.
     
     ![screenshots/Screenshot_20240709_152103_EyeTrackingTest.jpg](screenshots/Screenshot_20240709_152103_EyeTrackingTest.jpg)

- 6. After completing the EyeLink Portable Duo calibration, double-tap the screen again. A dialog box will appear.  
     
    ![screenshots/Screenshot_20240709_155107_EyeTrackingTest.jpg](screenshots/Screenshot_20240709_155107_EyeTrackingTest.jpg)

- 7. Click the `校准 (Calibration)` button to enter the calibration interface on the phone. Upon completion, a dialog will display the calibration results. If the error is below 0.75 cm, click the `验证 (Validation)` button to validate the calibration results. Otherwise, recalibrate the phone by clicking `校准 (Calibration)` again. During calibration and validation, please instruct the participant to maintain fixation on the red dot inside the small circle.

- 8. EyeLink gaze data is recorded during phone calibration and validation. **Mobile eye tracking data is collected only during the validation process.**

- 9. All eye tracking data can be found in android local storage `/sdcard/Android/data/org.gaze.eyetrackingtest`. For Android 11, you are not allowed to access this folder. Please grant shell permission to [MTManager](https://mt2.cn/) by installing [Shizuku](https://shizuku.rikka.app/) and activating it. 

- 10. For video demonstrations of experimental procedures, visit:  
     https://ganchengzhu.github.io/eye_tracking_data_quality_analysis/
