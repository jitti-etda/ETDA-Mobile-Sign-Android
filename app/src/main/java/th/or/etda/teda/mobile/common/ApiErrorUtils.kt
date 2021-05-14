package th.or.etda.teda.mobile.common

import com.google.gson.GsonBuilder
import retrofit2.Response


object ApiErrorUtils {

    fun parseError(response: Response<*>): APIError {

        val gson = GsonBuilder().create()
        var error: APIError

        try {
            error = gson.fromJson(response.errorBody()?.string(), APIError::class.java)
        } catch (e: Exception) {
            error = APIError("",response.code().toString()+" "+response.message())
//            e.message?.let { Log.d(TAG, it) }
//            return APIError()
        }
        return error
    }

}