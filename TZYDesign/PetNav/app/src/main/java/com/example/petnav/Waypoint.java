package com.example.petnav;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.example.petnav.R;
import com.example.petnav.util.AMapUtil;
import com.example.petnav.util.ToastUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class Waypoint extends Activity implements OnMarkerClickListener, GeocodeSearch.OnGeocodeSearchListener {
    private MapView mMapView;
    private AMap mAmap;
    private ProgressDialog progDialog = null;
    private int count,i = 1;
    private MarkerOptions markerOption;
    private float la1,la2,la3,la4,la5,la6,la7,la8,la9,la10;
    private float lo1,lo2,lo3,lo4,lo5,lo6,lo7,lo8,lo9,lo10;
    private String[] PointList,temp = null;
    private String PointRecieve,regeocoder;
    private GeocodeSearch geocoderSearch;
    private LatLonPoint latLonPoint = null;
    private RegeocodeResult result;


    @Override
    protected  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waypoint);
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        while (PointRecieve == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = new Socket("121.41.104.6", 6172);
//                        OutputStream sendData = socket.getOutputStream();
//                        sendData.write(("").getBytes("utf-8"));
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                        count = Integer.parseInt(bufferedReader.readLine());
                        while (i <=count){
                            PointRecieve = bufferedReader.readLine();
                            temp = new String[temp.length+3];
                            temp[i] = PointRecieve.split("\\,")[1];
                            temp[i+1] = PointRecieve.split("\\,")[2];
                            temp[i+2] = PointRecieve.split("\\,")[3];
                            PointList = temp;
                            init();
                            markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.claw))
                                    .position(new LatLng(Double.valueOf(PointList[i+1]),Double.valueOf(PointList[i+2])))
                                    .draggable(false);
                            mAmap.addMarker(markerOption);
                        }

//                        sendData.close();
                        bufferedReader.close();
                        socket.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }

    }

    private void init() {
        if (mAmap == null){
            mAmap = mMapView.getMap();
            mAmap.setOnMarkerClickListener(this);
//            addMarkersToMap();//添加marker
        }
    }
    /**
     * 显示进度条对话框
     */
    public void showDialog() {
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在获取地址");
        progDialog.show();
    }

    /**
     * 隐藏进度条对话框
     */
    public void dismissDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }


    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
    private void addMarkersToMap(){
        markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.claw))
                .position(new LatLng(la1,lo1))
                .draggable(false);
        mAmap.addMarker(markerOption);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (mAmap != null){
            jumpPoint(marker);
        }
        latLonPoint = new LatLonPoint(marker.getPosition().latitude,marker.getPosition().longitude);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint,200,GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocoderSearch.getFromLocationAsyn(query);// 设置异步逆地理编码请求
        regeocoder = result.getRegeocodeAddress().getFormatAddress()
                + "附近";
        Toast.makeText(Waypoint.this,"今天"+ i +"点，您的宠物在"+ i ,Toast.LENGTH_LONG).show();
        return true;
    }

    public void jumpPoint(final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mAmap.getProjection();
        final LatLng markerLatlng = marker.getPosition();
        Point markerPoint = proj.toScreenLocation(markerLatlng);
        markerPoint.offset(0, -100);
        final LatLng startLatLng = proj.fromScreenLocation(markerPoint);
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * markerLatlng.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * markerLatlng.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }
}
