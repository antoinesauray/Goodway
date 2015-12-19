package io.goodway.model;

/**
 * Created by antoine on 12/14/15.
 */

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by antoine on 12/5/15.
 */
public enum ContainerType implements Parcelable {
    navitia, carSharing, uber, bike;

    public static final Parcelable.Creator<ContainerType> CREATOR = new Creator<ContainerType>() {

        @Override
        public ContainerType[] newArray(int size) {
            return new ContainerType[size];
        }

        @Override
        public ContainerType createFromParcel(Parcel source) {
            return ContainerType.values()[source.readInt()];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.ordinal());
    }
}