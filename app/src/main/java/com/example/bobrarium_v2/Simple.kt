package com.example.bobrarium_v2

sealed class Simple(val err: Throwable? = null, val msg: Int? = null){
    object Loading : Simple()
    object Success: Simple()
    class Fail(err: Throwable? = null, msg: Int? = null): Simple(err, msg)
}
