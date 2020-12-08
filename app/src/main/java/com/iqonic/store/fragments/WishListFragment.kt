package com.iqonic.store.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.R
import com.iqonic.store.activity.ProductDetailActivity1
import com.iqonic.store.activity.ProductDetailActivity2
import com.iqonic.store.adapter.BaseAdapter
import com.iqonic.store.models.RequestModel
import com.iqonic.store.models.WishList
import com.iqonic.store.utils.Constants
import com.iqonic.store.utils.extensions.*
import kotlinx.android.synthetic.main.fragment_wishlist.*
import kotlinx.android.synthetic.main.item_wishlist.view.*
import kotlinx.android.synthetic.main.item_wishlist.view.ivProduct
import kotlinx.android.synthetic.main.item_wishlist.view.tvDiscountPrice
import kotlinx.android.synthetic.main.item_wishlist.view.tvOriginalPrice
import kotlinx.android.synthetic.main.item_wishlist.view.tvProductName
import kotlinx.android.synthetic.main.layout_nodata.*

class WishListFragment : BaseFragment() {

    private val mListAdapter = BaseAdapter<WishList>(R.layout.item_wishlist, onBind = { view, model, _ ->
        if (activity !== null) {
            if(model.sale_price.isNotEmpty()){
                view.tvOriginalPrice.applyStrike()
                view.tvDiscountPrice.text = model.sale_price.currencyFormat()
                view.tvOriginalPrice.text = model.regular_price.currencyFormat()
            }
            else
            {
                if (model.regular_price.isEmpty()) {
                    view.tvDiscountPrice.text =model.price.currencyFormat()
                } else {
                    view.tvDiscountPrice.text =model.regular_price.currencyFormat()
                }
            }
            view.tvProductName.text = model.name
            view.tvProductName.changeTextPrimaryColor()
            view.tvDiscountPrice.changeTextPrimaryColor()
            view.tvOriginalPrice.changeTextSecondaryColor()
            if (model.full.isNotEmpty()) view.ivProduct.loadImageFromUrl(model.full)
        }
        view.onClick {
            if(getProductDetailConstant()==0){
                activity?.launchActivity<ProductDetailActivity1> {
                    putExtra(Constants.KeyIntent.PRODUCT_ID, model.pro_id)

                }
            }
            else{
                activity?.launchActivity<ProductDetailActivity2> {
                    putExtra(Constants.KeyIntent.PRODUCT_ID, model.pro_id)
                }
            }
        }
        view.ivMoveToCart.onClick {
            val requestModel = RequestModel()
            requestModel.pro_id = model.pro_id
            requestModel.quantity = 1
            activity?.setResult(Activity.RESULT_OK)
            getRestApiImpl().addItemToCart(request = requestModel, onApiSuccess = {
                if (activity == null) return@addItemToCart
                snackBar(getString(R.string.success_add))
                activity?.sendCartBroadcast()
                activity!!.fetchAndStoreCartData()
                (activity as AppBaseActivity).removeFromWishList(requestModel)
                {
                    if (it) snackBar(getString(R.string.lbl_remove)); hideProgress()
                    wishListItemChange()
                }
            }, onApiError = {
                if (activity == null) return@addItemToCart
                snackBar(it)
                (activity as AppBaseActivity).removeFromWishList(requestModel)
                {
                    if (it) snackBar(getString(R.string.lbl_remove)); hideProgress()
                    wishListItemChange()
                }
                (activity as AppBaseActivity).fetchAndStoreCartData()

            })
        }
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_wishlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvWishList.apply {
            layoutManager = GridLayoutManager(activity, 2)
            adapter = mListAdapter
            setHasFixedSize(true)
            rvItemAnimation()

        }
        wishListItemChange()
        llMain.changeBackgroundColor()
    }

    private fun wishListItemChange() {
        if (isNetworkAvailable()) {
            if (activity == null) return
            (activity as AppBaseActivity).showProgress(true)
            getRestApiImpl().getWishList(onApiSuccess = {
                if (activity == null) return@getWishList
                (activity as AppBaseActivity).showProgress(false)
                getSharedPrefInstance().setValue(Constants.SharedPref.KEY_WISHLIST_COUNT, it.size)
                if (it.isNullOrEmpty()) {
                    rvWishList.hide()
                    rlNoData.show()
                } else {
                    rlNoData.hide()
                    rvWishList.show()
                    mListAdapter.clearItems()
                    mListAdapter.addItems(it)
                }
            }, onApiError = {
                if (activity == null) return@getWishList
                (activity as AppBaseActivity).showProgress(false)
                if (it == "no product available") {
                    getSharedPrefInstance().setValue(Constants.SharedPref.KEY_WISHLIST_COUNT, 0)
                    (activity as AppBaseActivity).sendWishlistBroadcast()
                    rlNoData.show()
                    rvWishList.hide()
                } else {
                    (activity as AppBaseActivity).snackBarError(it)
                }
            })
        } else {
            if (activity == null) return
            (activity as AppBaseActivity).showProgress(false)
            (activity as AppBaseActivity).noInternetSnackBar()
        }
    }

}
