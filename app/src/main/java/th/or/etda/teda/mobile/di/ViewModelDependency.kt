package th.or.etda.teda.mobile.di

import android.content.Context
import org.koin.dsl.module
import th.or.etda.teda.mobile.api.SigningApi
import th.or.etda.teda.mobile.data.CertificateDao
import th.or.etda.teda.mobile.data.CertificateRepository
import th.or.etda.teda.mobile.extract.ExtractCAViewModel
import th.or.etda.teda.mobile.repository.SigningRepository
import th.or.etda.teda.mobile.ui.backupkey.BackupKeyViewModel
import th.or.etda.teda.mobile.ui.backupkey.googledrive.DriveViewModel
import th.or.etda.teda.mobile.ui.backupkey.password.BackupKeyPasswordViewModel
import th.or.etda.teda.mobile.ui.cert.CertListViewModel
import th.or.etda.teda.mobile.ui.home.HomeViewModel
import th.or.etda.teda.mobile.ui.importkey.ImportKeyViewModel
import th.or.etda.teda.mobile.ui.importkey.directory.DirectoryViewModel
import th.or.etda.teda.mobile.ui.importkey.password.ImportKeyPasswordViewModel
import th.or.etda.teda.mobile.ui.restorekey.RestoreKeyViewModel
import th.or.etda.teda.mobile.ui.restorekey.import.RestoreImportKeyPasswordViewModel
import th.or.etda.teda.mobile.ui.restorekey.password.RestoreKeyPasswordViewModel
import th.or.etda.teda.mobile.ui.sign.SignViewModel

val viewModelModule = module {

    fun provideExtractCAViewModel(dao: CertificateDao): ExtractCAViewModel {
        return ExtractCAViewModel(CertificateRepository(dao))
    }


    fun provideCertViewModel(dao: CertificateDao): CertListViewModel {
        return CertListViewModel(CertificateRepository(dao))
    }

    fun provideHomeViewModel(api: SigningApi, context: Context): HomeViewModel {
        return HomeViewModel(SigningRepository(api, context))
    }

    fun provideBackupViewModel(dao: CertificateDao): BackupKeyViewModel {
        return BackupKeyViewModel(CertificateRepository(dao))
    }

    fun provideImportKeyPasswordViewModel(dao: CertificateDao): ImportKeyPasswordViewModel {
        return ImportKeyPasswordViewModel(CertificateRepository(dao))
    }

    fun provideBackupPasswordViewModel(dao: CertificateDao): BackupKeyPasswordViewModel {
        return BackupKeyPasswordViewModel(CertificateRepository(dao))
    }

    fun provideRestoreViewModel(dao: CertificateDao): RestoreKeyViewModel {
        return RestoreKeyViewModel(CertificateRepository(dao))
    }

    fun provideImportViewModel(dao: CertificateDao): ImportKeyViewModel {
        return ImportKeyViewModel(CertificateRepository(dao))
    }

    fun provideRestoreImportKeyPasswordViewModel(dao: CertificateDao): RestoreImportKeyPasswordViewModel {
        return RestoreImportKeyPasswordViewModel(CertificateRepository(dao))
    }

    fun provideSignViewModel(api: SigningApi, context: Context): SignViewModel {
        return SignViewModel(SigningRepository(api, context))
    }
//
//    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl("https://mobilekey-uat-signing.teda.th")
//            .client(okHttpClient)
//            .addConverterFactory(ScalarsConverterFactory.create())
//            .addConverterFactory(GsonConverterFactory.create(gson))
//            .build()
//    }
    factory { provideHomeViewModel(get(), get()) }
    factory { provideSignViewModel(get(), get()) }
    factory { provideCertViewModel(get()) }
    factory { provideExtractCAViewModel(get()) }
    factory { DirectoryViewModel() }
    factory { DriveViewModel() }
    factory { provideBackupViewModel(get()) }
    factory { provideImportKeyPasswordViewModel(get()) }
    factory { provideBackupPasswordViewModel(get()) }
    factory { provideRestoreViewModel(get()) }
    factory { provideImportViewModel(get()) }
    factory { RestoreKeyPasswordViewModel() }
    factory { provideRestoreImportKeyPasswordViewModel(get()) }

//    factory { provideOkHttpClient() }
//    factory { provideRetrofit(get(), get()) }
}

