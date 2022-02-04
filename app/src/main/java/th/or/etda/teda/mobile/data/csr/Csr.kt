package th.or.etda.teda.mobile.data.csr

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "csr_table")
data class Csr(
    @PrimaryKey
    @ColumnInfo(name = "csr_name") var csrName: String,
    @ColumnInfo(name = "csr_key") var csrKey: String?,
    @ColumnInfo(name = "private_key") var privateKey: String?,
    @ColumnInfo(name = "sign_key") var signKey: String?,
    @ColumnInfo(name = "chains") var chains: String?,
    @ColumnInfo(name = "date") var date: String?,
    @ColumnInfo(name = "path") var path: String?
): Parcelable {

    @Ignore
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(csrName)
        parcel.writeString(csrKey)
        parcel.writeString(privateKey)
        parcel.writeString(signKey)
        parcel.writeString(chains)
        parcel.writeString(date)
        parcel.writeString(path)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Csr> {
        override fun createFromParcel(parcel: Parcel): Csr {
            return Csr(parcel)
        }

        override fun newArray(size: Int): Array<Csr?> {
            return arrayOfNulls(size)
        }
    }
}
