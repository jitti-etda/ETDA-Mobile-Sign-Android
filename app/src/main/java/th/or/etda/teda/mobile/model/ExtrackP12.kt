package th.or.etda.teda.mobile.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.security.PrivateKey

@Parcelize
data class ExtrackP12(
    val chains : String,
    val cert : String,
    val privateKey : PrivateKey
): Parcelable
