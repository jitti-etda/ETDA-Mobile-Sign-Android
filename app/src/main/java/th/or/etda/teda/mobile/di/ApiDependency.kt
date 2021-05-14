package th.or.etda.teda.mobile.di

import org.koin.dsl.module
import retrofit2.Retrofit
import th.or.etda.teda.mobile.api.SigningApi

val apiModule = module {
    fun provideGitHubApi(retrofit: Retrofit) = retrofit.create(SigningApi::class.java)
    factory { provideGitHubApi(retrofit = get()) }
}