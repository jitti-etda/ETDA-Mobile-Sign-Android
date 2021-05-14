package th.or.etda.teda.mobile.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

public class BiometricEncryptedSharedPreferences {
    private static final int KEY_SIZE = 256;
    private static final String MASTER_KEY_ALIAS = "_androidx_security_master_key_biometric";

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
            return EncryptedSharedPreferences.create(fileName, MasterKeys.getOrCreate(b.build()), c, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
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


    public static boolean checkBio(Activity context){
        BiometricManager biometricManager = BiometricManager.from(context);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("MY_APP_TAG", "No biometric features available on this device.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e("MY_APP_TAG", "BIOMETRIC_ERROR_NONE_ENROLLED.");
                // Prompts the user to create credentials that your app accepts.
                final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                context.startActivityForResult(enrollIntent, 1234);
                return false;
        }
        return false;
    }
}