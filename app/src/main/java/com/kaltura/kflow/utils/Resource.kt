package com.kaltura.kflow.utils

import com.kaltura.client.types.APIException

sealed class Resource {
    data class Error(val ex: APIException) : Resource()
    data class Success<T>(val data: T) : Resource()
}