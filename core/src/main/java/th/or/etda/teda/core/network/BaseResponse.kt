package th.or.etda.teda.core.network

sealed class BaseResponse<out T>

data class SuccessResponse<out T>(val value: T): BaseResponse<T>()

data class ErrorResponse<out T>(val error: String): BaseResponse<T>()