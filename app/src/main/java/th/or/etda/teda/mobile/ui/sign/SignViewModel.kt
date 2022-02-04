package th.or.etda.teda.mobile.ui.sign

import android.app.Activity
import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import okhttp3.internal.and
import th.or.etda.teda.mobile.common.AppResult
import th.or.etda.teda.mobile.common.SingleLiveEvent
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.model.SignedInfo
import th.or.etda.teda.mobile.repository.DataResponse
import th.or.etda.teda.mobile.repository.SigningRepository
import th.or.etda.teda.mobile.util.SigningSingUtil
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.util.concurrent.ExecutionException


class SignViewModel(val homeRepository: SigningRepository) : ViewModel() {

    companion object {
        const val SIGN_ALGORITHM = "SHA256withRSA"

        private const val TAG = "SignViewModel"
    }

    private lateinit var signMessage: String

    private var cameraProviderLiveData: MutableLiveData<ProcessCameraProvider>? = null

    val signedInfo = SingleLiveEvent<SignedInfo>()
    val signedInfoSubmit = SingleLiveEvent<SignedInfo>()

    val signedInfoSubmitError = SingleLiveEvent<DataResponse>()


    fun signWithKeyStore(signInfo: String, pvKey: String): String {
        var signString = Base64.decode(signInfo, Base64.NO_WRAP)

        val binCpk: ByteArray = Base64.decode(pvKey,Base64.NO_WRAP)
        val keyFactory = KeyFactory.getInstance("RSA")
        val privateKeySpec = PKCS8EncodedKeySpec(binCpk)
        var privateKey = keyFactory.generatePrivate(privateKeySpec)
        val signature = Signature.getInstance(SIGN_ALGORITHM)
        signature.initSign(privateKey)
        signature.update(signString)
        val encodeSign = Base64.encodeToString(signature.sign(), Base64.NO_WRAP)
        signMessage = encodeSign
        return signMessage
    }


    fun cameraProvider(context: Context): MutableLiveData<ProcessCameraProvider>? {
        if (cameraProviderLiveData == null) {
            cameraProviderLiveData = MutableLiveData()
            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener(
                Runnable {
                    try {
                        cameraProviderLiveData!!.setValue(cameraProviderFuture.get())
                    } catch (e: ExecutionException) {
                        Log.e(TAG, "Unhandled exception", e)
                    } catch (e: InterruptedException) {
                        Log.e(TAG, "Unhandled exception", e)
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
            return cameraProviderLiveData!!
        }

        return null
    }



    val showLoading = ObservableBoolean()
    var showError = SingleLiveEvent<String>()

    fun signingSignInfo(urls: String, certCa: String, certChains: String) {

        var data: List<String>
        data = urls.split(";")

        var url = data[SigningSingUtil.URL.ordinal] + "/" + data[SigningSingUtil.REQUEST_ID.ordinal]

        showLoading.set(true)

        viewModelScope.launch {
            val result = homeRepository.signingSign(
                url,
                data[SigningSingUtil.TOKEN.ordinal], certCa, certChains
            )
            showLoading.set(false)

            when (result) {
                is AppResult.Success -> {
//                    signedInfo.value = result.successData
                    val gson = Gson()
                    val type = object : TypeToken<SignedInfo>() {}.type
                    signedInfo.value = gson.fromJson(result.successData, type)
                    showError.value = ""
                }
                is AppResult.Error -> {
                    showError.value = result.exception.message
                }
            }
        }
    }


    fun signingSignInfoSubmit(urls: String, signature: String) {
        val data = urls.split(";")
        var url =
            data[SigningSingUtil.URL.ordinal] + "/" + data[SigningSingUtil.REQUEST_ID.ordinal] + "/submit"


        showLoading.set(true)

        viewModelScope.launch {
            val result = homeRepository.signingSignSubmit(
                url,
                data[SigningSingUtil.TOKEN.ordinal], signature
            )
            showLoading.set(false)
            when (result) {
                is AppResult.Success -> {
                    val gson = Gson()
                    val type = object : TypeToken<SignedInfo>() {}.type
                    signedInfoSubmit.value = gson.fromJson(result.successData, type)
                    showError.value = ""
                }
                is AppResult.Error -> {
                    showError.value = result.exception.message
                }
            }
        }
    }





}

