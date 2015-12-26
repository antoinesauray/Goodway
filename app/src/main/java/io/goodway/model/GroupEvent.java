package io.goodway.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import io.goodway.navitia_android.Address;

/**
 * Created by root on 6/13/15.
 */
public class GroupEvent implements Parcelable {

    private int id;
    private String name, avatar, s_time, e_time, url;
    private double latitude, longitude;


    public static final Creator CREATOR =
            new Creator() {
                public GroupEvent createFromParcel(Parcel in) {
                    return new GroupEvent(in);
                }

                public GroupEvent[] newArray(int size) {
                    return new GroupEvent[size];
                }
            };

    public GroupEvent(int id, String name, String url, String avatar, String s_time, String e_time, double latitude, double longitude){
        this.id = id;
        this.name = name;
        this.url = url;
        this.avatar = avatar;
        this.s_time = s_time;
        this.e_time = e_time;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GroupEvent(Parcel in){
        readFromParcel(in);
    }

    public String getName(){
        return name;
    }

    public String getUrl(){
        return url;
    }

    public String getS_time(){return s_time;}

    public String getE_time(){return e_time;}

    public String getAvatar(){return avatar;}

    public int getId(){return id;}

    public double getLatitude(){return latitude;}
    public double getLongitude(){return longitude;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(url);
        dest.writeString(s_time);
        dest.writeString(e_time);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
    private void readFromParcel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        url = in.readString();
        s_time = in.readString();
        e_time = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static String formatMonth(String month) {
        SimpleDateFormat monthParse = new SimpleDateFormat("MM");
        SimpleDateFormat monthDisplay = new SimpleDateFormat("MMMM");
        try {
            return monthDisplay.format(monthParse.parse(month));
        } catch (ParseException e) {
            e.printStackTrace();
            return month;
        }

    }

}
