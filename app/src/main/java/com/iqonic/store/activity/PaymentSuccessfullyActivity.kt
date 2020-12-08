package com.iqonic.store.activity

import android.os.Bundle
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.R
import com.iqonic.store.models.CreateOrderNotes
import com.iqonic.store.models.CreateOrderResponse
import com.iqonic.store.utils.BroadcastReceiverExt
import com.iqonic.store.utils.Constants
import com.iqonic.store.utils.extensions.*
import kotlinx.android.synthetic.main.activity_success_transaction.*
import kotlinx.android.synthetic.main.activity_success_transaction.rlMain

class PaymentSuccessfullyActivity : AppBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success_transaction)

        changeColor()
        val response: CreateOrderResponse =
            intent.getSerializableExtra(Constants.KeyIntent.ORDER_DATA) as CreateOrderResponse
        amount.text = response.total.toString().currencyFormat()
        transactionId.text = response.transaction_id
        orderId.text = response.order_key
        paidVia.text = response.payment_method
        transactionDate.text = convertToLocalDate(response.date_created)
        imgBack.onClick {
            finish()
        }
        BroadcastReceiverExt(this@PaymentSuccessfullyActivity) {
            onAction(Constants.AppBroadcasts.CART_COUNT_CHANGE) {
            }
        }
        tvDone.onClick {
            finish()
            fetchAndStoreCartData()
        }

        /**
         * Place Order Notes
         *
         */
        showProgress(true)
        val notes = CreateOrderNotes()
        notes.customer_note = true
        notes.note = "{\n" +
                "\"status\":\"Ordered\",\n" +
                "\"message\":\"Your order has been placed.\"\n" +
                "} "

        getRestApiImpl().addOrderNotes(response.id, request = notes, onApiSuccess = {
            showProgress(false)
        }, onApiError = {
            //showProgress(false)
        })

    }

    private fun changeColor() {
        lblOrderPlaceSuccessfully.changeTextPrimaryColor()
        lblTotalAmount.changeTextSecondaryColor()
        amount.changeTextPrimaryColor()
        lblTransaction.changeTextSecondaryColor()
        transactionId.changeTextPrimaryColor()
        lblOrderId.changeTextSecondaryColor()
        orderId.changeTextPrimaryColor()
        lblPaymentThrough.changeTextSecondaryColor()
        paidVia.changeTextPrimaryColor()
        lblTransactionFee.changeTextSecondaryColor()
        transactionDate.changeTextPrimaryColor()
        tvDone.changeBackgroundTint(getButtonColor())
        rlMain.changeBackgroundColor()
    }
}
