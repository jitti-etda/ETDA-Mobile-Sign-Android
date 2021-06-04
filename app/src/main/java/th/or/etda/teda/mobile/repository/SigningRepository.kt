package th.or.etda.teda.mobile.repository

import android.content.Context
import android.util.Log
import org.json.JSONObject
import th.or.etda.teda.mobile.api.SigningApi
import th.or.etda.teda.mobile.common.AppResult
import th.or.etda.teda.mobile.common.NetworkManager.isOnline
import th.or.etda.teda.mobile.common.Utils.handleApiError
import th.or.etda.teda.mobile.common.Utils.handleSuccess
import th.or.etda.teda.mobile.common.noNetworkConnectivityError
import th.or.etda.teda.mobile.model.SignedInfo

class SigningRepository(val signingApi: SigningApi, val context: Context) {

//    fun signingSign(onSignedInfoResponse: OnSignedInfoResponse) {
//        homeApi.signingSign().enqueue(object : retrofit2.Callback<SignedInfo> {
//            override fun onResponse(call: Call<SignedInfo>, response: Response<SignedInfo>) {
//                onSignedInfoResponse.onSuccess((response.body() as SignedInfo))
//            }
//
//            override fun onFailure(call: Call<SignedInfo>, t: Throwable) {
//                onSignedInfoResponse.onFailure()
//            }
//        })
//    }
//
//    interface OnSignedInfoResponse {
//        fun onSuccess(data: SignedInfo)
//        fun onFailure()
//    }

    suspend fun signingSign(
        url: String,
        token: String,
        certCa: String,
        certChains: String
    ): AppResult<String> {
        var json = JSONObject()
        json.put("cert", certCa)
        json.put("chains", certChains)
        var jsonKey = JSONObject()
        jsonKey.put("key", json)
        Log.i("p12",jsonKey.toString())
        if (isOnline(context)) {
            return try {
                val response = signingApi.signingSignQrcode(url, token, jsonKey.toString())
                return if (response.isSuccessful) {
                    handleSuccess(response)
                } else {
                    handleApiError(response)
                }
            } catch (e: Exception) {
                AppResult.Error(e)
            }
        } else {

            return context.noNetworkConnectivityError()
        }
    }


//    fun signingSign(
//        url: String,
//        token: String,
//        certCa: String,
//        certChains: String,
//        onData: OnData
//    ) {
//        var json = JSONObject()
//        json.put("cert", certCa)
//        json.put("chains", certChains)
//        var jsonKey = JSONObject()
//        jsonKey.put("key", json)
//
//        signingApi.signingSignQrcode(url, token, jsonKey.toString())
//            .enqueue(object : retrofit2.Callback<String> {
//                override fun onResponse(call: Call<String>, response: Response<String>) {
//
//                    if (response.isSuccessful) {
//                        response.body()?.let {
//                            val gson = Gson()
//                            val type = object : TypeToken<SignedInfo>() {}.type
//                            var signedInfo: SignedInfo = gson.fromJson(it, type)
//                            onData.onSuccess(signedInfo)
//                        }
//                    } else {
//                        val gson = Gson()
//                        val type = object : TypeToken<DataResponse>() {}.type
//                        var errorResponse: DataResponse =
//                            gson.fromJson(response.errorBody()?.charStream(), type)
//
//                        onData.onFailure(errorResponse)
//                    }
//
//                }
//
//                override fun onFailure(call: Call<String>, t: Throwable) {
////                t.message?.let {
////                    onData.onFailure(it)
////                }
//                }
//            })
//    }

    suspend fun signingSignSubmit(
        url: String,
        token: String,
        signature: String
    ): AppResult<String> {
        var json = JSONObject()
        json.put("signature", signature)
        if (isOnline(context)) {
            return try {
                val response = signingApi.signingSignSubmit(url, token, json.toString())
                return if (response.isSuccessful) {
                    handleSuccess(response)
                } else {
                    handleApiError(response)
                }
            } catch (e: Exception) {
                AppResult.Error(e)
            }
        } else {

            return context.noNetworkConnectivityError()
        }
    }

//    fun signingSignSubmit(url: String, token: String, signature: String, onData: OnData) {
//        var json = JSONObject()
//        json.put("signature", signature)
//        var jsonKey = JSONObject()
//        jsonKey.put("key", json)
//
//        signingApi.signingSignSubmit(url, token, json.toString())
//            .enqueue(object : retrofit2.Callback<String> {
//                override fun onResponse(call: Call<String>, response: Response<String>) {
//
//                    if (response.isSuccessful) {
//                        response.body()?.let {
//                            val gson = Gson()
//                            val type = object : TypeToken<SignedInfo>() {}.type
//                            var signedInfo: SignedInfo = gson.fromJson(it, type)
//                            onData.onSuccess(signedInfo)
//                        }
//                    } else {
//                        val gson = Gson()
//                        val type = object : TypeToken<DataResponse>() {}.type
//                        var errorResponse: DataResponse =
//                            gson.fromJson(response.errorBody()?.charStream(), type)
//
//                        onData.onFailure(errorResponse)
//                    }
//
//                }
//
//                override fun onFailure(call: Call<String>, t: Throwable) {
////                t.message?.let {
////                    onData.onFailure(it)
////                }
//                }
//            })
//    }


    interface OnData {
        fun onSuccess(data: SignedInfo)
        fun onFailure(data: DataResponse)
    }
}

