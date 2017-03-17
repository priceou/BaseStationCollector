package com.example.shangchang.basestationcollector.DB;

/**
 * Created by shangchang on 2017/3/6.
 */

public class SigInfo {
    double ulanti;
    double ulongi;
    int lac;
    int cid;
    double strength;

    public SigInfo(double ulanti,double ulongi,int lac,int cid,double strength){
        this.ulanti=ulanti;
        this.ulongi=ulongi;
        this.lac=lac;
        this.cid=cid;
        this.strength=strength;
    }

    public double getUlanti(){
        return this.ulanti;
    }

    public double getUlongi(){
        return this.ulongi;
    }

    public int getLac(){
        return this.lac;
    }

    public int getCid(){
        return this.cid;
    }

    public double getStrength(){
        return this.strength;
    }

}
