package io.goodway.model.gtfs;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Antoine Sauray on 5/25/2016.
 */
public class Schema implements Parcelable{

    public static final Creator<Schema> CREATOR = new Creator<Schema>() {
        @Override
        public Schema createFromParcel(Parcel in) {
            return new Schema(in);
        }

        @Override
        public Schema[] newArray(int size) {
            return new Schema[size];
        }
    };

    public String getName() {
        return name;
    }

    private String name;

    public Schema(String name){
        this.name=name;
    }

    public Schema(Parcel in){
        this.name=in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
    }
}
