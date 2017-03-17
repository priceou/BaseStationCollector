package com.example.shangchang.basestationcollector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.LoginFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.http.HttpClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.HeatMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.example.shangchang.basestationcollector.DB.BSinfo;
import com.example.shangchang.basestationcollector.DB.DBhelper;
import com.example.shangchang.basestationcollector.DB.SigInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class Baidumaptest extends AppCompatActivity {
    private android.os.Handler handler=new android.os.Handler(){
        public void handlemsg(Message msg){
            switch (msg.arg1){
                case 1:
                    bsjson=msg.getData().getString("json");
                    break;
                case 2:
                    signaljson=msg.getData().getString("json");
                    break;

            }
        }
    };


    private MapView mapView=null;
    private BaiduMap baiduMap;
    private LocationClient locationClient;
    private myLocationListener myll;
    private boolean isfirst=true;
    private double mylati=0;
    private double mylongi=0;
    private TelephonyManager tm;
    private  Marker marker=null;
    private ContentValues values;
    private String bsjson="";
    private String signaljson="";
    private ArrayList<Marker> sigMarker=null;
    private ArrayList<SigInfo> siArray=null;






    private BitmapDescriptor mbitmapdes;
    private String receive="";
    String oid="3527";
    String key="81C8A3813B80959998BA39236175BAF3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_baidumaptest);

        sigMarker=new ArrayList<Marker>();
        siArray=new ArrayList<SigInfo>();
        mapView=(MapView) findViewById(R.id.map_baidumap);

        values=new ContentValues();

        baiduMap=mapView.getMap();



        baiduMap.setMyLocationEnabled(true);
        locationClient=new LocationClient(getApplicationContext());
        myll=new myLocationListener();
        locationClient.registerLocationListener(myll);
        locationClient.start();
        setclientoption(locationClient);

        tm=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        myPhoneStateManager myPSM=new myPhoneStateManager();
        tm.listen(myPSM,PhoneStateListener.LISTEN_CELL_LOCATION|PhoneStateListener.LISTEN_SIGNAL_STRENGTHS| PhoneStateListener.LISTEN_CELL_INFO);

        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Bundle bundle=marker.getExtraInfo();
                int lac=bundle.getInt("lac");
                int cid=bundle.getInt("cid");
                Toast.makeText(getApplicationContext(),"marker click!lac:"+lac+" cid:"+cid,Toast.LENGTH_LONG).show();
                DeleteSigMarker(lac,cid);

                return false;
            }
        });


        Thread thread1=new Thread(new Runnable() {
            @Override
            public void run() {
                request(2);
                siArray=addSig(signaljson);
            }
        });
        thread1.start();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
        locationClient.stop();



    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    private class myLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            MyLocationData myLocationData=new MyLocationData.Builder().accuracy(bdLocation.getRadius())
                    .direction(100).latitude(bdLocation.getLatitude()).longitude(bdLocation.getLongitude())
                   .build();

            mylati=bdLocation.getLatitude();
            mylongi=bdLocation.getLongitude();
            baiduMap.setMyLocationData(myLocationData);
            if (isfirst) {
                isfirst=false;
                LatLng latLng=new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                MapStatusUpdate msu=MapStatusUpdateFactory.newLatLngZoom(latLng,15);
                baiduMap.animateMapStatus(msu);
            }
        }
    }

    private void setclientoption(LocationClient lc){
        LocationClientOption lcoption=new LocationClientOption();
        lcoption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        lcoption.setCoorType("bd09ll");
        lcoption.setIsNeedAddress(true);
        lcoption.setOpenGps(true);
        lcoption.setScanSpan(1000);
        lc.setLocOption(lcoption);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();

        switch (id) {
            case R.id.btn_backcenter:
                LatLng latLng=new LatLng(mylati,mylongi);
                MapStatusUpdate msu=MapStatusUpdateFactory.newLatLngZoom(latLng,20);
                baiduMap.animateMapStatus(msu);
                return true;
            case R.id.btn_showcellinfo:
                Thread thread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        request(1);
                        ArrayList<BSinfo> bSinfos=addBS(bsjson);
                        refreshBasestation(bSinfos);
                    }
                });
                thread.start();


                return true;
            case R.id.btn_showsignal:
                Thread thread1=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        request(2);
                        siArray=addSig(signaljson);
                    }
                });
                thread1.start();

                return true;
        }

        return  super.onOptionsItemSelected(item);
    }

    public void initMarker(double lanti,double longi,int lac,int cid){
        mbitmapdes =BitmapDescriptorFactory.fromResource(R.drawable.img3);

        LatLng markerlatlng=new LatLng(lanti,longi);

        CoordinateConverter cc=new CoordinateConverter();
        cc.from(CoordinateConverter.CoordType.GPS);
        cc.coord(markerlatlng);
        LatLng desmarker=cc.convert();

        OverlayOptions overlayOptions=new MarkerOptions().position(desmarker).icon(mbitmapdes).zIndex(5);
        marker=(Marker)baiduMap.addOverlay(overlayOptions);
        Bundle bundle=new Bundle();
        bundle.putInt("lac",lac);
        bundle.putInt("cid",cid);
        marker.setExtraInfo(bundle);

    }
    public void initNeighbour(double lanti,double longi,int lac,int cid){
        BitmapDescriptor mbitmapneigh=BitmapDescriptorFactory.fromResource(R.drawable.img2);

        LatLng markerNeighbour=new LatLng(lanti,longi);
        CoordinateConverter cc=new CoordinateConverter();
        cc.from(CoordinateConverter.CoordType.GPS);
        cc.coord(markerNeighbour);
        LatLng desmarkerNeighbour=cc.convert();

        OverlayOptions overlayOptions=new MarkerOptions().position(desmarkerNeighbour).icon(mbitmapneigh).zIndex(5);
        Marker marker2=(Marker) baiduMap.addOverlay(overlayOptions);
        Bundle bundle=new Bundle();
        bundle.putInt("lac",lac);
        bundle.putInt("cid",cid);
        marker2.setExtraInfo(bundle);
    }

    //display all base station
    public void refreshBasestation(ArrayList<BSinfo> BSinfos) {
        baiduMap.clear();

        GsmCellLocation gsm=(GsmCellLocation) tm.getCellLocation();
        final int curlac=gsm.getLac();
        final int curcid=gsm.getCid();

        for (int i=0;i<BSinfos.size();i++){
            double blanti=BSinfos.get(i).getBlanti();
            double blongi=BSinfos.get(i).getBlongi();
            int lac=BSinfos.get(i).getLac();
            int cid=BSinfos.get(i).getCid();

            if(lac==curlac&&cid==curcid){
                initMarker(blanti,blongi,lac,cid);
            } else {
                initNeighbour(blanti,blongi,lac,cid);
            }

        }
    }

    public void updatesignaldata(ArrayList<SigInfo> si,int lac,int cid){
        for (int i=0;i<si.size();i++){
            int blac=si.get(i).getLac();
            int bcid=si.get(i).getCid();

            if(blac==lac&&bcid==cid){
                double ulanti=si.get(i).getUlanti();
                double ulongi=si.get(i).getUlongi();
                LatLng latLng=new LatLng(ulanti,ulongi);
                double level=si.get(i).getStrength();

                int fillcolor=Color.argb(50,0,255,0);
                if (level<-90&&level>=-95){
                    fillcolor=Color.argb(50,255,153,0);
                }
                if (level<-95&&level>=-100){
                    fillcolor=Color.argb(50,255,76,0);
                }
                if (level<-100){
                    fillcolor=Color.argb(50,255,0,0);
                }

                CircleOptions options =new CircleOptions().center(latLng).fillColor(fillcolor).radius(20).visible(true).zIndex(3);
                baiduMap.addOverlay(options);
            }
        }
    }
    public void DeleteSigMarker(int lac,int cid){
        final int lac2=lac;
        final int cid2=cid;
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                request(1);
                ArrayList<BSinfo> bSinfos=addBS(bsjson);
                refreshBasestation(bSinfos);
                updatesignaldata(siArray,lac2,cid2);
            }
        });
        thread.start();
    }


    public ArrayList<BSinfo> addBS(String json){
        ArrayList<BSinfo> BSinfos=new ArrayList<BSinfo>();
        try {
            JSONArray js=new JSONArray(json);

            for(int i=0;i<js.length();i++){
                JSONObject ob=new JSONObject(js.get(i).toString());
                double blanti=ob.getDouble("blanti");
                double blongi=ob.getDouble("blongi");
                int lac=ob.getInt("lac");
                int cid=ob.getInt("cid");
                BSinfo bSinfo=new BSinfo(blanti,blongi,lac,cid);
                BSinfos.add(bSinfo);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return BSinfos;
    }

    public ArrayList<SigInfo> addSig(String json){
        ArrayList<SigInfo> sigInfos=new ArrayList<SigInfo>();

        try {
            JSONArray js=new JSONArray(json);
            for(int i=0;i<js.length();i++){
                JSONObject ob=new JSONObject(js.get(i).toString());
                double ulanti=ob.getDouble("ulanti");
                double ulongi=ob.getDouble("ulongi");
                int lac=ob.getInt("lac");
                int cid=ob.getInt("cid");
                double strength=ob.getDouble("strength");
                SigInfo si=new SigInfo(ulanti,ulongi,lac,cid,strength);
                sigInfos.add(si);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sigInfos;
    }

    public class myPhoneStateManager extends PhoneStateListener{

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            GsmCellLocation gsm=(GsmCellLocation) tm.getCellLocation();
            final int lac=gsm.getLac();
            final int cid=gsm.getCid();
            int signal=signalStrength.getGsmSignalStrength()*2-113;

            if (mylati!=0&&mylongi!=0) {
                String json = "{\"ulanti\":" + mylati + ",\"ulongi\":" + mylongi + ",\"lac\":" + lac + ",\"cid\":" + cid + ",\"strength\":" + signal + ",}";
                Log.i("sendjson", json);
                sendjson(json);
            }
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);
            int phonetype=tm.getPhoneType();

            if (phonetype==TelephonyManager.PHONE_TYPE_GSM) {
                GsmCellLocation gsm=(GsmCellLocation) location;
                final int lac=gsm.getLac();
                final int cid=gsm.getCid();

                Thread thread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject json=httpcell(lac,cid);
                        sendbsjson(getbsjson(json,lac,cid));
                    }
                });
                thread.start();
            }

            if (phonetype==TelephonyManager.PHONE_TYPE_CDMA) {
                CdmaCellLocation cdma=(CdmaCellLocation) location;
                final int lac=cdma.getBaseStationId();
                final int cid=cdma.getNetworkId();

                Thread thread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject json=httpcell(lac,cid);
                    }

                });
                thread.start();
            }
        }

    }


    //obtain json object of http request
    public JSONObject httpcell(int lac,int cid){
        String imsi=tm.getSubscriberId();
        String mcc=imsi.substring(0,3);
        String mnc=imsi.substring(3,5);

        String path=new String();
        JSONObject json=null;
        path="http://api.cellocation.com/cell/?mcc="+mcc+"&mnc="+mnc+"&lac="+lac+"&ci="+cid+"&coord=bd09&output=json";
        //path="http://api.gpsspg.com/bs/?oid="+oid+"&key="+key+"&type=gsm&bs="+mcc+","+mnc+","+lac+","+cid+"&output=json";

        URL url= null;
        try {
            url = new URL(path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            HttpURLConnection conn=(HttpURLConnection) url.openConnection();
            conn.addRequestProperty("user-agent","User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36");
            if (conn.getResponseCode()==200) {
                InputStream in=conn.getInputStream();
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                byte[] buff=new byte[1024*8];
                byte[] data=null;
                int length=0;
                while ((length=in.read(buff))>0) {
                    baos.write(buff,0,length);
                }
                data=baos.toByteArray();

                receive=new String(data,"UTF-8"); //received message

                in.close();
                baos.close();
            }
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            json=new JSONObject(receive); //convert received message to json format
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    //insert json information into database
    public String getbsjson(JSONObject json,int lac,int cid){
        double lat = 0.0;
        double lon = 0.0;
        int s=-1;
        String bsjsontoserver="";
        try {
            s=json.getInt("errcode");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (s==0) {
            try {
                lat = Double.parseDouble(json.get("lat").toString());
                lon = Double.parseDouble(json.get("lon").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (lac != 0 && cid != 0) {
                bsjsontoserver="{\"lat\":"+lat+", \"lon\":"+lon+", \"lac\":"+lac+" , \"cid\":"+cid+"}";
            }
            return bsjsontoserver;
        } else  return null;


    }



    public void sendjson(String json){
        final String jsonstr=json;
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result="";
                    URL url=new URL("http://bobology.tech/bscollector/jsoncollect");
                    HttpURLConnection conn=(HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(5000);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Charset", "UTF-8");
                    DataOutputStream os=new DataOutputStream(conn.getOutputStream());

                    os.writeBytes(jsonstr);
                    os.flush();
                    os.close();

                    if (conn.getResponseCode()==200){
                        InputStreamReader is=new InputStreamReader(conn.getInputStream());
                        BufferedReader reader=new BufferedReader(is);
                        String len;
                        while ((len=reader.readLine())!=null){
                            result =result+len;
                        }
                        Log.i("send json result: ", result);

                        reader.close();
                        is.close();


                    }   else{
                        Log.i("send json result: ", conn.getResponseCode()+"");

                    }
                    conn.disconnect();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

    public void sendbsjson(String json){
        final String jsonstr=json;
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result="";
                    URL url=new URL("http://bobology.tech/bscollector/bscollect");
                    HttpURLConnection conn=(HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(5000);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Charset", "UTF-8");
                    DataOutputStream os=new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(jsonstr);
                    os.flush();
                    os.close();
                    if (conn.getResponseCode()==200){
                        InputStreamReader is=new InputStreamReader(conn.getInputStream());
                        BufferedReader reader=new BufferedReader(is);
                        String len;
                        while ((len=reader.readLine())!=null){
                            result =result+len;
                        }
                        Log.i("send bs json result: ", result);

                        reader.close();
                        is.close();
                    }   else{
                        Log.i("send bs json result: ", conn.getResponseCode()+"");

                    }
                    conn.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void request(int type){

        final int typeno=type;
        Log.i("start request"," ");
        try {
            String result="";
            URL url=null;
            if (type==1){
                url=new URL("http://bobology.tech/bscollector/bsretrieve");
            }

            if (type==2){
                url=new URL("http://bobology.tech/bscollector/signalretrieve");
            }

            HttpURLConnection conn=(HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");


            if (conn.getResponseCode()==200){
                InputStreamReader is=new InputStreamReader(conn.getInputStream());
                BufferedReader reader=new BufferedReader(is);
                String len;
                while ((len=reader.readLine())!=null){
                    result =result+len;
                }
                if (type==1){
                    bsjson=result;
                }
                if (type==2){
                    signaljson=result;
                }

                Log.i("http:", " "+result);

                reader.close();
                is.close();


            }   else{
                Log.i("http:", " "+conn.getResponseCode());

            }
            conn.disconnect();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }







}
