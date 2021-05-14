package th.or.etda.teda.mobile.ui.restorekey

import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.AESHelper
import th.or.etda.teda.mobile.common.SingleLiveEvent
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.data.CertificateRepository
import th.or.etda.teda.mobile.ui.backupkey.googledrive.DriveServiceHelper
import th.or.etda.teda.mobile.ui.backupkey.googledrive.GoogleDriveFileHolder
import th.or.etda.teda.mobile.ui.importkey.ImportHelper
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class RestoreKeyViewModel(val repository: CertificateRepository) : ViewModel() {


    var fileGoogleDriveLive = SingleLiveEvent<List<GoogleDriveFileHolder>>()
    var folderGoogleDriveLive = SingleLiveEvent<List<GoogleDriveFileHolder>>()
    val showLoading = ObservableBoolean()


    fun getFileBackup(context: Context, mDriveServiceHelper: DriveServiceHelper) {
        showLoading.set(true)
        viewModelScope.launch {

            mDriveServiceHelper.searchFolder(context.getString(R.string.app_name))
                ?.addOnCompleteListener { foldersResult ->
                    var folders = foldersResult.result
                    if (folders.isNotEmpty()) {
                        for (i in 0 until folders.size) {
                            var folder = folders[i]
                            mDriveServiceHelper.queryFilesWithoutDelete(folder.id)
                                ?.addOnCompleteListener {
                                    showLoading.set(false)
                                    fileGoogleDriveLive.value = it.result
                                }
                        }

                    } else {
                        folderGoogleDriveLive.value = folders
                        showLoading.set(false)
                    }
                }
        }
//        viewModelScope.launch {
//            mDriveServiceHelper.queryFilesWithoutDelete(folders[0]?.id)?.addOnCompleteListener {
//                showLoading.set(false)
//                fileGoogleDriveLive.value = it.result
//            }
//        }
    }

//    var downloadSuccess = MutableLiveData<InputStream>()
//    fun downloadFile(mDriveServiceHelper: DriveServiceHelper, fileID: String) {
//        viewModelScope.launch {
//
//            mDriveServiceHelper.downloadFile(fileID)?.addOnCompleteListener {
//                downloadSuccess.value = it.result
//            }
//
//        }
//    }

    var downloadSuccess = MutableLiveData<InputStream>()
    suspend fun downloadFile(
        context: Context,
        mDriveServiceHelper: DriveServiceHelper, file: GoogleDriveFileHolder
    ) {


        viewModelScope.launch {


            mDriveServiceHelper.downloadFile( file.id)?.addOnCompleteListener {


                downloadSuccess.value = it.result

            }
        }
    }

    fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                inputStream.use { input ->
                    val outputStream = FileOutputStream(outputFile)
                    outputStream.use { output ->
                        val buffer = ByteArray(4 * 1024) // buffer size
                        while (true) {
                            val byteCount = input.read(buffer)
                            if (byteCount < 0) break
                            output.write(buffer, 0, byteCount)
                        }
                        output.flush()
                    }
                }
            }

        }

    }

//    var restoreSuccess = MutableLiveData<Boolean>()
//    fun restore(context: Context, byteArray: ByteArray, password: String, nameRoot: String) {
//        viewModelScope.launch {
//            try {
//                var name = nameRoot + "_" + System.currentTimeMillis() / 1000
//                var cert = ImportHelper.restore(byteArray, password, name)
//
//                addCertificate(cert)
//                restoreSuccess.value = true
//            } catch (e: IOException) {
//                e.printStackTrace()
//                Toast.makeText(context, "wrong password or corrupted file", Toast.LENGTH_SHORT)
//                    .show()
//                restoreSuccess.value = false
//            }
//        }
//    }

    //    var dataDecrypt = MutableLiveData<ByteArray>()
    fun decrypt(inputStream: InputStream, password: String): ByteArray? {
        var data = ImportHelper.convertStreamToString(inputStream)

//        viewModelScope.launch {
//
//
//            dataDecrypt.value = data?.let { RSACrypt2.decryptAES(it, password) };
//
//        }
        return data?.let { AESHelper.decryptAES(it, password) }
    }

    fun addCertificate(certificate: Certificate) {
        viewModelScope.launch {
            repository.insert(certificate)
        }
    }

}
