package th.or.etda.teda.mobile.ui.importkey

import android.content.Context
import android.net.Uri
import android.os.StrictMode
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import th.or.etda.teda.mobile.common.AESHelper
import th.or.etda.teda.mobile.common.SingleLiveEvent
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.ui.home.HomeViewModel
import th.or.etda.teda.mobile.util.Constants
import th.or.etda.teda.mobile.util.KeyUtil.toBase64
import th.or.etda.teda.mobile.util.UtilApps
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyStore
import java.security.PrivateKey
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


            println("sss => $keyFile")

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

//                val certificate = Certificate("etda-ca", certCa, cartChains)
//            val c: X509Certificate = p12.getCertificate(alias) as X509Certificate

//                var private = priKey.toString()

//                var rsaKey = RSACrypt2.getAsymmetricKeyPair()
//                var pv = rsaKey?.public?.let { it1 -> RSACrypt2.encrypt(priKey.encoded, it1) }

//                var aes = RSACrypt2.encryptAES(priKey.encoded)
//                KeyStoreHelper.createKeys(context, name)
//
//
//                val kpg = KeyPairGenerator.getInstance(
//                    KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore"
//                )
//
//                kpg.initialize(
//                    KeyGenParameterSpec.Builder(
//                        "password_alias",
//                        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
//                    )
//                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
//                        .setKeySize(1024)
//                        .build()
//                )
//
//                val keyPair = kpg.generateKeyPair()
//
//                val keyStore = KeyStore.getInstance("AndroidKeyStore")
//                keyStore.load(null)
//                val entry = keyStore.getEntry("password_alias", null)
//                val privateKey = (entry as KeyStore.PrivateKeyEntry).privateKey
//                val publicKey = keyStore.getCertificate("password_alias").publicKey
//
////                var rsa = KeyStoreHelper.encrypt(name, priKey.encoded,publicKey)
//
//                var ss = RSACrypt2.encrypt(priKey.encoded,publicKey)

//            addCertificate(Certificate(name, certCa, cartChains))


            val cert = p12.getCertificateChain(alias)
            val c = p12.getCertificate(alias)

            generateSecretKey(KeyGenParameterSpec.Builder(
                HomeViewModel.ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                // Invalidate the keys if the user has registered a new biometric
                // credential, such as a new fingerprint. Can call this method only
                // on Android 7.0 (API level 24) or higher. The variable
                // "invalidatedByBiometricEnrollment" is true by default.
                .setInvalidatedByBiometricEnrollment(true)
                .build())


//            val ks = KeyStore.getInstance(HomeViewModel.ANDROID_KEY_STORE)
////                val ks = KeyStore.getInstance(KeyStore.getDefaultType())
//            ks.load(null)
////                ks.setCertificateEntry(alias, cert[0])
//            ks.setKeyEntry(name, priKey, null, cert)

            return Certificate(name, certCa, cartChains)
        }



        private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, HomeViewModel.ANDROID_KEY_STORE)
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }

        fun extrackP12(
            context: Context,
            uri: Uri,
            password: String
        ): PrivateKey {
            val keyFile = context.contentResolver.openInputStream(uri)


            println("sss => $keyFile")

            val p12: KeyStore = KeyStore.getInstance("pkcs12")
            p12.load(keyFile, password.toCharArray())
            println("----------------------------")

//                if (!p12.aliases().toList().isEmpty()) {
            val alias = p12.aliases().toList().first()
            Log.i("alias", alias)
            val priKey: PrivateKey =
                p12.getKey(p12.aliases().toList().first(), password.toCharArray()) as PrivateKey
//            val certificateChain = p12.getCertificateChain(alias)
//            val certCa = certificateChain.first().toBase64()
//            val cartChains = certificateChain.last().toBase64()

            return priKey
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
            val ks = KeyStore.getInstance(HomeViewModel.ANDROID_KEY_STORE)
//                val ks = KeyStore.getInstance(KeyStore.getDefaultType())
            ks.load(null)
//                ks.setCertificateEntry(alias, cert[0])
            ks.setKeyEntry(name, priKey, null, cert)


            return Certificate(name, certCa, cartChains)
        }

        fun restoreP12(
            byteBuff: ByteArray,
            password: String, name: String
        ): PrivateKey {
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

//            val aliasesName = p12.aliases().toList().first()
//            val certificateChain = p12.getCertificateChain(aliasesName)
//            val certCa = certificateChain.first().toBase64()
//            val cartChains = certificateChain.last().toBase64()
//
//            val cert = p12.getCertificateChain(alias)
//            val ks = KeyStore.getInstance(HomeViewModel.ANDROID_KEY_STORE)
////                val ks = KeyStore.getInstance(KeyStore.getDefaultType())
//            ks.load(null)
////                ks.setCertificateEntry(alias, cert[0])
//            ks.setKeyEntry(name, priKey, null, cert)


            return priKey
        }

        public fun encryptP12forBackup(
            context: Context,
            name: String,
            password: String,
            file: File
        ) {

            try {
//                val folder = context.getExternalFilesDir(Constants.Folder)
                val folder = context.getExternalFilesDir(Constants.FolderBackup)
//                val folder = File(
//                    Environment.getExternalStorageDirectory().toString() +
//                            File.separator + Constants.Folder
//                )
//                if (!folder.exists()) {
//                    folder.mkdirs()
//                }

//            val fileStore = File(
//                folder,
//                name + "_backup.p12"
//            )
                val fileStoreEncrypt = File(
                    folder,
                    name + "_backup" + ".txt"
                )

                val sss: ByteArray = Files.readAllBytes(Paths.get(file.toURI()))
                var res = AESHelper.encryptAES(sss, password)
                writeToFile(res, fileStoreEncrypt)


//            var keyStore = KeyStore.getInstance("PKCS12")
//            val byteBuff: ByteArray = Files.readAllBytes(Paths.get(file.toURI()))
//            val inputStream: InputStream = ByteArrayInputStream(byteBuff)
//            keyStore.load(inputStream, password)
//
//            keyStore.store(FileOutputStream(fileStore), password).let {
//
//                val sss: ByteArray = Files.readAllBytes(Paths.get(fileStore.toURI()))
//                var res = RSACrypt2.encryptAES(sss)
//                writeToFile(res, fileStoreEncrypt)
//
//                var read = readFile(fileStoreEncrypt)
//                var de = RSACrypt2.decryptAES(read)
//                de?.let { it1 -> writeToFileByteArray(it1, fileStoreDecrypt) }
//            }


            } catch (e: IOException) {
                e.printStackTrace()
//                Toast.makeText(context, "wrong password or corrupted file", Toast.LENGTH_SHORT)
//                    .show()
                UtilApps.alertDialog(context, "wrong password or corrupted file")
            }


        }


        private fun writeToFileByteArray(data: ByteArray, file: File) {
            val stream = FileOutputStream(file)
            try {
                stream.write(data)
            } finally {
                stream.close()
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

        private fun readFile(file: File): String {
            val length = file.length().toInt()

            val bytes = ByteArray(length)

            val `in` = FileInputStream(file)
            try {
                `in`.read(bytes)
            } finally {
                `in`.close()
            }

            return String(bytes)
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