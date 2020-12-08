package com.iqonic.store.razorpay

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.iqonic.store.R
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class PaymentActivity : Activity(), PaymentResultListener {

    private val TAG: String = PaymentActivity::class.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.payment_razorpay_main)
        Checkout.preload(applicationContext)

        val button: Button = findViewById(R.id.razerPayPayment)
        button.setOnClickListener {
            startPayment()
        }
    }

    private fun startPayment() {
        val activity: Activity = this
        val co = Checkout()
        co.setKeyID(getString(R.string.razor_key))
        try {
            val options = JSONObject()
            options.put("name", "Product Name")
            options.put("description", "Demoing Charges")
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("currency", "INR")
            options.put("amount", "100")

            val prefill = JSONObject()
            prefill.put("email", "sandipkalola1990@gmail.com")
            prefill.put("contact", "9638472777")

            options.put("prefill", prefill)
            co.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(activity, getString(R.string.lbl_error_in_payment) + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentError(errorCode: Int, response: String?) {
        try {
            Toast.makeText(this, getString(R.string.lbl_error_in_payment)+errorCode +"\n"+response, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        try {
            Toast.makeText(this, getString(R.string.lbl_payment_successful)+razorpayPaymentId, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }
    }
}