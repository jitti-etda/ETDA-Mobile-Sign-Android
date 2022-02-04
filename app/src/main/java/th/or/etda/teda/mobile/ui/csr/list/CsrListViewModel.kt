package th.or.etda.teda.mobile.ui.csr.list

import android.os.FileUtils
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.spongycastle.cert.X509CertificateHolder
import org.spongycastle.cms.CMSProcessable
import org.spongycastle.cms.CMSSignedData
import org.spongycastle.cms.SignerInformation
import org.spongycastle.cms.SignerInformationStore
import org.spongycastle.util.Store
import th.or.etda.teda.mobile.common.RSA
import th.or.etda.teda.mobile.common.SingleLiveEvent
import th.or.etda.teda.mobile.data.csr.Csr
import th.or.etda.teda.mobile.data.csr.CsrRepository
import th.or.etda.teda.mobile.model.ExtrackP12
import th.or.etda.teda.mobile.util.KeyUtil.toBase64
import java.io.*
import java.security.*
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate


class CsrListViewModel(val csrRepository: CsrRepository) : ViewModel() {


    var csrLiveData = SingleLiveEvent<List<Csr>>()
    var extrackP12LiveData = SingleLiveEvent<ExtrackP12>()
    var csrLiveData2 = SingleLiveEvent<Csr>()
    var databaseSuccess = SingleLiveEvent<Boolean>()
    var chainsLiveData = SingleLiveEvent<String>()
    var chainsUpdateLiveData = SingleLiveEvent<Boolean>()
    var certUpdateLiveData = SingleLiveEvent<Boolean>()

    fun getCsrAll() {
        viewModelScope.launch {
            csrLiveData.value = csrRepository.getCsrAll();
        }
    }

    suspend fun updateStatus(csr: Csr) {
        databaseSuccess.postValue(csrRepository.updateCsr(csr))
    }

     fun deleteCsr(csr: Csr) {
        viewModelScope.launch {
            databaseSuccess.postValue(csrRepository.deleteCsr(csr))
        }

    }

    suspend fun importCrt(fileCrt: String) {
        var list = csrRepository.getCsrAll() as ArrayList<Csr>
        for (i in list.indices) {
//            list[i].privateKey?.let { Log.i("private", it) }
            try {
//                val binCpk: ByteArray = Base64.decode(list[i].privateKey, Base64.NO_WRAP)
//                val keyFactory = KeyFactory.getInstance("RSA")
//                val privateKeySpec = PKCS8EncodedKeySpec(binCpk)
//                var privateKey = keyFactory.generatePrivate(privateKeySpec)

                val fileInputStream = FileInputStream(File(fileCrt))
                val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
//                val caInput: InputStream = BufferedInputStream(fileInputStream)
//                val ca: java.security.cert.Certificate
//                var name = ""
                val ca = cf.generateCertificate(fileInputStream)
                var name = (ca as X509Certificate).subjectDN.toString()

                if (RSA.verify(ca.publicKey, "test", list[i].signKey)) {
                    Log.i("match", "true")
                    val cert =  ca.toBase64();
                    list[i].csrKey = cert
                    csrRepository.updateCsr(list[i])
                    csrLiveData2.postValue(list[i])
                } else {
                    Log.i("match", "false")
                }


            } catch (e: Exception) {
                e.printStackTrace()
            }
//                val keyStoreType: String = KeyStore.getDefaultType()
//                val keyStore: KeyStore = KeyStore.getInstance(keyStoreType)
//                keyStore.load(null, null)
//                keyStore.setCertificateEntry("ca", ca)
//                extrackP12LiveData.postValue(ExtrackP12(name, "", Base64.encodeToString(ca.encoded, Base64.NO_WRAP), privateKey))

        }
        certUpdateLiveData.postValue(true)
    }

    suspend fun importChains(fileCrt: String) {
        var list = csrRepository.getCsrAll() as ArrayList<Csr>
        for (i in list.indices) {
//            list[i].privateKey?.let { Log.i("private", it) }
            try {
//                val binCpk: ByteArray = Base64.decode(list[i].privateKey, Base64.NO_WRAP)
//                val keyFactory = KeyFactory.getInstance("RSA")
//                val privateKeySpec = PKCS8EncodedKeySpec(binCpk)
//                var privateKey = keyFactory.generatePrivate(privateKeySpec)


                val fileInputStream = FileInputStream(File(fileCrt))
                val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
                val caInput: InputStream = BufferedInputStream(fileInputStream)
                val ca: java.security.cert.Certificate


//                val inputStream: InputStream = File(fileCrt).inputStream()
//                val inputString = inputStream.bufferedReader().use { it.readText() }
//                println(inputString)

                try {
                    ca = cf.generateCertificate(caInput)
//                    print(ca.encoded);
//                    val chains = String(Base64.encode(ca.encoded, Base64.NO_WRAP))
                    val chains  =  ca.toBase64();
//                    Log.i("chains",chains);
                    list[i].chains = chains
                    csrRepository.updateCsr(list[i])

                } finally {
                    caInput.close()
                }



            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        chainsUpdateLiveData.postValue(true)
    }




}
