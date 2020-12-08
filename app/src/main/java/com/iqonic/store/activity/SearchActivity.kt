package com.iqonic.store.activity

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.R
import com.iqonic.store.adapter.BaseAdapter
import com.iqonic.store.models.RequestModel
import com.iqonic.store.models.SearchRequest
import com.iqonic.store.models.StoreProductModel
import com.iqonic.store.models.Term
import com.iqonic.store.utils.Constants
import com.iqonic.store.utils.extensions.*
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.item_filter_brand.view.*
import kotlinx.android.synthetic.main.item_viewproductgrid.view.*
import kotlinx.android.synthetic.main.layout_filter.*
import kotlinx.android.synthetic.main.layout_nodata.*


class SearchActivity : AppBaseActivity() {
    private var mTerms = ArrayList<Term>()
    private var mSelectedSlug: ArrayList<String> = ArrayList()
    private var mSelectedTermId: ArrayList<String> = ArrayList()
    private var mSearchQuery = ""
    private var mIsFilterDataLoaded = false
    private var mPage = 1
    private var mIsLoading = false
    private var searchRequest = SearchRequest()
    private var totalPages = 0
    private val mProductAdapter = BaseAdapter<StoreProductModel>(
        R.layout.item_viewproductgrid,
        onBind = { view, model, _ ->
            if (model.images!![0].src!!.isNotEmpty()) {
                view.ivProduct.loadImageFromUrl(model.images!![0].src!!)
            }

            val mName = model.name!!.split(",")

            view.tvProductName.text = mName[0]
            view.tvProductName.changeTextPrimaryColor()
            if (!model.onSale) {
                view.tvDiscountPrice.text = model.price!!.currencyFormat()
                view.tvOriginalPrice.visibility = View.VISIBLE
                view.tvOriginalPrice.text = ""
            } else {
                if (model.salePrice!!.isNotEmpty()) {
                    view.tvDiscountPrice.text = model.salePrice!!.currencyFormat()
                    view.tvOriginalPrice.applyStrike()
                    view.tvOriginalPrice.text = model.regularPrice!!.currencyFormat()
                    view.tvOriginalPrice.visibility = View.VISIBLE
                } else {
                    view.tvOriginalPrice.visibility = View.VISIBLE
                    if (model.regularPrice!!.isEmpty()) {
                        view.tvOriginalPrice.text = ""
                        view.tvDiscountPrice.text = model.price!!.currencyFormat()
                    } else {
                        view.tvOriginalPrice.text = ""
                        view.tvDiscountPrice.text = model.regularPrice!!.currencyFormat()
                    }
                }
            }
            view.tvOriginalPrice.changeTextSecondaryColor()
            view.tvDiscountPrice.changeTextPrimaryColor()
            view.tvAdd.changeBackgroundTint(getAccentColor())
            if (model.attributes!!.isNotEmpty()) {
                view.tvProductWeight.text = model.attributes!![0].options!![0]
            }
            if (model.in_stock) {
                view.tvAdd.show()
            } else {
                view.tvAdd.hide()
            }
            if (!model.purchasable) {
                view.tvAdd.hide()
            } else {
                view.tvAdd.show()
            }
            view.onClick {
                if (getProductDetailConstant() == 0) {
                    launchActivity<ProductDetailActivity1> {
                        putExtra(Constants.KeyIntent.PRODUCT_ID, model.id)
                        putExtra(Constants.KeyIntent.DATA, model)
                    }
                } else {
                    launchActivity<ProductDetailActivity2> {
                        putExtra(Constants.KeyIntent.PRODUCT_ID, model.id)
                        putExtra(Constants.KeyIntent.DATA, model)
                    }
                }
            }
            view.tvAdd.onClick {
                addCart(model.id)
            }
        })

    private fun addCart(modelId: Int) {
        if (isLoggedIn()) {
            val requestModel = RequestModel()
            requestModel.pro_id = modelId
            requestModel.quantity = 1
            addItemToCart(requestModel, onApiSuccess = {
            })
        } else launchActivity<SignInUpActivity> { }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        toolbar.title = ""
        toolbar.navigationIcon!!.setColorFilter(
            resources.getColor(R.color.colorBackArrow),
            PorterDuff.Mode.SRC_ATOP
        )
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        setToolbar(toolbar)
        mAppBarColor()
        llMain.changeBackgroundColor()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                mPage = 1
                mSearchQuery = query!!
                searchRequest.text = mSearchQuery
                searchRequest.page = mPage
                loadProducts()

                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()) {
                    mSearchQuery = ""
                    mPage = 1
                    searchRequest.text = null
                    searchRequest.page = mPage
                }
                return true
            }

        })
        searchView.setOnCloseListener {
            mSearchQuery = ""
            mPage = 1
            searchRequest.text = mSearchQuery
            searchRequest.page = mPage
            true
        }

        val searchEditText =
            searchView.findViewById<View>(R.id.search_src_text) as EditText
        searchEditText.setTextColor(resources.getColor(R.color.white))
        searchEditText.setHintTextColor(resources.getColor(R.color.white))
        searchView.onActionViewExpanded()
        aSearch_rvSearch.apply {
            adapter = mProductAdapter
            layoutManager = GridLayoutManager(this@SearchActivity, 2)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val countItem = recyclerView.layoutManager?.itemCount
                    val lastVisiblePosition =
                        (recyclerView.layoutManager as GridLayoutManager).findLastCompletelyVisibleItemPosition()
                    if (lastVisiblePosition != 0 && !mIsLoading && countItem?.minus(1) == lastVisiblePosition && totalPages > mPage) {
                        mIsLoading = true
                        mPage = mPage.plus(1)
                        searchRequest.page = mPage
                        loadProducts()
                    }
                }
            })

        }
        getProductAttribute()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val item = menu!!.findItem(R.id.action_filter)
        val icon = resources.getDrawable(R.drawable.ic_filter)
        icon.setColorFilter(Color.parseColor(getTextTitleColor()), PorterDuff.Mode.SRC_IN)
        item.icon = icon
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed();true
            }
            R.id.action_filter -> {
                if (mIsFilterDataLoaded) openFilterBottomSheet()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadProducts() {
        if (isNetworkAvailable()) {
            showProgress(true)
            getRestApiImpl().search(searchRequest, onApiSuccess = {
                showProgress(false)
                if (mPage == 1) {
                    mProductAdapter.clearItems()
                }
                mIsLoading = false
                totalPages = it.numOfPages
                if (it.data != null) {
                    mProductAdapter.addMoreItems(it.data!!)
                }
                if (mProductAdapter.itemCount == 0) {
                    rlNoData.show()
                    aSearch_rvSearch.hide()
                } else {
                    rlNoData.hide()
                    aSearch_rvSearch.show()
                }


            }, onApiError = {
                showProgress(false)
                if (it == "Sorry! No Product Available") {
                    if (mPage == 1) {
                        mProductAdapter.clearItems()
                    }
                    rlNoData.show()
                }
                snackBar(it)
            })
        }
    }

    private fun getProductAttribute() {
        if (isNetworkAvailable()) {
            showProgress(true)
            getRestApiImpl().listAllProductAttribute(onApiSuccess = {
                showProgress(false)
                mIsFilterDataLoaded = true

                it.attribute!!.forEachIndexed { index, storeProductAttribute ->
                    val teams = Term()
                    teams.name = it.attribute[index].name
                    teams.isParent = true

                    mTerms.add(teams)
                    storeProductAttribute.terms.forEachIndexed { _, term ->
                        mTerms.add(term)
                    }
                }

            }, onApiError = {
                showProgress(false)
                snackBar(it)
            })
        }
    }


    private var mSelectedPrice: ArrayList<Int> = ArrayList()

    private fun openFilterBottomSheet() {
        val filterDialog =
            BottomSheetDialog(this); filterDialog.setContentView(R.layout.layout_filter)
        val priceArray = arrayOf(
            "1".currencyFormat(),
            "100".currencyFormat(),
            "500".currencyFormat(),
            "700".currencyFormat(),
            "1000".currencyFormat(),
            "2000".currencyFormat(),
            "5000".currencyFormat(),
            "7000".currencyFormat(),
            "10000".currencyFormat(),
            "15000".currencyFormat(),
            "20000".currencyFormat()

        )
        val priceArray2 =
            arrayOf(
                "1",
                "100",
                "500",
                "700",
                "1000",
                "2000",
                "5000",
                "7000",
                "10000",
                "15000",
                "20000"
            )
        filterDialog.lblFilter.changeTextPrimaryColor()
        filterDialog.tvAttributesName.changeTint(getAccentColor())
        filterDialog.tvApply.changeTint(getPrimaryColor())
        filterDialog.lblMin.changeTextPrimaryColor()
        filterDialog.lblMax.changeTextPrimaryColor()
        filterDialog.ivClose.changeBackgroundImageTint(getPrimaryColor())
        filterDialog.rangebar1.tickTopLabels = priceArray
        filterDialog.rangebar1.setConnectingLineColor(Color.parseColor(getAccentColor()))
        filterDialog.rangebar1.leftSelectorColor=Color.parseColor(getAccentColor())
        filterDialog.rangebar1.rightSelectorColor=Color.parseColor(getAccentColor())

        if (mSelectedPrice.size == 2) {
            filterDialog.rangebar1.setRangePinsByValue(
                priceArray2.indexOf(mSelectedPrice[0].toString()).toFloat(),
                priceArray2.indexOf(mSelectedPrice[1].toString()).toFloat()
            )
        }

        val brandAdapter =
            BaseAdapter<Term>(R.layout.item_filter_brand, onBind = { view, model, _ ->

                if (model.isParent) {
                    view.tvAttributesName.visibility = View.VISIBLE
                    view.termsView.visibility = View.GONE
                    view.tvAttributesName.text = model.name
                } else {
                    view.termsView.visibility = View.VISIBLE
                    view.tvAttributesName.visibility = View.GONE
                    view.tvBrandName.text = model.name

                    if (model.isSelected) {
                        view.tvBrandName.changePrimaryColor()
                        view.ivSelect.setImageResource(R.drawable.ic_check)
                        view.ivSelect.changeBackgroundImageTint(getPrimaryColor())
                        view.ivSelect.setStrokedBackground(
                            Color.parseColor(getAccentColor()),
                            Color.parseColor(getAccentColor()),
                            0.4f
                        )
                    } else {
                        view.tvBrandName.changeTextSecondaryColor()
                        view.ivSelect.setImageResource(0)
                        view.ivSelect.setStrokedBackground(color(R.color.checkbox_color))
                    }
                }
                view.tvAttributesName.changeTint(getAccentColor())
            })
        brandAdapter.onItemClick = { _, _, model ->
            model.isSelected = !(model.isSelected)
            brandAdapter.notifyDataSetChanged()
        }

        filterDialog.rcvBrands.apply {
            setVerticalLayout(); adapter = brandAdapter
        }
        brandAdapter.clearItems()
        brandAdapter.addItems(mTerms)

        filterDialog.tvApply.onClick {
            mSelectedSlug.clear()
            mSelectedTermId.clear()
            mSelectedPrice.clear()
            val map = HashMap<String, ArrayList<Int>>()
            mTerms.forEachIndexed { _, storeProductAttribute ->
                if (storeProductAttribute.isSelected) {
                    if (map.containsKey(storeProductAttribute.taxonomy)) {
                        map[storeProductAttribute.taxonomy]?.add(storeProductAttribute.term_id)
                    } else {
                        val list = ArrayList<Int>()
                        list.add(storeProductAttribute.term_id)
                        map[storeProductAttribute.taxonomy] = list
                    }
                    mSelectedSlug.add(storeProductAttribute.slug)
                    mSelectedTermId.add(storeProductAttribute.term_id.toString())
                }
            }
            mSelectedPrice.add(priceArray2[filterDialog.rangebar1.leftPinValue.toInt()].toInt())
            mSelectedPrice.add(priceArray2[filterDialog.rangebar1.rightPinValue.toInt()].toInt())
            val list = ArrayList<Map<String, Any?>>()
            map.keys.forEach {
                val attribute = HashMap<String, Any?>()
                attribute[it] = map[it]
                list.add(attribute)
            }
            mPage = 1
            searchRequest.page = mPage
            searchRequest.attribute = list
            searchRequest.price = mSelectedPrice
            Log.e("selected params", Gson().toJson(searchRequest).toString())
            loadProducts()
            filterDialog.dismiss()
        }
        filterDialog.tvReset.onClick {
            mTerms.forEach { it.isSelected = false }
            mSelectedPrice.clear()
            searchRequest.attribute = null
            searchRequest.price = null
            loadProducts()
            filterDialog.dismiss()
        }
        filterDialog.tvSelectAll.onClick {
            mTerms.forEach { it.isSelected = true }
            brandAdapter.notifyDataSetChanged()
        }

        filterDialog.ivClose.onClick { filterDialog.dismiss() }

        filterDialog.show()
    }


}
