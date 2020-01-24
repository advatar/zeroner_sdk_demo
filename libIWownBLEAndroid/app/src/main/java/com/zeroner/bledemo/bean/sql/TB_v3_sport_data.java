package com.zeroner.bledemo.bean.sql;

import android.content.Context;
import android.util.Log;



import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by hzy on 2015/10/28.
 */
public class TB_v3_sport_data extends DataSupport implements Serializable {

    // 运动类型
    private int sport_type;
    // 数据来源
    private String data_from;
    // 消耗卡路里
    private double calorie;
    // 目标完成度
    private int complete_progress;
    // 年
    private int year;
    // 月
    private int month;
    // 日
    private int day;
    // 周
    private int week;
    // 开始时间
    private int start_time;
    // 结束时间
    private int end_time;
    // 数据详情
    private String detail_data;
    // 上传标志
    private int _uploaded;
    // 预留字段
    private int reserved;
    @Column(ignore = true)
    public int index;
    private long start_uxtime;
    private long end_uxtime;
    private String sportCode;

    public TB_v3_sport_data() {
    }

    public int getSport_type() {
        return sport_type;
    }

    public void setSport_type(int sport_type) {
        this.sport_type = sport_type;
    }

    public String getData_from() {
        return data_from;
    }

    public void setData_from(String data_from) {
        this.data_from = data_from;
    }

    public double getCalorie() {
        return calorie;
    }

    public void setCalorie(double calorie) {
        this.calorie = calorie;
    }

    public int getComplete_progress() {
        return complete_progress;
    }

    public void setComplete_progress(int complete_progress) {
        this.complete_progress = complete_progress;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getStart_time() {
        return start_time;
    }

    public void setStart_time(int start_time) {
        this.start_time = start_time;
    }

    public int getEnd_time() {
        return end_time;
    }

    public void setEnd_time(int end_time) {
        this.end_time = end_time;
    }

    public String getDetail_data() {
        return detail_data;
    }

    public void setDetail_data(String detail_data) {
        this.detail_data = detail_data;
    }

    public int get_uploaded() {
        return _uploaded;
    }

    public void set_uploaded(int _uploaded) {
        this._uploaded = _uploaded;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getEnd_uxtime() {
        return end_uxtime;
    }

    public void setEnd_uxtime(long end_uxtime) {
        this.end_uxtime = end_uxtime;
    }

    public String getSportCode() {
        return sportCode;
    }

    public void setSportCode(String sportCode) {
        this.sportCode = sportCode;
    }

    public long getStart_uxtime() {
        return start_uxtime;
    }

    public void setStart_uxtime(long start_uxtime) {
        this.start_uxtime = start_uxtime;
    }


    @Override
    public String toString() {
        return "TB_v3_sport_data{" +
                ", sport_type=" + sport_type +
                ", data_from='" + data_from + '\'' +
                ", calorie=" + calorie +
                ", complete_progress=" + complete_progress +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", week=" + week +
                ", start_time=" + start_time +
                ", end_time=" + end_time +
                ", detail_data='" + detail_data + '\'' +
                ", _uploaded=" + _uploaded +
                ", reserved=" + reserved +
                ", index=" + index +
                ", start_uxtime=" + start_uxtime +
                ", end_uxtime=" + end_uxtime +
                ", sportCode='" + sportCode + '\'' +
                '}';
    }
}
