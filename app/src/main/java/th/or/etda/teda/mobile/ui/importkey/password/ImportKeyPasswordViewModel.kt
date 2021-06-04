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

    val showLoading = ObservableBoolean()

    fun addCertificate(certificate: Certificate) {
        viewModelScope.launch {
            repository.insert(certificate)
        }
    }


    //    fun extractCA(context: Context, password: String,passwordBackup: String, name: String, file: File,isBackup:Boolean): Boolean {
//
//        try {
//            caUri.value?.let { it ->
//                println("path is => ${caUri.value}")
//
//
//               var cert =  ImportHelper.import(context,it,password,passwordBackup,name,file,isBackup)
//
//                addCertificate(cert)
//
//                return true
//            }
//
//
//        } catch (e: IOException) {
//            e.printStackTrace()
//            Toast.makeText(context, "wrong password or corrupted file", Toast.LENGTH_SHORT).show()
//        }
//        return false
//    }
    var extractSuccess = SingleLiveEvent<String>()
    fun extractCA(
        context: Context,
        password: String,
        nameRoot: String
    ) {

        showLoading.set(true)
        viewModelScope.launch {
            try {
                caUri.value?.let { it ->
                    println("path is => ${caUri.value}")

//                    var name = nameRoot + "_" + System.currentTimeMillis() / 1000
                    var name = nameRoot + "_" + UtilApps.timestampName()
                    var cert = ImportHelper.import(
                        context,
                        it,
                        password,
                        name
                    )
                    showLoading.set(false)

                    addCertificate(cert)
                    extractSuccess.value = name

                }


            } catch (e: IOException) {
                e.printStackTrace()
                extractSuccess.value = ""
//                Toast.makeText(context, "wrong password or corrupted file", Toast.LENGTH_SHORT)
//                    .show()

            }
        }

    }

    var extractP12Success = SingleLiveEvent<ExtrackP12>()
    fun extractP12(
        context: Context,
        password: String
    ) {

        showLoading.set(true)
        viewModelScope.launch {
            try {
                caUri.value?.let { it ->
                    println("path is => ${caUri.value}")

                    var cert = ImportHelper.extrackP12(
                        context,
                        it,
                        password
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

    fun importTest(context: Context, password: String) {
        try {
            caUri.value?.let { it ->
                val keyFile = context.contentResolver.openInputStream(it)
                val p12 = KeyStore.getInstance("pkcs12")
                p12.load(keyFile, password.toCharArray())
                val e = p12.aliases()
                while (e.hasMoreElements()) {
                    val alias = e.nextElement() as String
                    val c = p12.getCertificate(alias) as X509Certificate
                    addCertificateToKeyStore(c)
                }
            }

        } catch (e: Exception) {
        }
    }

    private fun addCertificateToKeyStore(c: X509Certificate) {
        try {
            val ks = KeyStore.getInstance("AndroidKeyStore")
            ks.load(null)
            ks.setCertificateEntry("myCertAlias", c)
        } catch (e: java.lang.Exception) {
        }
    }


    fun encrypt(data: String, publicKey: Key?): String {
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val bytes = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    fun decrypt(data: String, privateKey: Key?): String {
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedData = Base64.decode(data, Base64.DEFAULT)
        val decodedData = cipher.doFinal(encryptedData)
        return String(decodedData)
    }


    fun genKey(alias: String) {
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore"
        )

        kpg.initialize(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setKeySize(1024)
                .build()
        )

        val keyPair: KeyPair = kpg.generateKeyPair()

        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val entry = keyStore.getEntry(alias, null)
        val privateKey = (entry as KeyStore.PrivateKeyEntry).privateKey
        val publicKey = keyStore.getCertificate(alias).publicKey

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
                    var folderID = it.result.id
                    mDriveServiceHelper.uploadFile(filePathBackup, "text/plain", folderID)
                        ?.addOnCompleteListener {
                            showLoading.set(false)
                            uploadSuccess.value = true
                            Log.i("upload", "success")
                        }
                }
        }
    }

}

//class ExtractCaViewModelFactory(private val repository: CertificateRepository) :
//    ViewModelProvider.Factory {
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(ExtractCAViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return ExtractCAViewModel(repository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//
//}