package th.or.etda.teda.mobile.util

import android.util.Base64
import java.security.PublicKey
import java.security.cert.Certificate

object KeyUtil {

    fun PublicKey.toBase64(): String {
        return Base64.encodeToString(this.encoded, Base64.DEFAULT)
    }

    fun Certificate.toBase64(): String {
        return Base64.encodeToString(this.encoded, Base64.DEFAULT)
    }

}