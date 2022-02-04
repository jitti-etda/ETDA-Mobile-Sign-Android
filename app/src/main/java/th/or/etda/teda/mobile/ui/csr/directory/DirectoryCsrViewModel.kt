package th.or.etda.teda.mobile.ui.csr.directory

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import th.or.etda.teda.mobile.data.csr.Csr
import th.or.etda.teda.mobile.data.csr.CsrRepository
import th.or.etda.teda.mobile.util.Constants
import java.io.File


class DirectoryCsrViewModel() : ViewModel() {

    private val fileList = ArrayList<DirectoryCsr>()

    val fileListLive = MutableLiveData<ArrayList<DirectoryCsr>>()




    suspend fun getFile(dir: File) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                getFileLocal(dir)
            }
            fileListLive.postValue(fileList)
        }
    }

    private fun getFileLocal(dir: File) {
        val listFile = dir.listFiles()
        if (listFile != null && listFile.isNotEmpty()) {
            for (file in listFile) {
                if (file.isDirectory) {
                    getFileLocal(file)
                } else {
                    if (file.name.endsWith(".csr")) {
                        val temp = DirectoryCsr(
                            file.name,
                            file.absolutePath
                        )

                        if (!fileList.contains(temp)) {
                            fileList.add(temp)
                        }

                    }
                }
            }

        }

    }

    suspend fun getFileCer(dir: File) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                getFileCerLocal(dir)
            }
            fileListLive.postValue(fileList)
        }
    }

    suspend fun getFileP7b(dir: File) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                getFileP7bLocal(dir)
            }
            fileListLive.postValue(fileList)
        }
    }

    private fun getFileCerLocal(dir: File) {
        val listFile = dir.listFiles()
        if (listFile != null && listFile.isNotEmpty()) {
            for (file in listFile) {
                if (file.isDirectory) {
                    getFileCerLocal(file)
                } else {
                    if (file.name.endsWith(".cer")) {
                        val temp = DirectoryCsr(
                            file.name,
                            file.absolutePath
                        )

                        if (!fileList.contains(temp)) {
                            fileList.add(temp)
                        }

                    }
                }
            }

        }

    }

    private fun getFileP7bLocal(dir: File) {
        val listFile = dir.listFiles()
        if (listFile != null && listFile.isNotEmpty()) {
            for (file in listFile) {
                if (file.isDirectory) {
                    getFileP7bLocal(file)
                } else {
                    if (file.name.endsWith(".p7b")) {
                        val temp = DirectoryCsr(
                            file.name,
                            file.absolutePath
                        )

                        if (!fileList.contains(temp)) {
                            fileList.add(temp)
                        }

                    }
                }
            }

        }

    }



    private val EXTERNAL_STORAGE_DIRECTORY = getDirectory("EXTERNAL_STORAGE", "/sdcard")

    fun getDirectory(variableName: String?, defaultPath: String?): File {
        val path = System.getenv(variableName)
        return if (path == null) File(defaultPath) else File(path)
    }


    fun getExternalStorageDirectory(context: Context): File? {
        return context.getExternalFilesDir(Constants.FolderCsr)
    }

    fun getExternalStorageDirectoryDownload(): File? {
        return EXTERNAL_STORAGE_DIRECTORY
    }

}
