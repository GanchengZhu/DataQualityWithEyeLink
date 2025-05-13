package org.gaze.eyetrackingtest;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.gaze.tracker.core.GazeTracker;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReceiverUtil {
    private static String port;

//    private static final SimpleDateFormat simpleDateFormat =
//            new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.CHINA);

    private static UDPRunnable udpRunnable;
    private static Handler handler;

    public static void startReceive(String path) {
        port = SharedPreferencesUtils.getString("port", "50880");
        File file = new File(path);
        Log.i("ReceiverUtil", file.getAbsolutePath());
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;//指定以UTF-8格式写入文件
        if (!file.exists()) {
            try {
                file.createNewFile();//如果文件不存在，就创建该文件
                fos = new FileOutputStream(file);//首次写入获取
                osw = new OutputStreamWriter(fos, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ReceiverUtil", e.getMessage());
                Log.e("ReceiverUtil", "Fail to create file: " + file.getAbsolutePath());
            }
        } else {
            //如果文件已存在，那么就在文件末尾追加写入
            try {
                fos = new FileOutputStream(file, true);//这里构造方法多了一个参数true,表示在文件末尾追加写入
                osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (osw != null) {
            udpRunnable = new UDPRunnable(fos, osw, port);
//            udpRunnable.setHandler(handler);
            new Thread(udpRunnable).start();
        }

    }

    public void setGetMassage(boolean isGetPackage) {
        if (udpRunnable != null) udpRunnable.setGetPackage(isGetPackage);
    }

    public static void stopReceive() {
        udpRunnable.release();
    }

    public void setHandler(@NotNull Handler handler) {
        this.handler = handler;
    }
}

class UDPRunnable implements Runnable {
    private final FileOutputStream fos;
    private final OutputStreamWriter osw;
    private final String port;
    private DatagramSocket service;
    private AtomicBoolean isGetPackage;

    UDPRunnable(FileOutputStream fos, OutputStreamWriter osw, String port) {
        this.fos = fos;
        this.osw = osw;
        this.port = port;
        isGetPackage = new AtomicBoolean(false);
    }

//    public void setHandler(@NotNull Handler handler) {
//        this.handler = handler;
//    }

    public void release() {
        if (service != null) service.close();
    }


    public void setGetPackage(boolean getPackage) {
        isGetPackage.set(getPackage);
    }


    @Override public void run() {
        Looper.prepare();
        DatagramPacket dpReceive;
        try {
            if (service == null) {
                service = new DatagramSocket(null);
                service.setReuseAddress(true);
                service.bind(new InetSocketAddress(Integer.parseInt(port)));
            }
            StringBuilder stringBuilder = new StringBuilder();
            byte[] b = new byte[1024];
            Log.i("ReceiverUtil", "开始接受和写入UDP");
            osw.write("phone_timestamp\tel_Sample\teyelink_timestamp\tboth\tleft_x\tleft_y" +
                    "\tleft_pupil_size\tright_x\tright_y\tright_pupil_size\t_\t_" +
                    "\ttarget\t_\t_\t_\n");
            osw.flush();
            while (!service.isClosed()) {
                dpReceive = new DatagramPacket(b, b.length);
                service.receive(dpReceive);
                byte[] data = dpReceive.getData();
                int len = data.length;
                stringBuilder.append(GazeTracker.getInstance().nanoTime());
                stringBuilder.append("\t");
                stringBuilder.append(new String(data, 0, len).trim());
                stringBuilder.append("\n");
//                if (isGetPackage.get()) {
//                    Message msg = Message.obtain(); // 实例化消息对象
//                    msg.what = 25; // 消息标识
//                    msg.obj = stringBuilder.toString(); // 消息内容存放
//                    handler.sendMessage(msg);
//                    Log.i("ReceiverUtil", "发送数据帧: " + msg.obj);
//                    isGetPackage.set(false);
//                }
//                recordActivity.tipUpdateDataframe(stringBuilder.toString());
                if (osw != null) {
                    osw.write(stringBuilder.toString());
                    osw.flush();
                }
                stringBuilder.setLength(0);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ReceiverUtil", e.getMessage());
//            Toast.makeText(MyApp.getInstance(), "Fail to create socket server!", Toast
//            .LENGTH_SHORT)
//                    .show();
        }
        try {
            fos.close();
            if (osw != null) osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
