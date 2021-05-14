package th.or.etda.teda.mobile.ui.restorekey.password

import androidx.lifecycle.ViewModel
import th.or.etda.teda.mobile.common.AESHelper
import th.or.etda.teda.mobile.ui.importkey.ImportHelper
import java.io.InputStream


class RestoreKeyPasswordViewModel : ViewModel() {


    //    var dataDecrypt = MutableLiveData<ByteArray>()
    fun decrypt(inputStream: InputStream, password: String): ByteArray? {
        var data = ImportHelper.convertStreamToString(inputStream)
        if (data != null) {
            var bArray = AESHelper.decryptAES(data, password)
            if (bArray != null) {
                if (bArray.isEmpty()) {
                    return null
                }

//                for (b in bArray) {
//                    if (b.toInt() == 0) {
//                        return null
//                    }
//                }
                return bArray
            }

        } else {
            return null
        }
        return null
    }


}
