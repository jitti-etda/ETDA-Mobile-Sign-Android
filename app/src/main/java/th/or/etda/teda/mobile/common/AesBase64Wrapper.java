package th.or.etda.teda.mobile.common;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AesBase64Wrapper {

//    private static String PASSWORD = "it should be same like server or other platform";
    private static String SALT = "tedamobilesigning";


    private static volatile AesBase64Wrapper sSoleInstance = new AesBase64Wrapper();

    //private constructor.
    private AesBase64Wrapper() {
//        String EncryptString = AesBase64Wrapper.getInstance().encryptAndEncode("hello");
//        String DecryptString = AesBase64Wrapper.getInstance().encryptAndEncode(EncryptString);

    }

    public static AesBase64Wrapper getInstance() {

        return sSoleInstance;

    }

    // For Encryption
    public String encryptAndEncode(String raw,String passwordString) {
        try {
            Cipher c = getCipher(Cipher.ENCRYPT_MODE,passwordString);
            byte[] encryptedVal = c.doFinal(getBytes(raw));

            //String retVal = Base64.encodeToString(encryptedVal, Base64.DEFAULT);

            String retVal = Base64.encodeToString(encryptedVal, Base64.NO_WRAP);
            return retVal;
        }catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public byte[] decodeAndDecrypt(String encrypted,String passwordString) throws Exception {
//        byte[] decodedValue = Base64.decode(getBytes(encrypted),Base64.DEFAULT);
        byte[] decodedValue = Base64.decode(getBytes(encrypted), Base64.NO_WRAP);

        Cipher c = getCipher(Cipher.DECRYPT_MODE,passwordString);
        byte[] decValue = c.doFinal(decodedValue);
//        return new String(decValue);
        return decValue;
    }

    private String getString(byte[] bytes) throws UnsupportedEncodingException {
        return new String(bytes, "UTF-8");
    }

    private byte[] getBytes(String str) throws UnsupportedEncodingException {
        return str.getBytes("UTF-8");
    }

    private Cipher getCipher(int mode,String passwordString) throws Exception {
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        String xyz = String.valueOf(generateKey(passwordString));
        Log.i("generateKey", xyz);
        c.init(mode, generateKey(passwordString), new IvParameterSpec(iv));
        return c;
    }

    private Key generateKey(String passwordString) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        char[] password = passwordString.toCharArray();
        byte[] salt = getBytes(SALT);

        KeySpec spec = new PBEKeySpec(password, salt, 4096, 32);
        SecretKey tmp = factory.generateSecret(spec);
        byte[] encoded = tmp.getEncoded();
        byte b = encoded[1];
        Log.e("Secrete Key", String.valueOf(encoded));
//        [B@769d84b
        return new SecretKeySpec(encoded, "AES");
    }


}
