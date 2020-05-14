package com.example.petnav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.enums.PathPlanningStrategy;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.autonavi.tbt.TrafficFacilityInfo;

import java.util.ArrayList;


public class RoutePlanningActivity extends Activity implements AMapNaviListener, View.OnClickListener {


    // 起点、终点坐标显示
    private TextView endInput;
    // 驾车线路：路线规划、模拟导航、实时导航按钮
    private Button mDriveRouteButton, mDriveNaviStart, mDriveNaviCancle;
    // 步行线路：路线规划、模拟导航、实时导航按钮
    private Button mFootRouteButton, mFootNaviStart, mFootNaviCancle;
    // 地图和导航资源
    private MapView mMapView;
    private AMap mAMap;

    // 起点终点坐标
    private NaviLatLng mNaviStart = new NaviLatLng(39.989614, 116.481763);
    private NaviLatLng mNaviEnd = new NaviLatLng(39.983456, 116.3154950);
    // 起点终点列表
    private ArrayList<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
    private ArrayList<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();

    // 规划线路
    private RouteOverLay mRouteOverLay;
    private AMapNavi aMapNavi;
    String lon, lat, HospitalName;
    double HospitalLat, HospitalLon;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        lon = bundle.getString("targetlongitude");
        lat = bundle.getString("targetlatitude");
        HospitalLat = bundle.getDouble("poilatitude");
        HospitalLon = bundle.getDouble("poilongitude");
        HospitalName = bundle.getString("poiName");
        mNaviStart = new NaviLatLng(Double.valueOf(lat),Double.valueOf(lon));
        mNaviEnd = new NaviLatLng(HospitalLat,HospitalLon);
        setContentView(R.layout.activity_route_planning);
        endInput = (TextView) findViewById(R.id.end_position_textview);
        endInput.setText(HospitalName);
        aMapNavi = AMapNavi.getInstance(this);
        aMapNavi.addAMapNaviListener(this);
        aMapNavi.setUseInnerVoice(true);
        aMapNavi.setEmulatorNaviSpeed(150);

        initView(savedInstanceState);
    }

    // 初始化View
    private void initView(Bundle savedInstanceState) {
        mDriveRouteButton = (Button) findViewById(R.id.car_navi_route);
        mFootRouteButton = (Button) findViewById(R.id.foot_navi_route);
        mDriveNaviStart = (Button) findViewById(R.id.car_navi_start);
        mDriveNaviCancle = (Button) findViewById(R.id.car_navi_cancle);
        mFootNaviStart = (Button) findViewById(R.id.foot_navi_start);
        mFootNaviCancle = (Button) findViewById(R.id.foot_navi_cancle);

        mDriveRouteButton.setOnClickListener(this);
        mDriveNaviStart.setOnClickListener(this);
        mDriveNaviCancle.setOnClickListener(this);
        mFootRouteButton.setOnClickListener(this);
        mFootNaviStart.setOnClickListener(this);
        mFootNaviCancle.setOnClickListener(this);

        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);
        mAMap = mMapView.getMap();
        mRouteOverLay = new RouteOverLay(mAMap, null, getApplicationContext());
        mRouteOverLay.zoomToSpan();
    }

    //计算驾车路线
    private void calculateDriveRoute() {
        mStartPoints.clear();
        mEndPoints.clear();
        mStartPoints.add(mNaviStart);
        mEndPoints.add(mNaviEnd);

        boolean isSuccess = aMapNavi.calculateDriveRoute(mStartPoints,
                mEndPoints, null, PathPlanningStrategy.DRIVING_DEFAULT);
        if (!isSuccess) {
            showToast("路线计算失败,检查参数情况");
        }

    }

    //计算步行路线
    private void calculateFootRoute() {
        boolean isSuccess = aMapNavi.calculateWalkRoute(mNaviStart, mNaviEnd);
        if (!isSuccess) {
            showToast("路线计算失败,检查参数情况");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    //-------------------------Button点击事件和返回键监听事件---------------------------------
    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();

        switch (v.getId()) {
            case R.id.car_navi_route:
                calculateDriveRoute();
                mDriveRouteButton.setVisibility(View.GONE);
                mDriveNaviStart.setVisibility(View.VISIBLE);
                mDriveNaviCancle.setVisibility(View.VISIBLE);
                mFootNaviStart.setVisibility(View.GONE);
                mFootNaviCancle.setVisibility(View.GONE);
                mFootRouteButton.setVisibility(View.VISIBLE);
                break;
            case R.id.foot_navi_route:
                calculateFootRoute();
                mFootRouteButton.setVisibility(View.GONE);
                mDriveNaviCancle.setVisibility(View.GONE);
                mDriveNaviStart.setVisibility(View.GONE);
                mFootNaviStart.setVisibility(View.VISIBLE);
                mFootNaviCancle.setVisibility(View.VISIBLE);
                mDriveRouteButton.setVisibility(View.VISIBLE);
                break;
            case R.id.car_navi_cancle:
                mDriveRouteButton.setVisibility(View.VISIBLE);
                mDriveNaviStart.setVisibility(View.GONE);
                mDriveNaviCancle.setVisibility(View.GONE);
                break;
            case R.id.car_navi_start:
                Log.i("none", "正在跳转页面...");
                bundle.putString("targetlongitude", lon + "");
                bundle.putString("targetlatitude", lat + "");
                bundle.putDouble("poilatitude",HospitalLat);
                bundle.putDouble("poilongitude",HospitalLon);
                intent.putExtras(bundle);
                intent.setClass(RoutePlanningActivity.this, CarNavi.class);
                RoutePlanningActivity.this.startActivity(intent);
                break;
            case R.id.foot_navi_start:
                Log.i("none", "正在跳转页面...");
                bundle.putString("targetlongitude", lon + "");
                bundle.putString("targetlatitude", lat + "");
                bundle.putDouble("poilatitude",HospitalLat);
                bundle.putDouble("poilongitude",HospitalLon);
                intent.putExtras(bundle);
                intent.setClass(RoutePlanningActivity.this, WalkNavi.class);
                RoutePlanningActivity.this.startActivity(intent);
                break;
            case R.id.foot_navi_cancle:
                mFootRouteButton.setVisibility(View.VISIBLE);
                mFootNaviStart.setVisibility(View.GONE);
                mFootNaviCancle.setVisibility(View.GONE);
                break;
            default:
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();

        }
        return super.onKeyDown(keyCode, event);
    }

    //--------------------导航监听回调事件-----------------------------
    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onArrivedWayPoint(int arg0) {

    }

    @Override
    public void onCalculateRouteFailure(int arg0) {
        showToast("路径规划出错" + arg0);
    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        AMapNaviPath naviPath = aMapNavi.getNaviPath();
        if (naviPath == null) {
            return;
        }
        // 获取路径规划线路，显示到地图上
        mRouteOverLay.setAMapNaviPath(naviPath);
        mRouteOverLay.addToMap();
        mRouteOverLay.zoomToSpan();
    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onGetNavigationText(int arg0, String arg1) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onGpsOpenStatus(boolean arg0) {

    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation arg0) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo arg0) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapCameraInfos) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] amapServiceAreaInfos) {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onStartNavi(int arg0) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

//------------------生命周期重写函数---------------------------

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        mStartPoints.add(mNaviStart);
        mEndPoints.add(mNaviEnd);

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        aMapNavi.destroy();
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo arg0) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }


    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int i) {

    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

    }
}
