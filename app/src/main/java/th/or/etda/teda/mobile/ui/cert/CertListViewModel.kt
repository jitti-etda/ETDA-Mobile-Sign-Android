package th.or.etda.teda.mobile.ui.cert

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.data.CertificateRepository

class CertListViewModel (var repository: CertificateRepository) : ViewModel() {


    private var certList = ArrayList<Certificate>()
    var certLiveData = MutableLiveData<List<Certificate>>()
    fun getCertificateAll() {

        viewModelScope.launch {
            certList = repository.getCertAll() as ArrayList<Certificate>
            certLiveData.value = certList
        }
    }

//    var isDelete = MutableLiveData<Boolean>()
    fun deleteCertificate(certificate: Certificate){
        viewModelScope.launch {
//            isDelete.value = repository.deleteCert(certificate)
            repository.deleteCert(certificate)
            certList.remove(certificate)
            certLiveData.value = certList
        }
    }


}
