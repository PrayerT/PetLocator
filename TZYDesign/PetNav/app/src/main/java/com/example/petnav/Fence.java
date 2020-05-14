package com.example.petnav;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Fence extends CheckPermissionsActivity {
    Button confirm, refresh, gonav;
    TextView state, distance;
    EditText standard;
    String dis, lat, lon;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    double SelfLon, SelfLat, length;
    int counter = 0;


    //循环模块
    Handler cycle = new Handler();
    Runnable r = new Runnable() {
        @Override
        public void run() {
            while (lon == null && length == 0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket = new Socket("121.41.104.6", 6170);
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                            lon = bufferedReader.readLine();
                            lat = bufferedReader.readLine();
                            bufferedReader.close();
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            if (lon != null && lat != null && SelfLat != 0 && SelfLon != 0) {
                length = AMapUtils.calculateLineDistance(new LatLng(SelfLat, SelfLon), new LatLng(Double.parseDouble(lat), Double.parseDouble(lon)));
                distance.setText(Math.round(length) + "米");
                if (length >= Double.parseDouble(dis)) {
                    state.setText("在围栏外");
                    counter = counter + 1;
                    if (counter == 1) {
                        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            manager.createNotificationChannel(new NotificationChannel("default", "宠物丢失", NotificationManager.IMPORTANCE_HIGH));
                        }
                        Notification notification = new NotificationCompat.Builder(Fence.this, "default")//此处会有中间一道线，并不影响运行，这是android系统版本的问题
                                .setContentTitle("宠物跑了！")  //显示通知的标题
                                .setContentText("您的宠物已经突破围栏!")//显示消息通知的内容
                                .setWhen(System.currentTimeMillis())//显示通知的具体时间
                                .setSmallIcon(R.drawable.ic)//这里设置显示的是手机顶部系统通知的应用图标
                                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.doge))//这里设置显示的是下拉通知栏后显示的系统图标
                                .setAutoCancel(true)//可以在此使用此方法，点击通知后，通知内容自动取消,也可以在NotificationActivity.java中设置方法取消显示通知内容
                                .setPriority(Notification.PRIORITY_MAX)
                                .setVibrate(new long[]{0, 1000, 1000, 1000})//设置发出通知后震动一秒，停止一秒后再震动一秒，需要在manifest.xml中设置权限
                                .build();
                        manager.notify(1, notification);
                    }
                } else {
                    state.setText("在围栏内");
                    counter = 0;
                }
            }
            cycle.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fence);
        refresh = findViewById(R.id.refresh);
        confirm = findViewById(R.id.confirm);
        distance = findViewById(R.id.distance);
        standard = findViewById(R.id.lengthinput);
        state = findViewById(R.id.state);
        gonav = findViewById(R.id.gonav);
        gonav.setOnClickListener(new button3_click());
        refresh.setOnClickListener(new button2_click());
        confirm.setOnClickListener(new button1_click());
        initLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyLocation();
    }

    //输入围栏半径阈值
    class button1_click implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            dis = standard.getText().toString();
            refresh.setEnabled(true);
            gonav.setEnabled(true);
        }
    }

    //开始\停止围栏
    class button2_click implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (refresh.getText().equals("开始围栏")) {
                refresh.setText("停止围栏");
                startLocation();
                cycle.postDelayed(r, 200);
            } else {
                cycle.removeCallbacksAndMessages(null);
                refresh.setText("开始围栏");
                distance.setText("");
                state.setText("");
                stopLocation();
            }
        }
    }

    //已丢失跳转
    class button3_click implements View.OnClickListener {
        Intent intent = new Intent();

        @Override
        public void onClick(View v) {
            Log.i("none", "正在跳转页面...");
            Bundle bundle = new Bundle();
            bundle.putString("selflatitude", SelfLat + "");
            bundle.putString("selflongitude", SelfLon + "");
            bundle.putString("targetlongitude", lon + "");
            bundle.putString("targetlatitude", lat + "");
            intent.putExtras(bundle);
            intent.setClass(Fence.this, WalkTest.class);
            Fence.this.startActivity(intent);

        }
    }


    private void initLocation() {
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = getDefaultOption();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(false);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(true);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        mOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.ZH);//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
        return mOption;
    }

    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {
                SelfLon = location.getLongitude();
                SelfLat = location.getLatitude();
            }
        }
    };

    private void resetOption() {
        // 设置是否需要显示地址信息
        locationOption.setNeedAddress(false);
        /**
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
        locationOption.setGpsFirst(true);
        // 设置是否开启缓存
        locationOption.setLocationCacheEnable(false);
        // 设置是否单次定位
        locationOption.setOnceLocation(false);
        //设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
        locationOption.setOnceLocationLatest(false);
        //设置是否使用传感器
        locationOption.setSensorEnable(true);
        //设置是否开启wifi扫描，如果设置为false时同时会停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        //设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
        locationOption.setInterval(1000);
        // 设置网络请求超时时间
        locationOption.setHttpTimeOut(3000);
    }

    private void startLocation() {
        //根据控件的选择，重新设置定位参数
        resetOption();
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }

    private void stopLocation() {
        // 停止定位
        locationClient.stopLocation();
    }

    private void destroyLocation() {
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

}
