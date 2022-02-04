package th.or.etda.teda.mobile.ui.csr

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.security.PrivateKey

@Parcelize
data class CsrTemp(
    val path : String,
    val privateKey : PrivateKey
): Parcelable
