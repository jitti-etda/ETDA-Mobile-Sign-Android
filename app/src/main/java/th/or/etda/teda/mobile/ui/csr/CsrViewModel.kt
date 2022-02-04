package th.or.etda.teda.mobile.ui.csr

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import th.or.etda.teda.mobile.common.GenCsr
import th.or.etda.teda.mobile.common.SingleLiveEvent
import th.or.etda.teda.mobile.data.csr.Csr
import th.or.etda.teda.mobile.data.csr.CsrRepository
import java.security.PrivateKey


class CsrViewModel(private val csrRepository: CsrRepository) : ViewModel() {


    var privateKeyEvent = SingleLiveEvent<CsrTemp>()
//    var certEvent = SingleLiveEvent<SelfSignedCertificate>()

    fun genCsr(
        context: Context,
        name: String,
        organize: String,
        unit: String,
        state: String,
        city: String,
        country: String,
        email: String
    ) {
        val temp = GenCsr.writeData(context, name, organize, unit, state, city, country, email)
        privateKeyEvent.postValue(temp)
    }

//    fun genCsr2(
//        context: Context,
//        name: String,
//        organize: String,
//        unit: String,
//        state: String,
//        city: String,
//        country: String,
//        email: String
//    ) {
//        val cert = SelfSignedCertificate(context,name)
//        certEvent.postValue(cert)
//    }

    fun addCsr(csr: Csr) {
        viewModelScope.launch {

            csrRepository.insert(csr)

        }
    }


}
