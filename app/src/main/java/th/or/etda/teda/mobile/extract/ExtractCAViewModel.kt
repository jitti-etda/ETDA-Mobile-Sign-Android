package th.or.etda.teda.mobile.extract

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import th.or.etda.teda.mobile.common.SingleLiveEvent
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.data.CertificateRepository


class ExtractCAViewModel(val repository: CertificateRepository) : ViewModel() {


    val caUri = SingleLiveEvent<Uri>()
    val password = SingleLiveEvent<String>()

    fun addCertificate(certificate: Certificate) {
        viewModelScope.launch {
            repository.insert(certificate)
        }
    }

    var certCaLiveData = SingleLiveEvent<String>()
    fun getCertCa(certName: String): SingleLiveEvent<String> {
        viewModelScope.launch {
            val certificate = repository.getCert(certName)
//            println("getCertificate => $certificate")
            println("cert_user: ${certificate?.certCa}")
            println("cert_chains: ${certificate?.certChains}")
            if (certificate?.certCa != null) {
                certCaLiveData.value = certificate.certCa!!
            }
        }
        return certCaLiveData
    }

    var certChainsLiveData = SingleLiveEvent<String>()
    fun getCertChains(certName: String): SingleLiveEvent<String> {
        viewModelScope.launch {
            val certificate = repository.getCert(certName)
//            println("getCertificate => $certificate")
//            println("cert_user: ${certificate?.certCa}")
//            println("cert_chains: ${certificate?.certChains}")
            if (certificate?.certChains != null) {
                certChainsLiveData.value = certificate.certChains!!
            }

        }
        return certChainsLiveData
    }



}

//class ExtractCaViewModelFactory(private val repository: CertificateRepository) :
//    ViewModelProvider.Factory {
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(ExtractCAViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return ExtractCAViewModel(repository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//
//}