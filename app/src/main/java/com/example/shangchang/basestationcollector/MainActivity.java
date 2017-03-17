package com.example.shangchang.basestationcollector;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.shangchang.basestationcollector.DB.DBhelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private TextView tv_sp;
    private TextView tv_IMIS;
    private TextView tv_MCC;
    private TextView tv_MNC;
    private TextView tv_phonetype;
    private TextView tv_signalstrength;
    private TextView tv_nettype;
    private TextView tv_cellid;
    private TextView tv_gps;
    private TextView tv_cellinfo;
    MyPhoneStateManager myPhoneStateManager;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Button btn_userposipage;
    private Button btn_bscollected;
    private Button btn_baidumaptest;
    private double lati=0;
    private double longi=0;
    private int[] al;
    private CellListener cellListener;
    private Location l;
    private int phonetype;
    private int nettype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tv_sp = (TextView) findViewById(R.id.tv_sp);
        tv_IMIS = (TextView) findViewById(R.id.tv_IMIS);
        tv_MCC = (TextView) findViewById(R.id.tv_MCC);
        tv_MNC = (TextView) findViewById(R.id.tv_MNC);
        tv_phonetype = (TextView) findViewById(R.id.tv_phonetype);
        tv_signalstrength = (TextView) findViewById(R.id.tv_signalstrength);
        tv_nettype = (TextView) findViewById(R.id.tv_nettype);
        tv_gps = (TextView) findViewById(R.id.tv_gps);
        tv_cellid = (TextView) findViewById(R.id.tv_cellid);
        tv_cellinfo=(TextView) findViewById(R.id.tv_cellinfo);

        btn_baidumaptest = (Button) findViewById(R.id.btn_baidumaptest);

        Context context = this;

        cellListener = new CellListener();
        cellListener.start(context);

        myPhoneStateManager = new MyPhoneStateManager();
        cellListener.listen(myPhoneStateManager, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                | PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_CELL_INFO);



        String operator = cellListener.getMCCandMNC();
        int imsi = Integer.parseInt(operator.substring(0, 5));
        tv_sp.append(cellListener.getNetOperatorName() + " ");
        findMCCandMNC(imsi);//find service provider

        tv_IMIS.append(cellListener.getIMSI());//find IMIS

        tv_MCC.append(operator.substring(0, 3));//find MCC

        tv_MNC.append(operator.substring(3, 5));//find MNC

        phonetype = cellListener.getPhonetype();//find phone type
        findphonetype(phonetype);

        nettype = cellListener.getnettype();//find network type
        findnettype(nettype);

        findneighbour();

        boolean b = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        Log.i("create location request", String.valueOf(b));

        boolean c = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        Log.i("create location request", String.valueOf(c));


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);//Ffind gps
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("location ", "success");
                lati=location.getLatitude();
                longi=location.getLongitude();
                tv_gps.setText("GPS:" + "\n" + "Latitude:" + lati + " Longtitude:" + longi);
            }


            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.INTERNET}, 10);
            }
            return;
        } else {
            locationManager.requestLocationUpdates("gps", 1000, 1, locationListener);

            String provider=locationManager.GPS_PROVIDER;
            l=locationManager.getLastKnownLocation(provider);
            if (l!=null) {tv_gps.setText("GPS:" + "\n" + "Latitude:" + l.getLatitude() + " Longtitude:" + l.getLongitude());}
        }


        btn_baidumaptest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, com.example.shangchang.basestationcollector.Baidumaptest.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
                }
        }

    }


    public void findphonetype(int phonetype) {
        switch (phonetype) {
            case 0:
                tv_phonetype.append("unkown phonetype");
                break;
            case 1:
                tv_phonetype.append("GSM");
                break;
            case 2:
                tv_phonetype.append("CDMA");
                break;
            case 3:
                tv_phonetype.append("SIP");
                break;
            default:
                break;
        }
    }

    public void findMCCandMNC(int MCCandMNC) {
        switch (MCCandMNC) {
            case 46000:
                tv_sp.append("China Mobile");
                break;
            case 46001:
                tv_sp.append("China Unicom");
                break;
            case 46002:
                tv_sp.append("China Mobile");
                break;
            case 46003:
                tv_sp.append("China Telecom");
                break;
            case 45502:
                tv_sp.append("Macau Telecom");
                break;
            default:
                break;
        }
    }

    public void findnettype(int nettype) {
        switch (nettype) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                tv_nettype.append("1xRTT");
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                tv_nettype.append("EVDO_A");
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                tv_nettype.append("EVDO_B");
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                tv_nettype.append("EVDO_0");
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                tv_nettype.append("CDMA");
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                tv_nettype.append("EDGE");
                break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                tv_nettype.append("GPRS");
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                tv_nettype.append("HSPA");
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                tv_nettype.append("HSDPA");
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                tv_nettype.append("HSPAP");
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                tv_nettype.append("HSUPA");
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                tv_nettype.append("UMTS");
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                tv_nettype.append("LTE");
                break;
            default:
                break;
        }
    }

    public class MyPhoneStateManager extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if (phonetype==TelephonyManager.PHONE_TYPE_CDMA){
                tv_signalstrength.setText("Signal Strength:" + String.valueOf(signalStrength.getCdmaDbm() + "dBm"));
            }
            if (phonetype==TelephonyManager.PHONE_TYPE_GSM) {
                tv_signalstrength.setText("Signal Strength:" + String.valueOf(
                        signalStrength.getGsmSignalStrength()*2-113
                        + "dBm"));
            }
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);
            if (phonetype==TelephonyManager.PHONE_TYPE_CDMA) {
                CdmaCellLocation cdma = (CdmaCellLocation) location;
                tv_cellid.setText("CellLac:" + cdma.getNetworkId() + "\n" + "CellId:" + cdma.getBaseStationId()
                +"\n"+"System ID:"+cdma.getSystemId()
                );

            }

            if (phonetype==TelephonyManager.PHONE_TYPE_GSM) {
                GsmCellLocation gsm=(GsmCellLocation) location;
                tv_cellid.setText("CellLac:" + gsm.getLac() + "\n" + "CellId:" + gsm.getCid() +
                        "\n" + "PSC:"+gsm.getPsc());
            }


        }

        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            super.onCellInfoChanged(cellInfo);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else
        locationManager.removeUpdates(locationListener);
    }

    private void findneighbour(){
        StringBuffer sb=new StringBuffer();
        List<CellInfo> neighbour=cellListener.gettm().getAllCellInfo();
        TreeMap<Integer,Integer> map=new TreeMap();
        int count=0;
        if (nettype==TelephonyManager.NETWORK_TYPE_HSUPA){
            for (CellInfo ci : neighbour) {
                CellIdentityWcdma wcdma=((CellInfoWcdma)ci).getCellIdentity();
                int cid=wcdma.getCid();
                int lac=wcdma.getLac();
                map.put(cid,lac);
            }


            for (int key:map.keySet()) {
                sb.append("cid:"+key+" "+"lac:"+map.get(key)+"\n");
            }

        }
        if (phonetype==TelephonyManager.PHONE_TYPE_CDMA) {


            for (CellInfo ci:neighbour) {
                CellIdentityCdma cdma=((CellInfoCdma)ci).getCellIdentity();
                int cid=cdma.getBasestationId();
                int lac=cdma.getNetworkId();
                map.put(cid,lac);
            }

            for (int key:map.keySet()) {
                sb.append("cid:"+key+" "+"lac:"+map.get(key)+"\n");
            }

        }
        tv_cellinfo.append(sb);
    }

}
