package th.or.etda.teda.mobile.ui.importkey

import android.content.Context
import android.net.Uri
import android.os.StrictMode
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64OutputStream
import android.util.Log
import th.or.etda.teda.mobile.common.CryptLib
import th.or.etda.teda.mobile.common.SingleLiveEvent
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.model.ExtrackP12
import th.or.etda.teda.mobile.util.Constants
import th.or.etda.teda.mobile.util.KeyUtil.toBase64
import th.or.etda.teda.mobile.util.UtilApps
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyStore
import java.security.PrivateKey
import java.util.*
import javax.crypto.KeyGenerator


class ImportHelper {


    companion object {

        val fileLiveData = SingleLiveEvent<File>()

        fun import(
            context: Context,
            uri: Uri,
            password: String,
            name: String
        ): Certificate {
            val keyFile = context.contentResolver.openInputStream(uri)



            val p12: KeyStore = KeyStore.getInstance("pkcs12")
            p12.load(keyFile, password.toCharArray())

            val alias = p12.aliases().toList().first()
            val priKey: PrivateKey =
                p12.getKey(p12.aliases().toList().first(), password.toCharArray()) as PrivateKey

            val aliasesName = p12.aliases().toList().first()
            val certificateChain = p12.getCertificateChain(aliasesName)
            val certCa = certificateChain.first().toBase64()
            val cartChains = certificateChain.last().toBase64()


            val cert = p12.getCertificateChain(alias)
            val c = p12.getCertificate(alias)




            val ks = KeyStore.getInstance(Constants.ANDROID_KEY_STORE)
            ks.load(null)
            ks.setKeyEntry(name, priKey, null, cert)

            return Certificate(name, certCa, cartChains,UtilApps.currentDate())
        }


        fun extrackP12(
            context: Context,
            uri: Uri,
            password: String,
            name: String
        ): ExtrackP12 {
            val keyFile = context.contentResolver.openInputStream(uri)



            val p12: KeyStore = KeyStore.getInstance("pkcs12")
            p12.load(keyFile, password.toCharArray())

            val alias = p12.aliases().toList().first()
            Log.i("alias", alias)
            val priKey: PrivateKey =
                p12.getKey(p12.aliases().toList().first(), password.toCharArray()) as PrivateKey
            val certificateChain = p12.getCertificateChain(alias)
            val certCa = certificateChain.first().toBase64()
            val cartChains = certificateChain.last().toBase64()

            val ks = KeyStore.getInstance(Constants.ANDROID_KEY_STORE)
            ks.load(null)
            ks.setKeyEntry(name, priKey, null, certificateChain)


            return ExtrackP12(name,cartChains,certCa,priKey)
        }

        fun restore(
            byteBuff: ByteArray,
            password: String, name: String
        ): Certificate {
//            val keyFile = context.contentResolver.openInputStream(uri)

            val keyFile: InputStream = ByteArrayInputStream(byteBuff)

            val p12: KeyStore = KeyStore.getInstance("pkcs12")
            p12.load(keyFile, password.toCharArray())
            println("----------------------------")

//                if (!p12.aliases().toList().isEmpty()) {
            val alias = p12.aliases().toList().first()
            Log.i("alias", alias)
            val priKey: PrivateKey =
                p12.getKey(p12.aliases().toList().first(), password.toCharArray()) as PrivateKey
//                    val privateKey: Key = p12.getKey(alias, password)

            val aliasesName = p12.aliases().toList().first()
            val certificateChain = p12.getCertificateChain(aliasesName)
            val certCa = certificateChain.first().toBase64()
            val cartChains = certificateChain.last().toBase64()

            val cert = p12.getCertificateChain(alias)
            val ks = KeyStore.getInstance(Constants.ANDROID_KEY_STORE)
//                val ks = KeyStore.getInstance(KeyStore.getDefaultType())
            ks.load(null)
//                ks.setCertificateEntry(alias, cert[0])
            ks.setKeyEntry(name, priKey, null, cert)


            return Certificate(name, certCa, cartChains,UtilApps.currentDate())
        }

        fun restoreP12(
            byteBuff: ByteArray,
            password: String, name: String
        ): ExtrackP12 {
//            val keyFile = context.contentResolver.openInputStream(uri)

            val keyFile: InputStream = ByteArrayInputStream(byteBuff)

            val p12: KeyStore = KeyStore.getInstance("pkcs12")
            p12.load(keyFile, password.toCharArray())

            println("----------------------------")

//                if (!p12.aliases().toList().isEmpty()) {
            val alias = p12.aliases().toList().first()
            Log.i("alias", alias)
            val priKey: PrivateKey =
                p12.getKey(p12.aliases().toList().first(), password.toCharArray()) as PrivateKey
//                    val privateKey: Key = p12.getKey(alias, password)

            val aliasesName = p12.aliases().toList().first()
            val certificateChain = p12.getCertificateChain(aliasesName)
            val certCa = certificateChain.first().toBase64()
            val cartChains = certificateChain.last().toBase64()
             return ExtrackP12(name,cartChains,certCa,priKey)
        }

        public fun encryptP12forBackup(
            context: Context,
            name: String,
            password: String,
            file: File
        ) {

            try {
                val folder = context.getExternalFilesDir(Constants.FolderBackup)
                val fileStoreEncrypt = File(
                    folder,
                    name + "_backup" + ".txt"
                )

                val sss: ByteArray = Files.readAllBytes(Paths.get(file.toURI()))
                val s: String = Base64.getEncoder().encodeToString(sss)

                val cryptLib = CryptLib()
                var res = cryptLib.encryptPlainTextWithRandomIV(s, password)

                writeToFile(res, fileStoreEncrypt)



            } catch (e: IOException) {
                e.printStackTrace()
                UtilApps.alertDialog(context, "Wrong password or corrupted file")
            }


        }





        private fun writeToFile(data: String, file: File) {
            val stream = FileOutputStream(file)
            try {
                stream.write(data.toByteArray())
            } finally {
                stream.close()
            }
            fileLiveData.value = file
        }



        public fun convertStreamToString(`is`: InputStream): String? {

            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            val reader = BufferedReader(InputStreamReader(`is`))
            val sb = StringBuilder()
            var line: String? = null
            try {
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append('\n')
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return sb.toString()
        }


        fun writeTempFile(context: Context, input: InputStream): File {
            try {
                val file = File(context.getCacheDir(), "temp.p12")
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(4 * 1024) // or other buffer size
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
                return file
            } finally {
                input.close()
            }
        }

    }


}