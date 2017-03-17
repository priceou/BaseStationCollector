package com.example.shangchang.basestationcollector.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by shangchang on 2016/10/22.
 */
public class DBhelper extends SQLiteOpenHelper{

    private static final String DB_NAME="BS_info";
    private static final String CREATE_TABLE="create table basestation" +
            "(_id integer primary key autoincrement,cid integer,lac integer,latitude text,longitude text)";
    private static final String CREATE_TABLE2="create table signalstrength"+
            "(_sid integer primary key autoincrement, strengthlevel integer, slatitude text, slongitude text,bid integer,"+
            "foreign key(bid) references basestation(id))";
    private static DBhelper instance=null;

    private DBhelper(Context c){
        super(c,DB_NAME,null,3);
    }

    public static DBhelper getInstance(Context c){
        if (instance==null){
            instance=new DBhelper(c);
        }
        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_TABLE2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insert(ContentValues values) {

    }

}
