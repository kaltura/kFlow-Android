package com.kaltura.kflow.utils

import com.kaltura.client.types.APIException

sealed class Resource<T> {
    data class Error<T>(val ex: APIException) : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
}