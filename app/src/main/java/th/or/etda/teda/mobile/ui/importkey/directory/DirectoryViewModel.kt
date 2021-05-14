package th.or.etda.teda.mobile.ui.importkey.directory

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class DirectoryViewModel : ViewModel() {

    private val fileList = ArrayList<Directory>()

    val fileListLive = MutableLiveData<ArrayList<Directory>>()

    suspend fun getFile(dir: File) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                getFileLocal(dir)
            }
            fileListLive.postValue(fileList)
        }
//        fileListLive.value = fileList
//        return fileList
    }

    private fun getFileLocal(dir: File) {
        val listFile = dir.listFiles()
        if (listFile != null && listFile.isNotEmpty()) {
            for (file in listFile) {
//                Log.i("file", file.name)
                if (file.isDirectory) {
                    getFileLocal(file)
                } else {
                    if (file.name.endsWith(".p12")
                    ) {
                        val temp = Directory(
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

    fun getFileBackup(dir: File): ArrayList<Directory> {
        viewModelScope.launch {

            val listFile = dir.listFiles()

            if (listFile != null && listFile.isNotEmpty()) {
                for (file in listFile) {
//                Log.i("file", file.name)
                    if (file.isDirectory) {
                        getFileBackup(file)
                    } else {
                        val temp = Directory(
                            file.name,
                            file.absolutePath
                        )

                        if (!fileList.contains(temp)) fileList.add(temp)
                    }
                }
            }
        }
        return fileList
    }

    private val EXTERNAL_STORAGE_DIRECTORY = getDirectory("EXTERNAL_STORAGE", "/sdcard")

    fun getDirectory(variableName: String?, defaultPath: String?): File {
        val path = System.getenv(variableName)
        return if (path == null) File(defaultPath) else File(path)
    }

    fun getExternalStorageDirectory(): File? {
        return EXTERNAL_STORAGE_DIRECTORY
    }

}
