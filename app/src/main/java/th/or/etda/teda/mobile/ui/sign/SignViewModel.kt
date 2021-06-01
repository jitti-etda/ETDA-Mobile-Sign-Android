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
        const val SIGN_ALGORITHM = "MD5WithRSA"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
//        const val ANDROID_KEY_STORE = "AndroidCAStore"

//        const val ANDROID_KEY_STORE = "pkcs12"

        const val ALIAS = "TEDA_KEY"
        private const val TAG = "HomeViewModel"
    }

    //    private var app: Application? = null
    private lateinit var privateKey: PrivateKey
    private lateinit var publicKey: PublicKey
    private val message: String = "Message"
    private lateinit var signMessage: String
    private lateinit var signWithKeyStore: String

    private var cameraProviderLiveData: MutableLiveData<ProcessCameraProvider>? = null

//    val signedInfo = MutableLiveData<SignedInfo?>()
//    val signedInfoSubmit = MutableLiveData<SignedInfo>()

    val signedInfo = SingleLiveEvent<SignedInfo>()
    val signedInfoSubmit = SingleLiveEvent<SignedInfo>()

    val signedInfoError = SingleLiveEvent<DataResponse>()
    val signedInfoSubmitError = SingleLiveEvent<DataResponse>()


    fun signWithKeyStore(signInfo: String, cert: Certificate): String {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
//        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null)
        val pvKey = keyStore.getKey(cert.certName, null) as PrivateKey

//        var pv = cert.pvk?.let { RSACrypt2.decryptAES(it) }
//
//
//        val keySpec = PKCS8EncodedKeySpec(pv)
//        val kf = KeyFactory.getInstance("RSA")
//        val privKey = kf.generatePrivate(keySpec)
//
//        println("KEY == >$privKey")
        val signature = Signature.getInstance(SIGN_ALGORITHM)
        signature.initSign(pvKey)
        signature.update(signInfo.toByteArray(Charsets.UTF_8))
        val encodeSign = Base64.encodeToString(signature.sign(), Base64.DEFAULT)
        signMessage = encodeSign
        println("signMessage => $signMessage")
        return signMessage
    }

    fun signWithKeyStore(signInfo: String, pvKey: String): String {
        val binCpk: ByteArray = Base64.decode(pvKey,2)
        val keyFactory = KeyFactory.getInstance("RSA")
        val privateKeySpec = PKCS8EncodedKeySpec(binCpk)
        var privateKey = keyFactory.generatePrivate(privateKeySpec)
//        var pv = cert.pvk?.let { RSACrypt2.decryptAES(it) }
//
//
//        val keySpec = PKCS8EncodedKeySpec(pv)
//        val kf = KeyFactory.getInstance("RSA")
//        val privKey = kf.generatePrivate(keySpec)
//
//        println("KEY == >$privKey")
        val signature = Signature.getInstance(SIGN_ALGORITHM)
        signature.initSign(privateKey)
        signature.update(signInfo.toByteArray(Charsets.UTF_8))
        val encodeSign = Base64.encodeToString(signature.sign(), Base64.DEFAULT)
        signMessage = encodeSign
        println("signMessage => $signMessage")
        return signMessage
    }

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


    var mockSignSuccess = "{\n" +
            "    \"description\": \"\",\n" +
            "    \"document\": {\n" +
            "        \"client_id\": \"docserver_18\",\n" +
            "        \"dnList\": null,\n" +
            "        \"document_category\": \"PDF\",\n" +
            "        \"document_info\": {\n" +
            "            \"business_type\": \"\",\n" +
            "            \"document_description\": \"Documents waiting to be signed\",\n" +
            "            \"document_name\": \"test\",\n" +
            "            \"document_type\": \"application/pdf\",\n" +
            "            \"ref_number\": \"187153\"\n" +
            "        },\n" +
            "        \"document_server_reject_at\": \"0001-01-01T00:00:00Z\",\n" +
            "        \"hash_result\": {\n" +
            "            \"description\": \"\",\n" +
            "            \"digest_method\": 1,\n" +
            "            \"document_hash\": \"J5Ydulsnl6DSAsxDYW/XLEHaHi9t3styrBN9khaXh5M=\",\n" +
            "            \"result\": \"accept\",\n" +
            "            \"xml_namespace\": \"\"\n" +
            "        },\n" +
            "        \"id\": \"6035db315b215d00075f90f1\",\n" +
            "        \"other_info\": {\n" +
            "            \"info\": [\n" +
            "                {\n" +
            "                    \"key\": \"company_name\",\n" +
            "                    \"value\": \"DOCUMENT SERVER UAT\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"request_token\": \"Rsq7k75FvGaErQgusTnh\",\n" +
            "        \"signedinfo\": {\n" +
            "            \"Status\": \"SUCCESS\",\n" +
            "            \"description\": \"\",\n" +
            "            \"signatureId\": \"MCEwFTETMBEGA1UEAwwKRVREQSBDQSBHMgIIUUNnAUVcNxk=\",\n" +
            "            \"signedInfo\": \"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\",\n" +
            "            \"signedInfoDigest\": \"\",\n" +
            "            \"xadesSignedProperties\": \"\"\n" +
            "        },\n" +
            "        \"status\": \"reject\",\n" +
            "        \"user_id\": \"P12TEST_james_finema\"\n" +
            "    },\n" +
            "    \"result\": \"accept\"\n" +
            "}"


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

//        showLoading.set(true)
        var urlTest =
            "https://api-uat.teda.th/signingserver/api/v2/signing_sign/{request_id}/" + data[SigningSingUtil.REQUEST_ID.ordinal]
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
        //Mock success
//        val gson = Gson()
//        val type = object : TypeToken<SignedInfo>() {}.type
//        signedInfo.value = gson.fromJson(mockSignSuccess, type)
//        showError.value = ""


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

    var mockSignSubmitSuccess = "{\n" +
            "    \"description\": \"\",\n" +
            "    \"document\": {\n" +
            "        \"client_id\": \"docserver_18\",\n" +
            "        \"dnList\": null,\n" +
            "        \"document_category\": \"PDF\",\n" +
            "        \"document_info\": {\n" +
            "            \"business_type\": \"\",\n" +
            "            \"document_description\": \"Documents waiting to be signed\",\n" +
            "            \"document_name\": \"test\",\n" +
            "            \"document_type\": \"application/pdf\",\n" +
            "            \"ref_number\": \"187153\"\n" +
            "        },\n" +
            "        \"document_server_reject_at\": \"0001-01-01T00:00:00Z\",\n" +
            "        \"hash_result\": {\n" +
            "            \"description\": \"\",\n" +
            "            \"digest_method\": 1,\n" +
            "            \"document_hash\": \"J5Ydulsnl6DSAsxDYW/XLEHaHi9t3styrBN9khaXh5M=\",\n" +
            "            \"result\": \"accept\",\n" +
            "            \"xml_namespace\": \"\"\n" +
            "        },\n" +
            "        \"id\": \"6035db315b215d00075f90f1\",\n" +
            "        \"other_info\": {\n" +
            "            \"info\": [\n" +
            "                {\n" +
            "                    \"key\": \"company_name\",\n" +
            "                    \"value\": \"DOCUMENT SERVER UAT\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"request_token\": \"Rsq7k75FvGaErQgusTnh\",\n" +
            "        \"signedinfo\": {\n" +
            "            \"Status\": \"SUCCESS\",\n" +
            "            \"description\": \"\",\n" +
            "            \"signatureId\": \"MCEwFTETMBEGA1UEAwwKRVREQSBDQSBHMgIIUUNnAUVcNxk=\",\n" +
            "            \"signedInfo\": \"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\",\n" +
            "            \"signedInfoDigest\": \"\",\n" +
            "            \"xadesSignedProperties\": \"\"\n" +
            "        },\n" +
            "        \"status\": \"reject\",\n" +
            "        \"user_id\": \"P12TEST_james_finema\"\n" +
            "    },\n" +
            "    \"result\": \"accept\"\n" +
            "}"

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
//        val gson = Gson()
//        val type = object : TypeToken<SignedInfo>() {}.type
//        signedInfoSubmit.value = gson.fromJson(mockSignSubmitSuccess, type)
//        showError.value = ""

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
                    showError.value = ""
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