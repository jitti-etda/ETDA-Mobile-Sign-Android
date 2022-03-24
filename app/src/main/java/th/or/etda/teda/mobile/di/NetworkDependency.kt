package th.or.etda.teda.mobile.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.BuildConfig
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

val retrofitModule = module {

    fun provideOkHttpClient(): OkHttpClient {
        val timeoutInSeconds = 120
        val dispatcher = Dispatcher()
        dispatcher.maxRequests = 1
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val builder = OkHttpClient.Builder()
            .connectTimeout(timeoutInSeconds.toLong(), TimeUnit.SECONDS)
            .readTimeout(timeoutInSeconds.toLong(), TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor)

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(interceptor)
        return builder.build()
    }

    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://api-signing.teda.th/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    factory { provideGson() }
    factory { provideOkHttpClient() }
    factory { provideRetrofit(get(), get()) }
}

