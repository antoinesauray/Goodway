package io.goodway.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import io.goodway.navitia_android.Address;

/**
 * Created by root on 6/13/15.
 */
public class User implements Parcelable {

    private String fname, lname, mail, avatar;
    private int id;
    private boolean friend;

    public static final Creator CREATOR =
            new Creator() {
                public User createFromParcel(Parcel in) {
                    return new User(in);
                }

                public User[] newArray(int size) {
                    return new User[size];
                }
            };


    public User(int id, String fname, String lname, String avatar, boolean friend){
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.friend=friend;
        this.avatar = avatar;
    }


    public User(int id, String fname, String lname, String avatar, String mail, boolean friend){
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.avatar = avatar;
        this.mail = mail;
        this.friend=friend;
    }

    public User(Parcel in){
        readFromParcel(in);
    }

    public int getId(){return id;}

    public String getMail(){return mail;}

    public String getFirstName(){return fname;}

    public String getLastName(){return lname;}

    public String getName(){return fname+" "+lname;}

    public String getAvatar(){return avatar;}

    public boolean isFriend(){return friend;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(fname);
        dest.writeString(lname);
        dest.writeString(avatar);
        dest.writeByte((byte) (friend ? 1 : 0));
        dest.writeString(mail);
    }
    private void readFromParcel(Parcel in) {
        id = in.readInt();
        fname = in.readString();
        lname = in.readString();
        avatar = in.readString();
        friend = in.readByte() != 0;
        mail = in.readString();
    }
}
