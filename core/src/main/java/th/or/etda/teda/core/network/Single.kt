package th.or.etda.teda.core.network

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException

abstract class Single<T>(): SingleExecute<Single<T>>() {
    fun execute(
        onResponse: (BaseResponse<T>) -> Unit
    ): Disposable {
        return this.execute()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( {
                onResponse(SuccessResponse(it))
            }, {
                onResponse(ErrorResponse(it.getErrorString()))
            })
    }

    private fun Throwable.getErrorString(): String {
        var stringReturn = ""
        try {
            (this as HttpException).response()?.errorBody()?.string()?.let {
                val error = JSONObject(it)
                stringReturn =  error.get("message").toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            return stringReturn
        }
    }
}