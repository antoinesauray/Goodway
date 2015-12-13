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

    private String fname, lname, mail;
    private int id, image;
    private boolean sharesHome, sharesWork;
    private Double homeLat, homeLon, workLat, workLon;
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

    public User(String fname, String lname, boolean sharesHome, boolean sharesWork, boolean friend){
        this.id = -1;
        this.fname = fname;
        this.lname = lname;
        this.sharesHome = sharesHome;
        this.sharesWork = sharesWork;
        homeLat=null;
        homeLon=null;
        workLat=null;
        workLon=null;
        this.friend=friend;
        Log.d("sharesHome="+sharesHome, "sharesHome user="+fname+" "+lname);
    }
    public User(int id, String fname, String lname, boolean sharesHome, boolean sharesWork, boolean friend){
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.sharesHome = sharesHome;
        this.sharesWork = sharesWork;
        homeLat=null;
        homeLon=null;
        workLat=null;
        workLon=null;
        this.friend=friend;
        Log.d("sharesHome="+sharesHome, "sharesHome user="+fname+" "+lname);
    }

    public User(String fname, String lname, String mail, boolean sharesHome, boolean sharesWork, boolean friend){
        this.id = -1;
        this.fname = fname;
        this.lname = lname;
        this.mail = mail;
        this.sharesHome = sharesHome;
        this.sharesWork = sharesWork;
        homeLat=null;
        homeLon=null;
        workLat=null;
        workLon=null;
        this.friend=friend;
        Log.d("sharesHome="+sharesHome, "sharesHome user="+fname+" "+lname);
    }

    public User(int id, String fname, String lname, String mail, boolean sharesHome, boolean sharesWork, Double homeLat, Double homeLon, Double workLat, Double workLon, boolean friend){
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.mail = mail;
        this.sharesHome = sharesHome;
        this.sharesWork = sharesWork;
        this.homeLat=homeLat;
        this.homeLon=homeLon;
        this.workLat=workLat;
        this.workLon=workLon;
        this.friend=friend;
        Log.d("this.homeLat" + this.homeLat, "this.homeLon=" + this.homeLon);
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

    public boolean sharesHome(){return sharesHome;}

    public boolean sharesWork(){return sharesWork;}

    public void setHome(Address addr){
        this.homeLat = addr.getLatitude();
        this.homeLon = addr.getLongitude();
    }

    public void setWork(Address addr){
        this.workLon = addr.getLatitude();
        this.workLon = addr.getLongitude();
    }

    public boolean isFriend(){return friend;}

    public void setSharesHome(boolean sharesHome){this.sharesHome=sharesHome;}

    public void setSharesWork(boolean sharesWork){this.sharesWork=sharesWork;}

    public LatLng getHome(){return new LatLng(homeLat, homeLon);}

    public LatLng getWork(){return new LatLng(workLat, workLon);}

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
        dest.writeByte((byte) (sharesHome ? 1 : 0));
        dest.writeByte((byte) (sharesWork ? 1 : 0));
        dest.writeByte((byte) (friend ? 1 : 0));
        if(homeLat!=null && homeLon!=null && workLat!=null && workLon!=null) {
            dest.writeDouble(homeLat);
            dest.writeDouble(homeLon);
            dest.writeDouble(workLat);
            dest.writeDouble(workLon);
        }
    }
    private void readFromParcel(Parcel in) {
        id = in.readInt();
        fname = in.readString();
        lname = in.readString();
        mail = in.readString();
        image = in.readInt();
        sharesHome = in.readByte() != 0;
        sharesWork = in.readByte() != 0;
        friend = in.readByte() != 0;
        homeLat = in.readDouble();
        homeLon = in.readDouble();
        workLat = in.readDouble();
        workLon = in.readDouble();
    }
}
