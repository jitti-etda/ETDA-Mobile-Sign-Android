package th.or.etda.teda.mobile.ui.sign

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SignCache(
    val name : String,
    val privateKey : String
): Parcelable
