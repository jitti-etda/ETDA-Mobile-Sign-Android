package th.or.etda.teda.mobile

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import th.or.etda.teda.mobile.di.*


class TEDAMobileApplication : Application() {
    //    val applicationScope = CoroutineScope(SupervisorJob())
//    val database by lazy { CertificateDatabase.getDatabase(this) }
//    val repository by lazy { CertificateRepository(database.certificateDao()) }


    override fun onCreate() {
        super.onCreate()
        initKoin()
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

    }

    private fun initKoin() {
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@TEDAMobileApplication)
            androidFileProperties()
            modules(provideModules())
        }
    }

    private fun provideModules() = listOf(
        retrofitModule,
        apiModule,
        databaseModule,
        viewModelModule
    )

}

