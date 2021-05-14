
package th.or.etda.teda.mobile.api

import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/**
 * Used to connect to the Unsplash API to fetch photos
 */
interface SigningApi {



//    @Headers("Content-Type: application/json")
//    @POST
//     fun signingSignQrcode(@Url url: String,@Header("Token") token:String,@Body body: String): Call<String>

    @Headers("Content-Type: application/json")
    @POST
    suspend fun signingSignQrcode(@Url url: String,@Header("Token") token:String,@Body body: String): Response<String>

    @Headers("Content-Type: application/json")
    @POST
    suspend fun signingSignSubmit(@Url url: String,@Header("Token") token:String,@Body body: String): Response<String>

}
