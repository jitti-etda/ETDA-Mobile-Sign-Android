package th.or.etda.teda.mobile.ui.importkey.password

import android.content.Context
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.SingleLiveEvent
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.data.CertificateRepository
import th.or.etda.teda.mobile.model.ExtrackP12
import th.or.etda.teda.mobile.ui.backupkey.googledrive.DriveServiceHelper
import th.or.etda.teda.mobile.ui.importkey.ImportHelper
import th.or.etda.teda.mobile.util.UtilApps
import java.io.File
import java.io.IOException
import java.security.*
import java.security.cert.X509Certificate
import javax.crypto.Cipher


class ImportKeyPasswordViewModel(val repository: CertificateRepository) : ViewModel() {


    val caUri = MutableLiveData<Uri>()
    val password = MutableLiveData<String>()
    var isSuccess = SingleLiveEvent<Boolean>()
    val showLoading = ObservableBoolean()

    fun addCertificate(certificate: Certificate) {
        viewModelScope.launch {
            repository.insert(certificate)
        }
    }



    var extractP12Success = SingleLiveEvent<ExtrackP12>()
    fun extractP12(
        context: Context,
        password: String,
        name: String
    ) {

        showLoading.set(true)
        viewModelScope.launch {
            try {
                caUri.value?.let { it ->
                    println("path is => ${caUri.value}")

                    var cert = ImportHelper.extrackP12(
                        context,
                        it,
                        password,
                        name
                    )
                    showLoading.set(false)
                    extractP12Success.value = cert
                }


            } catch (e: IOException) {
                e.printStackTrace()
                UtilApps.alertDialog(context,"wrong password or corrupted file")
//                Toast.makeText(context, "wrong password or corrupted file", Toast.LENGTH_SHORT)
//                    .show()

            }
        }

    }





    var uploadSuccess = MutableLiveData<Boolean>()

    fun uploadFile(
        context: Context,
        mDriveServiceHelper: DriveServiceHelper,
        filePathBackup: File
    ) {
        Log.i("upload", "start")
        showLoading.set(true)
        viewModelScope.launch {
            mDriveServiceHelper.createFolderIfNotExist(context.getString(R.string.app_name), null)
                ?.addOnCompleteListener {
                    if(it.isSuccessful){
                        var folderID = it.result.id
                        mDriveServiceHelper.uploadFile(filePathBackup, "text/plain", folderID)
                            ?.addOnCompleteListener {
                                showLoading.set(false)
                                uploadSuccess.value = true
                                Log.i("upload", "success")
                            }
                        isSuccess.value = true
                    }else{
                        isSuccess.value = false
                    }

                }
        }
    }

}
