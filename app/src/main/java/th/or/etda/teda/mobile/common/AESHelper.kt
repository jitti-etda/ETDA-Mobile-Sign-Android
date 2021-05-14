package th.or.etda.teda.mobile.common


class AESHelper {

    companion object{


//        fun encrypt(data: ByteArray, publicKey: Key): String {
//            val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
//            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
////            val bytes = cipher.doFinal(data.toByteArray())
//            return Base64.encodeToString(data, Base64.DEFAULT)
//        }
//
//        fun decrypt(data: String, privateKey: Key?): ByteArray {
//            val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
//            cipher.init(Cipher.DECRYPT_MODE, privateKey)
//            val encryptedData = Base64.decode(data, Base64.DEFAULT)
//            val decodedData = cipher.doFinal(encryptedData)
////            return String(decodedData)
//            return decodedData
//        }

        fun encryptAES(data: ByteArray,pass:String) :String{

            var aes = AESCrypt(pass)
            var res = aes.encrypt(data)
//            Log.i("encrypt", res)
            return res
        }

        fun decryptAES(message: String,pass:String): ByteArray? {
            var aes = AESCrypt(pass)
            try{
                var res = aes.decrypt(message)
//                Log.i("decrypt", res)
                return res;
            }catch (e: Exception){
                e.printStackTrace()
            }
            return null

        }
    }

}