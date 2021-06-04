package th.or.etda.teda.mobile.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SignedInfoBackup(
//    @SerializedName("result") val result
    val result : String,
    val description : String,
    val document :SignedDescription
): Parcelable {

    @Parcelize
    data class SignedDescription(
        @SerializedName("document_info") val documentInfo: DocumentInfo,
        @SerializedName("callback_url") val callbackUrl: String?,
        @SerializedName("other_info") val otherInfo: OtherInfo?,
        @SerializedName("signedinfo") val signedinfo: Signedinfo?

    ):Parcelable

    @Parcelize
    data class DocumentInfo(
        @SerializedName("document_type") val documentType: String,
        @SerializedName("ref_number") val refNumber: String,
        @SerializedName("document_name") val documentName: String,
        @SerializedName("document_description") val documentDescription: String,
        @SerializedName("business_type") val businessType: String,
    ): Parcelable

    @Parcelize
    data class OtherInfo(
        @SerializedName("info") val info: List<Info>?
    ): Parcelable

    @Parcelize
    data class Info(
        @SerializedName("key") val key: String?,
        @SerializedName("value") val value: String?
    ):Parcelable

    @Parcelize
    data class Signedinfo(
        @SerializedName("signatureId") val signatureId: String?,
        @SerializedName("signedInfo") val signedInfo: String?
    ):Parcelable
}
