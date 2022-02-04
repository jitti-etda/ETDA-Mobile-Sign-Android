package th.or.etda.teda.mobile.ui.csr

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import androidx.lifecycle.ViewModel

import android.R.attr.mimeType
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.webkit.CookieManager
import android.widget.Toast
import th.or.etda.teda.mobile.common.GenCsr
import th.or.etda.teda.mobile.common.Utils
import th.or.etda.teda.mobile.util.UtilApps
import java.io.File
import java.io.FileOutputStream


class InetWebViewModel : ViewModel() {


    fun downloadFile(
        context: Context,
        url: String,
        contentDisposition: String,
        mimetype: String,
        userAgent: String
    ) {
        val request: DownloadManager.Request = DownloadManager.Request(
            Uri.parse(url)
        )
//        request.allowScanningByMediaScanner()
        request.setMimeType(mimetype)
        val cookies: String = CookieManager.getInstance().getCookie(url)
        request.addRequestHeader("Cookie", cookies)
        request.addRequestHeader(
            "User-Agent",
            userAgent
        )
        request.setDescription("Downloading file...");
        request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            URLUtil.guessFileName(url, contentDisposition, mimetype)
        );

        val dm: DownloadManager? = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager?
        dm?.enqueue(request)
    }

    fun writeFileCer(data: String): String {
        getExternalStorageDirectory().let {
            val fileStoreEncrypt = File(
                it,
                UtilApps.timestampName() + ".cer"
            )
            val stream = FileOutputStream(fileStoreEncrypt)
            try {
                stream.write(data.toByteArray())

            } finally {
                stream.close()
            }
            return fileStoreEncrypt.name
        }
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
