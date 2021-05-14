package th.or.etda.teda.mobile.common

data class APIError(
    val result: String,
    val description: String
) {
    constructor() : this("", "")
}
