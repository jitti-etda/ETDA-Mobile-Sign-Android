package th.or.etda.teda.mobile.ui.restorekey

import android.content.Context
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.SingleLiveEvent
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.data.CertificateRepository
import th.or.etda.teda.mobile.ui.backupkey.googledrive.DriveServiceHelper
import th.or.etda.teda.mobile.ui.backupkey.googledrive.GoogleDriveFileHolder
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class RestoreKeyViewModel(val repository: CertificateRepository) : ViewModel() {


    var fileGoogleDriveLive = SingleLiveEvent<List<GoogleDriveFileHolder>>()
    var folderGoogleDriveLive = SingleLiveEvent<List<GoogleDriveFileHolder>>()
    var isSuccess = SingleLiveEvent<Boolean>()
    val showLoading = ObservableBoolean()

    fun getFileBackup(context: Context, mDriveServiceHelper: DriveServiceHelper) {
        showLoading.set(true)
        viewModelScope.launch {

            mDriveServiceHelper.searchFolder(context.getString(R.string.app_name))
//            mDriveServiceHelper.searchFolder("")
                ?.addOnCompleteListener { foldersResult ->

                    if (foldersResult.isSuccessful) {
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
                        isSuccess.value = true
                    }else{
                        isSuccess.value =  false
                    }

                }


        }

    }


    var downloadSuccess = MutableLiveData<InputStream>()
    suspend fun downloadFile(
        context: Context,
        mDriveServiceHelper: DriveServiceHelper, file: GoogleDriveFileHolder
    ) {


        viewModelScope.launch {


            mDriveServiceHelper.downloadFile(file.id)?.addOnCompleteListener {


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



}
