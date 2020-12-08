package com.iqonic.store.utils.oauthInterceptor

interface TimestampService {
    val timestampInSeconds: String
    val nonce: String
}