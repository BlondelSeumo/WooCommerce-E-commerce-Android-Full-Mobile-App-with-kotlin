package com.iqonic.store.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.R
import com.iqonic.store.adapter.BaseAdapter
import com.iqonic.store.models.Order
import com.iqonic.store.utils.Constants
import com.iqonic.store.utils.Constants.KeyIntent.DATA
import com.iqonic.store.utils.Constants.SharedPref.KEY_ORDER_COUNT
import com.iqonic.store.utils.extensions.*
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.activity_order.rlMain
import kotlinx.android.synthetic.main.item_orderlist.view.*
import kotlinx.android.synthetic.main.layout_nodata.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.ParseException
import kotlin.math.roundToInt

class OrderActivity : AppBaseActivity() {

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private val mOrderAdapter =
        BaseAdapter<Order>(R.layout.item_orderlist, onBind = { view, model, _ ->

            if (model.line_items.isNotEmpty()) {
                if (model.line_items[0].product_images[0].src.isNotEmpty()) {
                    view.ivProduct.loadImageFromUrl(model.line_items[0].product_images[0].src)
                }
                if (model.line_items.size > 1) {
                    view.tvProductName.text =
                        model.line_items[0].name + " + " + (model.line_items.size - 1) + " " + getString(
                            R.string.lbl_more_item
                        )
                } else {
                    view.tvProductName.text = model.line_items[0].name
                }
            } else {
                view.tvProductName.text = getString(R.string.lbl_order_title) + model.id
            }

            view.tvPrice.text =
                (model.total.toFloat() - model.discount_total.toFloat()).roundToInt().toString()
                    .currencyFormat(model.currency)

            try {
                if (model.date_paid != null) {
                    if (model.transaction_id.isNullOrBlank()) {
                        view.tvInfo.text =
                            getString(R.string.lbl_transaction_via) + " " + model.payment_method + " (" + model.transaction_id + "). " + getString(
                                R.string.lbl_paid_on
                            ) + " " + convertOrderDataToLocalDate(model.date_paid.date)
                    } else {
                        view.tvInfo.text =
                            getString(R.string.lbl_transaction_via) + " " + model.payment_method + ". " + getString(
                                R.string.lbl_paid_on
                            ) + " " + convertOrderDataToLocalDate(model.date_paid.date)
                    }
                } else {
                    view.tvInfo.text =
                        getString(R.string.lbl_transaction_via) + " " + model.payment_method
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }


            if (model.status == Constants.OrderStatus.COMPLETED) {
                view.tvProductDeliveryDate.text =
                    convertOrderDataToLocalDate(model.date_modified.date)
            } else {
                view.llDeliveryDate.hide()
            }

            view.tvStatus.text = model.status
            view.rlMainOrder.onClick {
                launchActivity<OrderDescriptionActivity>(Constants.RequestCode.ORDER_CANCEL) {
                    putExtra(DATA, model)
                }
            }

            view.tvProductName.changeTextPrimaryColor()
            view.tvInfo.changeTextSecondaryColor()
            view.lblDelivery.changeTextSecondaryColor()
            view.tvProductDeliveryDate.changeTextPrimaryColor()
            view.tvPrice.changePrimaryColor()
            view.tvStatus.changeTextPrimaryColor()
            view.tvStatus.changeTextPrimaryColor()
        })

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.RequestCode.ORDER_CANCEL) {
                getOrderList()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        title = getString(R.string.lbl_my_orders)
        setToolbar(toolbar)
        mAppBarColor()
        rlMain.changeBackgroundColor()
        disableHardwareRendering(rvOrder)
        rvOrder.adapter = mOrderAdapter

        getOrderList()
    }

    private fun getOrderList() {
        getOrder(onApiSuccess = {
            if (it.size == 0) {
                rlNoData.show()
            } else {
                rlNoData.hide()
                mOrderAdapter.clearItems()
                mOrderAdapter.addItems(it)
                getSharedPrefInstance().setValue(KEY_ORDER_COUNT, it.size)
                sendOrderCountChangeBroadcast()
            }
        })
    }

}
