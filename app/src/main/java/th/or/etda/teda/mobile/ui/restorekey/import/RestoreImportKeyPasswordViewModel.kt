package th.or.etda.teda.mobile.ui.restorekey.import

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import th.or.etda.teda.mobile.common.SingleLiveEvent
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.data.CertificateRepository
import th.or.etda.teda.mobile.ui.importkey.ImportHelper
import th.or.etda.teda.mobile.util.UtilApps
import java.io.IOException
import java.security.PrivateKey


class RestoreImportKeyPasswordViewModel(val repository: CertificateRepository) : ViewModel() {

    var restoreSuccess = MutableLiveData<Boolean>()

    var extractP12Success = SingleLiveEvent<PrivateKey>()
    fun restore(context: Context, byteArray: ByteArray, password: String, nameRoot: String) {
        viewModelScope.launch {
            try {
                var name = nameRoot + "_" + UtilApps.timestampName()
                var cert = ImportHelper.restore(byteArray, password, name)

                addCertificate(cert)
                restoreSuccess.value = true
            } catch (e: IOException) {
                e.printStackTrace()
                UtilApps.alertDialog(context,"wrong password or corrupted file")
                restoreSuccess.value = false
            }
        }
    }
    fun restoreP12(context: Context, byteArray: ByteArray, password: String, nameRoot: String) {
        viewModelScope.launch {
            try {
                var name = nameRoot + "_" + UtilApps.timestampName()
//                var cert = ImportHelper.restore(byteArray, password, name)
                var private = ImportHelper.restoreP12(byteArray, password, name)
                extractP12Success.value = private

            } catch (e: IOException) {
                e.printStackTrace()
                UtilApps.alertDialog(context,"wrong password or corrupted file")
            }
        }
    }
    fun addCertificate(certificate: Certificate) {
        viewModelScope.launch {
            repository.insert(certificate)
        }
    }

}