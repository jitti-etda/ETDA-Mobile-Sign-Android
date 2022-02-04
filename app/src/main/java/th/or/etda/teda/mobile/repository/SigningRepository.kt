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

}

