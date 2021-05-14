package th.or.etda.teda.mobile.di

import org.koin.dsl.module
import th.or.etda.teda.mobile.data.CertificateDatabase

val databaseModule = module {
    single { CertificateDatabase.getDatabase(get()) }
    single { get<CertificateDatabase>().certificateDao() }
}