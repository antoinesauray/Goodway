package io.goodway.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by root on 6/13/15.
 */
public class User implements Parcelable {

    private String fname, lname, mail;
    private int id, image;

    public static final Creator CREATOR =
            new Creator() {
                public User createFromParcel(Parcel in) {
                    return new User(in);
                }

                public User[] newArray(int size) {
                    return new User[size];
                }
            };

    public User(String fname, String lname){
        this.id = -1;
        this.fname = fname;
        this.lname = lname;
    }
    public User(int id, String fname, String lname){
        this.id = id;
        this.fname = fname;
        this.lname = lname;
    }

    public User(String fname, String lname, String mail){
        this.id = -1;
        this.fname = fname;
        this.lname = lname;
        this.mail = mail;
    }

    public User(int id, String fname, String lname, String mail){
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.mail = mail;
    }

    public User(Parcel in){
        readFromParcel(in);
    }

    public int getId(){return id;}

    public String getMail(){return mail;}

    public String getFirstName(){return fname;}

    public String getLastName(){return lname;}

    public String getName(){
        return fname+" "+lname;
    }

    @Override
    public int describeContents() {
        return 3;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(fname);
        dest.writeString(lname);
        dest.writeString(mail);
        dest.writeInt(image);
    }
    private void readFromParcel(Parcel in) {
        id = in.readInt();
        fname = in.readString();
        lname = in.readString();
        mail = in.readString();
        image = in.readInt();
    }
}
