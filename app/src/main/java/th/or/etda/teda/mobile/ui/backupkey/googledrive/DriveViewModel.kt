package th.or.etda.teda.mobile.ui.backupkey.googledrive

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import th.or.etda.teda.mobile.util.Constants
import java.io.*
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.SecretKeySpec


class DriveViewModel : ViewModel() {

//    fun encryptAES(data:String) :String{
//
//        var aes = AESCrypt("1234")
//        var res = aes.encrypt(data)
//        Log.i("encrypt", res)
//        return res
//    }
//
//    fun decryptAES(message: String):String {
//        var aes = AESCrypt("1234")
//        try{
//            var res = aes.decrypt(message)
//            Log.i("decrypt", res)
//            return res;
//        }catch (e: Exception){
//            e.printStackTrace()
//        }
//       return ""
//
//    }

    fun encryptFile(context: Context, cert: String, chains: String): File {
        val mainKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val folder = context.getExternalFilesDir(Constants.Folder)
//        var success = true
//        if (!folder.exists()) {
//            success = folder.mkdirs()
//        }
//       if (success) {
//           // Do something on success
//       } else {
//           // Do something else on failure
//       }
        val fileToWrite = "my_sensitive_data.txt"
        val file = File(
            folder,
            fileToWrite
        )
        file.delete()
        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            mainKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()


        val fileContent = "MY SUPER-SECRET INFORMATION"
            .toByteArray(StandardCharsets.UTF_8)
        encryptedFile.openFileOutput().apply {
            write(fileContent)
            flush()
            close()
        }
        return file
    }


    fun decrypt(context: Context) {
        val folder = context.getExternalFilesDir(Constants.Folder)
        val mainKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val fileToRead = "my_sensitive_data.txt"
        val file = File(
            folder,
            fileToRead
        )
        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            mainKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        val inputStream = encryptedFile.openFileInput()
        val byteArrayOutputStream = ByteArrayOutputStream()
        var nextByte: Int = inputStream.read()
        while (nextByte != -1) {
            byteArrayOutputStream.write(nextByte)
            nextByte = inputStream.read()
        }

        val plaintext: ByteArray = byteArrayOutputStream.toByteArray()
        val str = String(plaintext, StandardCharsets.UTF_8)
        Log.i("Decrypt", str)
    }


    @Throws(
        IOException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class
    )
    fun encrypt(context: Context,file:String) {
        // Here you read the cleartext.
        val fis = FileInputStream(file)
        // This stream write the encrypted text. This stream will be wrapped by another stream.
        val folder = context.getExternalFilesDir(Constants.Folder)
        val file = File(
            folder,
            "tempP12.p12"
        )
        val fos = FileOutputStream(file)

        // Length is 16 byte
        // Careful when taking user input!!! https://stackoverflow.com/a/3452620/1188357
        val sks = SecretKeySpec("MyDifficultPassw".toByteArray(), "AES")
        // Create cipher
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, sks)
        // Wrap the output stream
        val cos = CipherOutputStream(fos, cipher)
        // Write bytes
        var b: Int
        val d = ByteArray(8)
        while (fis.read(d).also { b = it } != -1) {
            cos.write(d, 0, b)
        }
        // Flush and close streams.
        cos.flush()
        cos.close()
        fis.close()
    }

    @Throws(
        IOException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class
    )
    fun decrypt() {
        val fis = FileInputStream("data/encrypted")
        val fos = FileOutputStream("data/decrypted")
        val sks = SecretKeySpec("MyDifficultPassw".toByteArray(), "AES")
        val cipher: Cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, sks)
        val cis = CipherInputStream(fis, cipher)
        var b: Int
        val d = ByteArray(8)
        while (cis.read(d).also { b = it } != -1) {
            fos.write(d, 0, b)
        }
        fos.flush()
        fos.close()
        cis.close()
    }



}
