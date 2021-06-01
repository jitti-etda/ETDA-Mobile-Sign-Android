package th.or.etda.teda.mobile.ui.home

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
import th.or.etda.teda.mobile.model.SignedInfo
import th.or.etda.teda.mobile.repository.DataResponse
import th.or.etda.teda.mobile.repository.SigningRepository
import th.or.etda.teda.mobile.util.SigningSingUtil
import java.security.*
import java.util.concurrent.ExecutionException


class HomeViewModel(val homeRepository: SigningRepository) : ViewModel() {

    companion object {
        const val SIGN_ALGORITHM = "MD5WithRSA"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
//         var ANDROID_KEY_STORE = KeyStore.getDefaultType()
//        const val ANDROID_KEY_STORE = "AndroidCAStore"

//        const val ANDROID_KEY_STORE = "pkcs12"

        const val ALIAS = "TEDA_KEY"
        const val FileName = "teda_secret_shared_prefs"
        private const val TAG = "HomeViewModel"
    }

    //    private var app: Application? = null
    private lateinit var privateKey: PrivateKey
    private lateinit var publicKey: PublicKey
    private val message: String = "Message"
    private lateinit var signMessage: String
    private lateinit var signWithKeyStore: String

    private var cameraProviderLiveData: MutableLiveData<ProcessCameraProvider>? = null

    val signedInfo = MutableLiveData<SignedInfo?>()
    val signedInfoSubmit = MutableLiveData<SignedInfo>()

    val signedInfoError = MutableLiveData<DataResponse>()
    val signedInfoSubmitError = MutableLiveData<DataResponse>()


//    fun signWithKeyStore(signInfo: String, cert: Certificate): String {
//        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
////        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
//        keyStore.load(null)
//        val pvKey = keyStore.getKey(cert.certName, null) as PrivateKey
//
////        var pv = cert.pvk?.let { RSACrypt2.decryptAES(it) }
////
////
////        val keySpec = PKCS8EncodedKeySpec(pv)
////        val kf = KeyFactory.getInstance("RSA")
////        val privKey = kf.generatePrivate(keySpec)
////
////        println("KEY == >$privKey")
//        val signature = Signature.getInstance(SIGN_ALGORITHM)
//        signature.initSign(pvKey)
//        signature.update(signInfo.toByteArray(Charsets.UTF_8))
//        val encodeSign = Base64.encodeToString(signature.sign(), Base64.DEFAULT)
//        signMessage = encodeSign
//        println("signMessage => $signMessage")
//        return signMessage
//    }

    fun verifySignature(context: Context) {
        val p12: KeyStore = KeyStore.getInstance("pkcs12")
        val keyFile = (context as Activity).assets.open("CN_TEST_2021.p12")
        println("keyFile => $keyFile")
        val passphrase = "123456789".toCharArray()
        p12.load(keyFile, passphrase)
        publicKey = p12.getCertificate(p12.aliases().toList().first()).publicKey
//=======
//    fun verifySignature(context: Context) {
//        val p12: KeyStore = KeyStore.getInstance("pkcs12")
//        val keyFile = (context as Activity).assets.open("CN_TEST_2021.p12")
//        println("keyFile => $keyFile")
//        val passphrase = "123456789".toCharArray()
//        p12.load(keyFile, passphrase)
//        publicKey = p12.getCertificate(p12.aliases().toList().first()).publicKey
//>>>>>>> toon
        val publicSignature = Signature.getInstance(SIGN_ALGORITHM)
        publicSignature.initVerify(publicKey)
        println("sdasd => $publicKey")
//        publicSignature.update(viewBinding.editText.text.toString().toByteArray(Charsets.UTF_8))
        val signature = Base64.decode(signMessage, Base64.DEFAULT)
        println("Verify ==> ${publicSignature.verify(signature)}")
    }

    fun singWithFile() {
        println("private key <<<<< ${privateKey.algorithm} ${privateKey.format}")
        //            privateKey.algorithm
        val signature = Signature.getInstance(SIGN_ALGORITHM)
        signature.initSign(privateKey)
//        signature.update(viewBinding.editText.text.toString().toByteArray(Charsets.UTF_8))
        val encodeSign = Base64.encodeToString(signature.sign(), Base64.DEFAULT)
        signMessage = encodeSign
        println("signMessage => $signMessage")
    }


    // Handle any errors (including cancellation) here.
    fun cameraProvider(context: Context): MutableLiveData<ProcessCameraProvider>? {
//        val processCameraProvider: LiveData<ProcessCameraProvider>
        if (cameraProviderLiveData == null) {
            cameraProviderLiveData = MutableLiveData()
            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener(
                Runnable {
                    try {
                        cameraProviderLiveData!!.setValue(cameraProviderFuture.get())
                    } catch (e: ExecutionException) {
                        // Handle any errors (including cancellation) here.
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
        showLoading.set(true)
//        val mockData = SignedInfo(
//            description = SignedInfo.SignedDescription(
//                document = SignedInfo.SignedDocument(
//                    documentInfo = "documentInfo",
//                    documentType = "documentType",
//                    refNumber = "refNumber",
//                    documentName = "documentName",
//                    documentDescription = "documentDescription",
//                    businessType = "businessType"
//                ),
//                callbackUrl = "callbackUrl",
//                otherInfo = null
//            )
//        )
//        signedInfo.value = mockData
        var data: List<String>

        println("sssss => ${urls.split(";")}")
        data = urls.split(";")
        println(
            "data => URL:${data[SigningSingUtil.URL.ordinal]} \n" +
                    "request_id:${data[SigningSingUtil.REQUEST_ID.ordinal]} \n" +
                    "token:${data[SigningSingUtil.TOKEN.ordinal]} \n" +
                    "ref_number:${data[SigningSingUtil.REF_NUMBER.ordinal]}"
        )

        showLoading.set(true)
        var urlTest =
            "https://mconnecttest-signing.teda.th/api/v1/signing_sign/" + data[SigningSingUtil.REQUEST_ID.ordinal]
        var url = data[SigningSingUtil.URL.ordinal] + "/" + data[SigningSingUtil.REQUEST_ID.ordinal]
//            homeRepository.signingSign(url,
//                data[SigningSingUtil.TOKEN.ordinal], certCa, certChains,
//                object : SigningRepository.OnData {
//                    override fun onSuccess(data: SignedInfo) {
//                        showLoading.set(false)
//                        signedInfo.value = data
//                    }
//
//                    override fun onFailure(data: DataResponse) {
//                        showLoading.set(false)
//                        signedInfoError.value = data
//                    }
//                })
        showLoading.set(true)
//        viewModelScope.launch {
//            val result = homeRepository.signingSign(
//                url,
//                data[SigningSingUtil.TOKEN.ordinal], certCa, certChains, object : SigningRepository.OnData {
//                    override fun onSuccess(data: SignedInfo) {
//                        showLoading.set(false)
//                        signedInfo.value = data
//                    }
//
//                    override fun onFailure(data: DataResponse) {
//                        showLoading.set(false)
//                        signedInfoError.value = data
//                    }
//                }
//            )
//            showLoading.set(false)
//            when (result) {
//                is AppResult.Success -> {
////                    signedInfo.value = result.successData
//                    val gson = Gson()
//                    val type = object : TypeToken<SignedInfo>() {}.type
//                    signedInfo.value = gson.fromJson(result.successData, type)
//                    showError.value = null
//                }
//                is AppResult.Error -> {
//                    showError.value = result.exception.message
//                }
//            }
//        }
    }


    fun signingSignInfoSubmit(urls: String, signature: String) {
        val data = urls.split(";")
        println(
            "data => URL:${data[SigningSingUtil.URL.ordinal]} \n" +
                    "request_id:${data[SigningSingUtil.REQUEST_ID.ordinal]} \n" +
                    "token:${data[SigningSingUtil.TOKEN.ordinal]} \n" +
                    "ref_number:${data[SigningSingUtil.REF_NUMBER.ordinal]}"
        )
        var urlTest =
            "https://mconnecttest-signing.teda.th/api/v1/signing_sign/" + data[SigningSingUtil.REQUEST_ID.ordinal] + "/submit"
        var url =
            data[SigningSingUtil.URL.ordinal] + "/" + data[SigningSingUtil.REQUEST_ID.ordinal] + "/submit"
//        showLoading.set(true)
//        viewModelScope.launch {
//            homeRepository.signingSignSubmit(url,
//                data[SigningSingUtil.TOKEN.ordinal], signature,
//                object : SigningRepository.OnData {
//                    override fun onSuccess(data: SignedInfo) {
//                        showLoading.set(false)
//                        signedInfoSubmit.value = data
//                    }
//
//                    override fun onFailure(data: DataResponse) {
//                        showLoading.set(false)
//                        signedInfoSubmitError.value = data
//                    }
//                })
//
//        }

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
                    signedInfo.value = gson.fromJson(result.successData, type)
                    showError.value = null
                }
                is AppResult.Error -> {
                    showError.value = result.exception.message
                }
            }
        }
    }


    fun getSha256Hash(password: String): String? {
        return try {
            var digest: MessageDigest? = null
            try {
                digest = MessageDigest.getInstance("SHA-256")
            } catch (e1: NoSuchAlgorithmException) {
                e1.printStackTrace()
            }
            digest!!.reset()
            bin2hex(digest.digest(password.toByteArray()))
        } catch (ignored: Exception) {
            null
        }
    }

    private fun bin2hex(data: ByteArray): String? {
        val hex = StringBuilder(data.size * 2)
        for (b in data) hex.append(String.format("%02x", b and 0xFF))
        return hex.toString()
    }


}

//class HomeViewModelFactory(private val repository: SigningRepository) :
//    ViewModelProvider.Factory {
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return HomeViewModel(repository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//
//}