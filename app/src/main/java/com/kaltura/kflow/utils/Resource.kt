package com.kaltura.kflow.utils

import com.kaltura.client.types.APIException

sealed class Resource<T> {
    data class Error<T>(val ex: APIException) : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
}

fun <T> Resource<T>.isSuccess() = this is Resource.Success

fun <T> Resource<T>.getSuccessData() = (this as Resource.Success).data

fun <T> Resource<T>.getErrorData() = (this as Resource.Error).ex