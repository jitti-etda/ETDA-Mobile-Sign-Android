package th.or.etda.teda.mobile.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import th.or.etda.teda.mobile.MainActivity;
import th.or.etda.teda.mobile.util.UtilApps;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

public class BiometricEncryptedSharedPreferences {
    private static final int KEY_SIZE = 256;
    private static final String MASTER_KEY_ALIAS = "_androidx_security_master_key_biometric";
//    private static final String MASTER_KEY_ALIAS = "_teda_master_key_biometric";

    /**
     * @param fragment   A reference to the client's fragment
     * @param fileName   The name of the file to open; can not contain path separators
     * @param timeout    duration in seconds, must be greater than 0
     * @param promptInfo The information that will be displayed on the prompt. Create this object using {@link BiometricPrompt.PromptInfo.Builder}
     * @return LiveData of EncryptedSharedPreferences that requires user biometric authentication
     */
    public static LiveData<SharedPreferences> create(final Fragment fragment, final String fileName, final int timeout, BiometricPrompt.PromptInfo promptInfo) {
        final MutableLiveData<SharedPreferences> out = new MutableLiveData<>();
        new BiometricPrompt(fragment.requireActivity(), ContextCompat.getMainExecutor(fragment.requireContext()),
                new AuthenticationCallback(fragment.requireContext(), fileName, timeout, out)
        ).authenticate(promptInfo);
        return out;
    }

    /**
     * @param activity   A reference to the client's activity
     * @param fileName   The name of the file to open; can not contain path separators
     * @param timeout    duration in seconds, must be greater than 0
     * @param promptInfo The information that will be displayed on the prompt. Create this object using {@link BiometricPrompt.PromptInfo.Builder}
     * @return LiveData of EncryptedSharedPreferences that requires user biometric authentication
     */
    public static LiveData<SharedPreferences> create(final FragmentActivity activity, final String fileName, final int timeout, BiometricPrompt.PromptInfo promptInfo) {
        final MutableLiveData<SharedPreferences> out = new MutableLiveData<>();
        new BiometricPrompt(activity, ContextCompat.getMainExecutor(activity),
                new AuthenticationCallback(activity, fileName, timeout, out)
        ).authenticate(promptInfo);
        return out;
    }

    private static SharedPreferences create(Context c, String fileName, int timeout) {
        try {
            KeyGenParameterSpec.Builder b = new KeyGenParameterSpec.Builder(MASTER_KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_SIZE)
                    .setUserAuthenticationRequired(true);
            b.setUserAuthenticationValidityDurationSeconds(timeout);

//            KeyPairGenerator kpg = KeyPairGenerator.getInstance(
//                    KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
//
//            kpg.initialize(new KeyGenParameterSpec.Builder(
//                    alias,
//                    KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
//                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
//                    .setKeySize(KEY_SIZE)
//                    .build());
//
//            KeyPair keyPair = kpg.generateKeyPair();

//            MasterKey masterKey = new MasterKey.Builder(c)
//                    .setKeyGenParameterSpec(b.build())
//                    .build();
            return EncryptedSharedPreferences.create(fileName, MasterKeys.getOrCreate(b.build()), c, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
//            return EncryptedSharedPreferences.create(c,fileName, masterKey,EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class AuthenticationCallback extends BiometricPrompt.AuthenticationCallback {
        private final Context context;
        private final String fileName;
        private final int timeout;
        private final MutableLiveData<SharedPreferences> out;


        AuthenticationCallback(Context context, String fileName, int timeout, MutableLiveData<SharedPreferences> out) {
            this.context = context;
            this.fileName = fileName;
            this.timeout = timeout;
            this.out = out;
        }

        @Override
        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            out.postValue(create(context, fileName, timeout));
        }

        @Override
        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            out.postValue(null);
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            out.postValue(null);
        }

    }


    public static boolean checkBio(Activity context) {
        BiometricManager biometricManager = BiometricManager.from(context);

        int bio = 0;
       try{
            bio = biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
        }catch (Exception e){
            bio = biometricManager.canAuthenticate();
        }
        switch (bio) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("MY_APP_TAG", "No biometric features available on this device.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                Toast.makeText(context,"Device not support Biometric",Toast.LENGTH_SHORT).show();
                return false;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e("MY_APP_TAG", "BIOMETRIC_ERROR_NONE_ENROLLED.");
                // Prompts the user to create credentials that your app accepts.
                new AlertDialog.Builder(context)
                        .setMessage("Please enable Biometric")
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // continue with delete

                                        Intent enrollIntent = null;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                            enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                                            enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                                    BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                                            enrollIntent = new Intent(Settings.ACTION_FINGERPRINT_ENROLL);

                                        } else {
                                            enrollIntent = new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS);
                                        }
                                        context.startActivityForResult(enrollIntent, 1234);
                                    }
                                }).show();

                return false;
        }
        return false;
    }
}