package th.or.etda.teda.mobile.ui.restorekey.password

import android.util.Base64
import androidx.lifecycle.ViewModel
import th.or.etda.teda.mobile.common.AESHelper
import th.or.etda.teda.mobile.common.CryptLib
import th.or.etda.teda.mobile.ui.importkey.ImportHelper
import java.io.*


class RestoreKeyPasswordViewModel : ViewModel() {


    //    var dataDecrypt = MutableLiveData<ByteArray>()
    fun decrypt(inputStream: InputStream, password: String): ByteArray? {
        var data = ImportHelper.convertStreamToString(inputStream)
        if (data != null) {
//            var bArray = AESHelper.decryptAES(data, password)
//            var bArray = AesBase64Wrapper.getInstance().decodeAndDecrypt(data,password);

            var bArray: ByteArray;
            val cryptLib = CryptLib()
            try {
                var aaa = cryptLib.decryptCipherText(data, password)
                bArray = Base64.decode(aaa, Base64.DEFAULT)
            } catch (e: Exception) {
                return null
            }
//            val bArray: ByteArray = text.encodeToByteArray()
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


    fun readTxt(file: File): String {
        val text = StringBuilder()

        try {
            val br = BufferedReader(FileReader(file))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                text.append(line)
                text.append('\n')
            }
            br.close()
        } catch (e: IOException) {
            //You'll need to add proper error handling here
            e.printStackTrace()
        }
        return text.toString()
    }
}
