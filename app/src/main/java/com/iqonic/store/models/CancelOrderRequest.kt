package com.iqonic.store.models

data class CancelOrderRequest(
    var status: String = "",
    var customer_note: String = ""
)

