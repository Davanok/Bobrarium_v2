package com.example.bobrarium_v2

data class CustomState<T> (
    val isLoading: Boolean = false,
    val isSuccess: T? = null,
    val isError: String? = null
)

//sealed class CustomState<T>(data: T? = null, msg: String? = null){
//    class Loading<T>(data: T? = null): CustomState<Boolean>()
//    class Success<T>(data: T): CustomState<T>(data)
//    class Error<T>(msg: String? = null, data: T? = null): CustomState<T>(msg = msg)
//}
