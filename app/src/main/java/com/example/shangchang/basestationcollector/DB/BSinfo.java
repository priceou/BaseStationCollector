package com.example.shangchang.basestationcollector.DB;

/**
 * Created by shangchang on 2017/3/4.
 */

public class BSinfo {
    double blanti;
    double blongi;
    int lac;
    int cid;

    public BSinfo(double blanti,double blongi,int lac,int cid){
        this.blanti=blanti;
        this.blongi=blongi;
        this.lac=lac;
        this.cid=cid;
    }

    public double getBlanti(){
        return this.blanti;
    }

    public double getBlongi(){
        return this.blongi;
    }

    public int getLac(){
        return this.lac;
    }

    public int getCid(){
        return this.cid;
    }
}
