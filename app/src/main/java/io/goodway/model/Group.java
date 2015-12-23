package io.goodway.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import io.goodway.navitia_android.Address;

/**
 * Created by root on 6/13/15.
 */
public class Group implements Parcelable {

    private String name, description, avatar;
    private int id;
    public static final Creator CREATOR =
            new Creator() {
                public Group createFromParcel(Parcel in) {
                    return new Group(in);
                }

                public Group[] newArray(int size) {
                    return new Group[size];
                }
            };

    public Group(int id, String name, String description, String avatar){
        this.id = id;
        this.name = name;
        this.description = description;
        this.avatar = avatar;
    }

    public Group(Parcel in){
        readFromParcel(in);
    }

    public int getId(){return id;}

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public String getAvatar(){return avatar;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(avatar);
    }
    private void readFromParcel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        avatar = in.readString();
    }
}
