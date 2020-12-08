package com.iqonic.store.network

import androidx.annotation.NonNull


interface RestApiCallback<T, K> {

    fun onSuccess(aApiCode: Int, aSuccessResponse: T)

    fun onApiError(aApiCode: Int, aFailureResponse: K)

}
