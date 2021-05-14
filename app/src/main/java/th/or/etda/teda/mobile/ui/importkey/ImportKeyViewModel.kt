package th.or.etda.teda.mobile.ui.importkey

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.data.CertificateRepository


class ImportKeyViewModel (var repository: CertificateRepository): ViewModel() {


    var certLiveData = MutableLiveData<List<Certificate>>()
    fun getCertificateAll(): MutableLiveData<List<Certificate>> {

        viewModelScope.launch {
            certLiveData.value = repository.getCertAll()
        }
        return certLiveData
    }


}
