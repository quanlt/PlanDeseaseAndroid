package com.quanlt.plantdisease

import android.os.Parcel
import android.os.Parcelable

data class Diagnosis(val codeName: String, val percent: Int, val imagePath: String) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readInt(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(codeName)
        writeInt(percent)
        writeString(imagePath)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Diagnosis> = object : Parcelable.Creator<Diagnosis> {
            override fun createFromParcel(source: Parcel): Diagnosis = Diagnosis(source)
            override fun newArray(size: Int): Array<Diagnosis?> = arrayOfNulls(size)
        }
    }
}