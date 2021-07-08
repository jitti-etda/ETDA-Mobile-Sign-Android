package th.or.etda.teda.mobile.ui.cert

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import th.or.etda.teda.mobile.common.SingleLiveEvent
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.data.CertificateRepository

class CertListViewModel(var repository: CertificateRepository) : ViewModel() {


    private var certList = ArrayList<Certificate>()
    var certLiveData = SingleLiveEvent<List<Certificate>>()
    fun getCertificateAll(): SingleLiveEvent<List<Certificate>> {

        viewModelScope.launch {
            certList = repository.getCertAll() as ArrayList<Certificate>
            certLiveData.value = certList
        }
        return certLiveData
    }


    //    var isDelete = MutableLiveData<Boolean>()
    fun deleteCertificate(certificate: Certificate) {
        viewModelScope.launch {
//            isDelete.value = repository.deleteCert(certificate)
            repository.deleteCert(certificate)
            certList.remove(certificate)
//            certLiveData.value = certList
        }
    }

    val password = SingleLiveEvent<String>()

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
            if (certificate?.certChains != null) {
                certChainsLiveData.value = certificate.certChains!!
            }

        }
        return certChainsLiveData
    }


}
