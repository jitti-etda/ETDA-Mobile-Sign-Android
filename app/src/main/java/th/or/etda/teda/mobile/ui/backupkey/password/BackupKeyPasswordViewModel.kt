package th.or.etda.teda.mobile.ui.backupkey.password

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.data.CertificateRepository
import th.or.etda.teda.mobile.util.Constants
import th.or.etda.teda.mobile.util.UtilApps
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyStore


class BackupKeyPasswordViewModel(var repository: CertificateRepository) : ViewModel() {


    var certLiveData = MutableLiveData<List<Certificate>>()
    fun getCertificateAll(): MutableLiveData<List<Certificate>> {

        viewModelScope.launch {
            certLiveData.value = repository.getCertAll()
        }
        return certLiveData
    }


    fun encryptP12forBackup(context: Context, password: CharArray, file: File) {

        try {
            val folder = context.getExternalFilesDir(Constants.FolderBackup)
            val fileStore = File(
                folder,
                file.name+"_backup.p12"
            )

//            var keyStore = KeyStore.getInstance("PKCS12")
//            keyStore.load(null, null)
//            keyStore.store(FileOutputStream(fileOne), password)

            var keyStore = KeyStore.getInstance("PKCS12")
            val byteBuff: ByteArray = Files.readAllBytes(Paths.get(file.toURI()))
            val inputStream: InputStream = ByteArrayInputStream(byteBuff)
            keyStore.load(inputStream, password)
            keyStore.store(FileOutputStream(fileStore), password)


        } catch (e: IOException) {
            e.printStackTrace()
//            Toast.makeText(context, "wrong password or corrupted file", Toast.LENGTH_SHORT).show()
            UtilApps.alertDialog(context,"wrong password or corrupted file")
        }


    }


//    fun encryptP12(context: Context, password: CharArray) {
//
//        try {
////            val folder = context.getExternalFilesDir(Constants.Folder)
//            val folder = Environment.getExternalStorageDirectory()
//
//            val fileTemp = File(
//                folder,
//                "temp_output_test.p12"
//            )
//
//            val jsonFile = File(
//                folder,
//                "temp_json_test.json"
//            )
//
//            val newKs = KeyStore.getInstance("PKCS12")
//            newKs.load(null, null)
//
//            val ks = KeyStore.getInstance(HomeViewModel.ANDROID_KEY_STORE)
////            val ks = KeyStore.getInstance(KeyStore.getDefaultType())
//            ks.load(null)
//
//            viewModelScope.launch {
//
//                var aliasList = repository.getCertAll()
//                for (i in aliasList.indices) {
//                    var cert = aliasList[i]
//                    val pvKey = ks.getKey(cert.certName, null) as PrivateKey
//                    val publicKey = ks.getCertificate(ks.aliases().toList().first()).publicKey
//                    val certCaKey = ks.getCertificate(ks.aliases().toList().last()).publicKey
//                    println("KEY == >${pvKey.encoded}")
////                var privateEncode = savePrivateKey(pvKey)
//                    val certificateChain = ks.getCertificateChain(cert.certName)
////                val certCa = certificateChain.first().toBase64()
////                val cartChains = certificateChain.last().toBase64()
////                var json = JSONObject()
////                json.put("private", privateEncode)
////                json.put("cert", certCa)
////                json.put("chains", cartChains)
////                json.put("alias", alias)
//////                val keyAsString = Base64.getEncoder().encodeToString(key)
//////                FileOutputStream(fileTemp)
////                val writer = FileWriter(jsonFile)
////                writer.write(json.toString())
////                writer.close()
//
////                    val pkcs8EncodedBytes: ByteArray =
////                        android.util.Base64.decode(cert.pvk, android.util.Base64.DEFAULT)
//
//                    // extract the private key
//
//
//                    // extract the private key
////                    var key = RSACrypt2.getAsymmetricKeyPair()
////                    var pv = cert.pvk?.let { RSACrypt2.decrypt(it,key?.private) }
////                    var pv = cert.pvk?.let { RSACrypt2.decryptAES(it) }
//
//
////                    val keySpec = PKCS8EncodedKeySpec(pv)
////                    val kf = KeyFactory.getInstance("RSA")
////                    val privKey = kf.generatePrivate(keySpec)
////                    System.out.println(privKey)
////
////                    newKs.setKeyEntry(cert.certName, privKey, null, certificateChain)
////                    newKs.store(FileOutputStream(fileTemp), password).let {
////
////                    }
//                }
//            }
//
//
//        } catch (e: Exception) {
//            e.printStackTrace()
////            Toast.makeText(context, "wrong password or corrupted file", Toast.LENGTH_SHORT).show()
//            UtilApps.alertDialog(context,"wrong password or corrupted file")
//        }
//
//    }




}
