package com.example.petnav;

import android.content.Intent;
import android.os.Bundle;

import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.NaviLatLng;
import com.example.petnav.R;

import java.util.ArrayList;
import java.util.List;

public class CarNavi extends BaseActivity {

    private String lat, lon;
    private double HospitalLat, HospitalLon;
    protected NaviLatLng mEndLatlng = new NaviLatLng(32.0587774300,121.3234645100);
    protected NaviLatLng mStartLatlng = new NaviLatLng(32.06109835541733,121.331600321);
    protected final List<NaviLatLng> sList = new ArrayList<NaviLatLng>();
    protected final List<NaviLatLng> eList = new ArrayList<NaviLatLng>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        lon = bundle.getString("targetlongitude");
        lat = bundle.getString("targetlatitude");
        HospitalLat = bundle.getDouble("poilatitude");
        HospitalLon = bundle.getDouble("poilongitude");
        mStartLatlng = new NaviLatLng(Double.valueOf(lat),Double.valueOf(lon));
        mEndLatlng = new NaviLatLng(HospitalLat,HospitalLon);
        sList.add(mStartLatlng);
        eList.add(mEndLatlng);



        setContentView(R.layout.activity_basic_navi);
        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);

        AMapNaviViewOptions options = new AMapNaviViewOptions();
        options.setScreenAlwaysBright(false);
        mAMapNaviView.setViewOptions(options);


    }

    @Override
    public void onInitNaviSuccess() {
        super.onInitNaviSuccess();
        /**
          方法: int strategy=mAMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, multipleroute); 参数:

         * @congestion 躲避拥堵
         * @avoidhightspeed 不走高速
         * @cost 避免收费
         * @hightspeed 高速优先
         * @multipleroute 多路径
         *
         *  说明: 以上参数都是boolean类型，其中multipleroute参数表示是否多条路线，如果为true则此策略会算出多条路线。
         *  注意: 不走高速与高速优先不能同时为true 高速优先与避免收费不能同时为true
         */
        int strategy = 0;
        try {
            //再次强调，最后一个参数为true时代表多路径，否则代表单路径
            strategy = mAMapNavi.strategyConvert(true, true, false, false, false);
        } catch (Exception e) {
            e.printStackTrace();

        }
        mAMapNavi.calculateDriveRoute(sList,eList, null, strategy);

    }

    @Override
    public void onCalculateRouteSuccess(int[] ids) {
        super.onCalculateRouteSuccess(ids);
        mAMapNavi.startNavi(NaviType.GPS);
    }
}
