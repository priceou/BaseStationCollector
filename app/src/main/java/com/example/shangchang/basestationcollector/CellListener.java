package com.example.shangchang.basestationcollector;

import android.content.Context;
import android.location.Location;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shangchang on 2016/9/4.
 */
public class CellListener extends PhoneStateListener{
    private TelephonyManager telephonyManager ;
    private List<CellInfo> cellInfos;
    private int signal;

    public void start(Context context){
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        cellInfos=telephonyManager.getAllCellInfo();

    }


    public String getMCCandMNC(){
        return telephonyManager.getNetworkOperator();
    }

    public String getNetOperatorName(){
        return telephonyManager.getNetworkOperatorName();
    }

    public String getIMSI(){
        return telephonyManager.getSubscriberId();
    }

    public  int getnettype(){
        return telephonyManager.getNetworkType();
    }

    public int getPhonetype(){
        return telephonyManager.getPhoneType();
    }

    public TelephonyManager gettm(){return telephonyManager;}

    public void listen(PhoneStateListener phoneStateListener,int event){
        telephonyManager.listen(phoneStateListener,event);
    }

    public void listen(PhoneStateListener phoneStateListener,int event1,int event2,int event3) {
        telephonyManager.listen(phoneStateListener,event1|event2|event3);
    }
}
