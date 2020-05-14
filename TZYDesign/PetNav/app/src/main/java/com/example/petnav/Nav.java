package com.example.petnav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Nav extends CheckPermissionsActivity {
    Button button1, button2;
    String SelfLon, SelfLat;
    public static String lat, lon;
    TextView selflontext, selflattext, lonValue, latValue;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav);
        button1 = (Button) findViewById(R.id.refresh);
        button1.setOnClickListener(new button1_click());
        button2 = (Button) findViewById(R.id.move);
        button2.setOnClickListener(new button2_click());
        selflontext = (TextView) findViewById(R.id.lonS);
        selflattext = (TextView) findViewById(R.id.latS);
        lonValue = (TextView) findViewById(R.id.lonT);
        latValue = (TextView) findViewById(R.id.latT);
        initLocation();
        startLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocation();
        destroyLocation();
    }


    class button2_click implements View.OnClickListener {
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
            intent.setClass(Nav.this, WalkTest.class);
            Nav.this.startActivity(intent);

        }
    }

    class button1_click implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
// TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (SelfLat != null && Double.parseDouble(SelfLon) >= 0) {
                    while (lon == null) {
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
                    lonValue.setText(lon);
                    latValue.setText(lat);
                    selflattext.setText(SelfLat);
                    selflontext.setText(SelfLon);
                    if (lon != null && SelfLon != null && lat != null && SelfLat != null){
                        button2.setEnabled(true);
                        Toast toast1 = Toast.makeText(getApplicationContext(), "定位成功，点击带我去即可进行步行导航", Toast.LENGTH_SHORT);
                        toast1.show();
                    }
                    break;
                }else {
                    Toast toast = Toast.makeText(getApplicationContext(), "正在定位，请在本条提示消失后再次刷新定位", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
    }


    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {
                SelfLon = String.valueOf(location.getLongitude());
                SelfLat = String.valueOf(location.getLatitude());
            }
        }
    };

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


    private void resetOption() {
        // 设置是否需要显示地址信息
        locationOption.setNeedAddress(false);
        /**
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
        locationOption.setGpsFirst(true);
        // 设置是否开启缓存
        locationOption.setLocationCacheEnable(true);
        // 设置是否单次定位
        locationOption.setOnceLocation(false);
        //设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
        locationOption.setOnceLocationLatest(true);
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
