package th.or.etda.teda.mobile.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "cert_table")
data class Certificate(
    @PrimaryKey
    @ColumnInfo(name = "cert_name") var certName: String,
    @ColumnInfo(name = "cert_ca") var certCa: String?,
    @ColumnInfo(name = "cert_chains") var certChains: String?
): Parcelable {

    @Ignore
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(certName)
        parcel.writeString(certCa)
        parcel.writeString(certChains)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Certificate> {
        override fun createFromParcel(parcel: Parcel): Certificate {
            return Certificate(parcel)
        }

        override fun newArray(size: Int): Array<Certificate?> {
            return arrayOfNulls(size)
        }
    }
}
