package th.or.etda.teda.mobile.di

import android.content.Context
import org.koin.dsl.module
import th.or.etda.teda.mobile.api.SigningApi
import th.or.etda.teda.mobile.data.CertificateDao
import th.or.etda.teda.mobile.data.CertificateRepository
import th.or.etda.teda.mobile.repository.SigningRepository
import th.or.etda.teda.mobile.ui.cert.CertListViewModel
import th.or.etda.teda.mobile.ui.importkey.directory.DirectoryViewModel
import th.or.etda.teda.mobile.ui.importkey.password.ImportKeyPasswordViewModel
import th.or.etda.teda.mobile.ui.restorekey.RestoreKeyViewModel
import th.or.etda.teda.mobile.ui.restorekey.import.RestoreImportKeyPasswordViewModel
import th.or.etda.teda.mobile.ui.restorekey.password.RestoreKeyPasswordViewModel
import th.or.etda.teda.mobile.ui.sign.SignViewModel

val viewModelModule = module {




    fun provideCertViewModel(dao: CertificateDao): CertListViewModel {
        return CertListViewModel(CertificateRepository(dao))
    }


    fun provideImportKeyPasswordViewModel(dao: CertificateDao): ImportKeyPasswordViewModel {
        return ImportKeyPasswordViewModel(CertificateRepository(dao))
    }


    fun provideRestoreViewModel(dao: CertificateDao): RestoreKeyViewModel {
        return RestoreKeyViewModel(CertificateRepository(dao))
    }

    fun provideRestoreImportKeyPasswordViewModel(dao: CertificateDao): RestoreImportKeyPasswordViewModel {
        return RestoreImportKeyPasswordViewModel(CertificateRepository(dao))
    }

    fun provideSignViewModel(api: SigningApi, context: Context): SignViewModel {
        return SignViewModel(SigningRepository(api, context))
    }

    factory { provideSignViewModel(get(), get()) }
    factory { provideCertViewModel(get()) }
    factory { DirectoryViewModel() }
    factory { provideImportKeyPasswordViewModel(get()) }
    factory { provideRestoreViewModel(get()) }
    factory { RestoreKeyPasswordViewModel() }
    factory { provideRestoreImportKeyPasswordViewModel(get()) }

}

