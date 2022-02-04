package th.or.etda.teda.mobile.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SignedInfo(
    val result : String,
    val description : String,
    val request_token : String,
    val signedInfo :String
): Parcelable
