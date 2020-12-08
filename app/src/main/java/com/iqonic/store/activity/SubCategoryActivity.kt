package com.iqonic.store.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.R
import com.iqonic.store.adapter.BaseAdapter
import com.iqonic.store.models.Category
import com.iqonic.store.models.RequestModel
import com.iqonic.store.models.StoreProductModel
import com.iqonic.store.utils.Constants
import com.iqonic.store.utils.Constants.TotalItem.TOTAL_ITEM_PER_PAGE
import com.iqonic.store.utils.Constants.TotalItem.TOTAL_SUB_CATEGORY_PER_PAGE
import com.iqonic.store.utils.extensions.*
import kotlinx.android.synthetic.main.activity_sub_category.*
import kotlinx.android.synthetic.main.item_subcategory.view.*
import kotlinx.android.synthetic.main.item_viewproductgrid.view.*
import kotlinx.android.synthetic.main.toolbar.*

class SubCategoryActivity : AppBaseActivity() {
    var image: String = ""
    private var mCategoryId: Int = 0
    private var showPagination: Boolean? = true
    private var mIsLoading = false
    private var countLoadMore = 1
    private val data: MutableMap<String, Int> =
        HashMap()

    private val subCategoryData: MutableMap<String, Int> =
        HashMap()
    private var isLastPage: Boolean? = false

    private val mSubCategoryAdapter =
        BaseAdapter<Category>(R.layout.item_subcategory, onBind = { view, model, _ ->
            view.tvSubCategory.text = model.name.getHtmlString()
            if (model.image != null) {
                if (model.image.src.isNotEmpty()) {
                    view.ivProducts.loadImageFromUrl(model.image.src)
                }
            }
            view.tvSubCategory.changeTextSecondaryColor()
            view.onClick {
                launchActivity<SubCategoryActivity> {
                    putExtra(Constants.KeyIntent.TITLE, model.name)
                    putExtra(Constants.KeyIntent.VIEWALLID, Constants.viewAllCode.CATEGORY)
                    putExtra(Constants.KeyIntent.KEYID, model.id)
                }
            }
        })

    private val mProductAdapter =
        BaseAdapter<StoreProductModel>(R.layout.item_viewproductgrid, onBind = { view, model, _ ->

            if (model.images!!.isNotEmpty()) {
                view.ivProduct.loadImageFromUrl(model.images!![0].src!!)
                image = model.images!![0].src!!
            }

            view.tvProductName.text = model.name?.getHtmlString()
            view.tvProductName.changeTextPrimaryColor()
            if (model.salePrice!!.isEmpty() && model.salePrice!!.isEmpty()) {
                view.tvDiscountPrice.text = model.price!!.currencyFormat()
                view.tvOriginalPrice.visibility = View.GONE
                view.tvOriginalPrice.text = ""
                view.tvSaleLabel.hide()
            } else {
                if (model.onSale) {
                    view.tvDiscountPrice.text = model.salePrice?.currencyFormat()
                    view.tvSaleLabel.show()
                    view.tvOriginalPrice.applyStrike()
                    view.tvOriginalPrice.text = model.regularPrice?.currencyFormat()
                    view.tvOriginalPrice.visibility = View.VISIBLE
                } else {
                    view.tvDiscountPrice.text = model.regularPrice?.currencyFormat()
                    view.tvOriginalPrice.text = ""
                    view.tvOriginalPrice.visibility = View.GONE
                    view.tvSaleLabel.hide()
                }
            }
            view.tvOriginalPrice.changeTextSecondaryColor()
            view.tvDiscountPrice.changePrimaryColor()
            view.tvAdd.changeBackgroundTint(getAccentColor())
            if (model.attributes!!.isNotEmpty()) {
                view.tvProductWeight.text = model.attributes?.get(0)?.options!![0]
                view.tvProductWeight.changeAccentColor()
            } else {
                view.tvProductWeight.text = ""
            }

            if (model.purchasable) {
                if (model.stockStatus == "instock") {
                    view.tvAdd.show()
                } else {
                    view.tvAdd.hide()
                }
            } else {
                view.tvAdd.hide()
            }

            view.tvAdd.onClick {
                addCart(model)
            }
            view.onClick {
                if (getProductDetailConstant() == 0) {
                    launchActivity<ProductDetailActivity1> {
                        putExtra(Constants.KeyIntent.PRODUCT_ID, model.id)

                    }
                } else {
                    launchActivity<ProductDetailActivity2> {
                        putExtra(Constants.KeyIntent.PRODUCT_ID, model.id)

                    }
                }
            }
        })

    private fun addCart(model: StoreProductModel) {
        if (isLoggedIn()) {
            val requestModel = RequestModel()
            if (model.type == "variable") {
                requestModel.pro_id = model.variations!![0]
            } else {
                requestModel.pro_id = model.id
            }
            requestModel.quantity = 1
            addItemToCart(requestModel, onApiSuccess = {
                fetchAndStoreCartData()
            })
        } else launchActivity<SignInUpActivity> { }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub_category)
        setToolbar(toolbar)
        mCategoryId = intent.getIntExtra(Constants.KeyIntent.KEYID, -1)
        title = intent.getStringExtra(Constants.KeyIntent.TITLE)
        mAppBarColor()
        llMain.changeBackgroundColor()
        rvCategory.apply {
            setHorizontalLayout(false)
            setHasFixedSize(true)
            rvCategory.adapter = mSubCategoryAdapter
            rvCategory.rvItemAnimation()
        }
        data["page"] = countLoadMore
        data["per_page"] = TOTAL_ITEM_PER_PAGE
        data["category"] = mCategoryId
        loadCategory(data)

        subCategoryData["per_page"] = TOTAL_SUB_CATEGORY_PER_PAGE
        subCategoryData["parent"] = mCategoryId
        loadSubCategory(subCategoryData)

        rvNewestProduct.apply {
            layoutManager = GridLayoutManager(this@SubCategoryActivity, 2)
            setHasFixedSize(true)
            rvNewestProduct.adapter = mProductAdapter
            rvNewestProduct.rvItemAnimation()
            if (showPagination!!) {
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        val countItem = recyclerView.layoutManager?.itemCount

                        var lastVisiblePosition = 0
                        if (recyclerView.layoutManager is LinearLayoutManager) {
                            lastVisiblePosition =
                                (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                        } else if (recyclerView.layoutManager is GridLayoutManager) {
                            lastVisiblePosition =
                                (recyclerView.layoutManager as GridLayoutManager).findLastCompletelyVisibleItemPosition()
                        }
                        if (isLastPage == false) {
                            if (lastVisiblePosition != 0 && !mIsLoading && countItem?.minus(1) == lastVisiblePosition) {
                                mIsLoading = true
                                countLoadMore = countLoadMore.plus(1)
                                data["page"] = countLoadMore
                                data["per_page"] = TOTAL_ITEM_PER_PAGE
                                data["category"] = mCategoryId
                                loadCategory(data)
                            }
                        }
                    }
                })
            }
        }

    }

    private fun loadCategory(data: MutableMap<String, Int>) {
        if (isNetworkAvailable()) {
            showProgress(true)
            getRestApiImpl().listAllCategoryProduct(data, onApiSuccess = {
                showProgress(false)
                if (countLoadMore == 1) {
                    mProductAdapter.clearItems()
                }
                if (it.isEmpty()) {
                    isLastPage = true
                }
                mIsLoading = false
                mProductAdapter.addMoreItems(it)
                if (mProductAdapter.itemCount == 0) {
                    rvNewestProduct.hide()
                } else {
                    rvNewestProduct.show()
                }

            }, onApiError = {
                showProgress(false)
                snackBar(it)
            })

        } else {
            showProgress(false)
            noInternetSnackBar()
        }
    }

    private fun loadSubCategory(data: MutableMap<String, Int>) {
        if (isNetworkAvailable()) {
            showProgress(true)
            getRestApiImpl().listAllCategory(data, onApiSuccess = {
                showProgress(false)
                if (countLoadMore == 1) {
                    mSubCategoryAdapter.clearItems()
                }
                mIsLoading = false
                mSubCategoryAdapter.addMoreItems(it)
                if (mSubCategoryAdapter.itemCount == 0) {
                    rvCategory.hide()
                } else {
                    rvCategory.show()
                }
            }, onApiError = {
                showProgress(false)
                snackBar(it)
            })

        }
    }
}
