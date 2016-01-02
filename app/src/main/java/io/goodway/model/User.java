package io.goodway.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import io.goodway.R;

/**
 * Created by root on 6/13/15.
 */
public class User implements Parcelable {

    private String fname, lname, mail, avatar, city, token;
    private int id, title;
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


    public User(int id, String mail, String fname, String lname, String avatar, int title, String city, boolean friend){
        this.id = id;
        this.mail = mail;
        this.fname = fname;
        this.lname = lname;
        this.friend=friend;
        this.title = title;
        this.avatar = avatar;
        this.city = city;
    }
    public User(int id, String mail, String fname, String lname, String avatar, int title, String city, boolean friend, String token){
        this.id = id;
        this.mail = mail;
        this.fname = fname;
        this.lname = lname;
        this.friend=friend;
        this.title = title;
        this.avatar = avatar;
        this.city = city;
        this.token = token;
    }
    public User(int id, String fname, String lname, String avatar, int title, String city, boolean friend){
        this.id = id;
        this.mail = null;
        this.fname = fname;
        this.lname = lname;
        this.friend=friend;
        this.title = title;
        this.avatar = avatar;
        this.city = city;
    }

    public User(int id, String fname, String lname, String avatar, String mail, int title, boolean friend){
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.avatar = avatar;
        this.mail = mail;
        this.title = title;
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

    public String getToken(){return token;}

    public int getTitle(){return title;}

    public String getCity(){return city;}

    public String getTitle(Context context){
        switch (title){
            case 0:
                // Précurseur
                return context.getString(R.string.early_adopter);
            case 1:
                // Cofondateur
                return context.getString(R.string.cofounder);
            case 2:
                // Nouveau venu
                return context.getString(R.string.newcomer);
            default:
                return context.getString(R.string.newcomer);
        }
    }

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
        dest.writeInt(title);
        dest.writeString(city);
        dest.writeString(mail);
    }
    private void readFromParcel(Parcel in) {
        id = in.readInt();
        fname = in.readString();
        lname = in.readString();
        avatar = in.readString();
        friend = in.readByte() != 0;
        title = in.readInt();
        city = in.readString();
        mail = in.readString();
    }
}
