package th.or.etda.teda.mobile.di

import org.koin.dsl.module
import th.or.etda.teda.mobile.data.CertificateDatabase
import th.or.etda.teda.mobile.data.csr.CsrDatabase

val databaseModule = module {
    single { CertificateDatabase.getDatabase(get()) }
    single { get<CertificateDatabase>().certificateDao() }

    single { CsrDatabase.getDatabase(get()) }
    single { get<CsrDatabase>().csrDao() }
}