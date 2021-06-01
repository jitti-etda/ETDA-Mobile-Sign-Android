package th.or.etda.teda.mobile.ui.backupkey

import th.or.etda.teda.mobile.common.SingleLiveEvent
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.data.CertificateRepository
import th.or.etda.teda.mobile.ui.backupkey.googledrive.DriveServiceHelper
import th.or.etda.teda.mobile.util.Constants
import th.or.etda.teda.mobile.util.UtilApps
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyStore


class BackupKeyViewModel (var repository: CertificateRepository): ViewModel() {

    var showError = SingleLiveEvent<String>()

    var certLiveData = MutableLiveData<List<Certificate>>()
    fun getCertificateAll(): MutableLiveData<List<Certificate>> {

        viewModelScope.launch {
            var res = repository.getCertAll()
            certLiveData.value = res
            if(res.isEmpty()){
                showError.value  = "Not found"
            }else{
                showError.value  = ""
            }

        }
        return certLiveData
    }





    fun encryptP12(context: Context, password: CharArray, name: String, file: String) {

        try {
//            val p12: KeyStore = KeyStore.getInstance("pkcs12")
//            p12.load(file.inputStream(), password)
////            p12.store(file.outputStream(), password)
//            println("----------------------------")
//            val priKey: PrivateKey =
//                p12.getKey(p12.aliases().toList().first(), password) as PrivateKey
//            val privateKey = priKey
//            val publicKey = p12.getCertificate(p12.aliases().toList().first()).publicKey
//            val certCaKey = p12.getCertificate(p12.aliases().toList().last()).publicKey
//            println(p12.aliases())
//
////                println("privateKey -> $privateKey")
////                println("publicKey -> $publicKey")
////                println("certCa -> $certCaKey \n certCa enc -> ${certCaKey.encoded}")
//
////            val aliasesName = p12.aliases().toList().first()
////            val certificateChain = p12.getCertificateChain(aliasesName)
////            val certCa = certificateChain.first().toBase64()
////            val cartChains = certificateChain.last().toBase64()
//
//            val cert = p12.getCertificateChain(p12.aliases().toList().first())
//            val ks = KeyStore.getInstance(HomeViewModel.ANDROID_KEY_STORE)
//            ks.load(null)
//            ks.setKeyEntry(name, priKey, null, cert)


//            val fileOne = "/tmp/output_1.p12"
//            val fileTwo = "/tmp/output_2.p12"
            val folder = context.getExternalFilesDir(Constants.FolderBackup)
            val fileStore = File(
                folder,
                "output_2.p12"
            )

//            var keyStore = KeyStore.getInstance("PKCS12")
//            keyStore.load(null, null)
//            keyStore.store(FileOutputStream(fileOne), password)

            var keyStore = KeyStore.getInstance("PKCS12")
            val byteBuff: ByteArray = Files.readAllBytes(Paths.get(file))
            val inputStream: InputStream = ByteArrayInputStream(byteBuff)
            keyStore.load(inputStream, password)
            keyStore.store(FileOutputStream(fileStore), password)


        } catch (e: IOException) {
            e.printStackTrace()
//            Toast.makeText(context, "wrong password or corrupted file", Toast.LENGTH_SHORT).show()
            UtilApps.alertDialog(context,"wrong password or corrupted file")
        }


    }


}
