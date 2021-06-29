package th.or.etda.teda.mobile.util

import th.or.etda.teda.mobile.ui.sign.SignCache

class Constants {
    companion object {
        const val SIGN_ALGORITHM = "SHA256WithRSA"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        val Folder = "TedaMobile"
        val FolderBackup = "backup"
        const val FileName = "teda_secret_shared_prefs"
        var listDataCache = ArrayList<SignCache>()
    }
}