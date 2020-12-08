package com.iqonic.store.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.R
import com.iqonic.store.adapter.BaseAdapter
import com.iqonic.store.adapter.ProductImageAdapter
import com.iqonic.store.adapter.SpinnerAdapter
import com.iqonic.store.utils.BroadcastReceiverExt
import com.iqonic.store.utils.Constants.AppBroadcasts.CART_COUNT_CHANGE
import com.iqonic.store.models.RequestModel
import com.iqonic.store.models.StoreCategory
import com.iqonic.store.models.StoreProductModel
import com.iqonic.store.models.StoreUpSale
import com.iqonic.store.utils.Constants
import com.iqonic.store.utils.Constants.KeyIntent.DATA
import com.iqonic.store.utils.Constants.KeyIntent.EXTERNAL_URL
import com.iqonic.store.utils.Constants.KeyIntent.PRODUCT_ID
import com.iqonic.store.utils.Constants.viewAllCode.CATEGORY
import com.iqonic.store.utils.extensions.*
import kotlinx.android.synthetic.main.activity_product_detail2.*
import kotlinx.android.synthetic.main.item_category.view.*
import kotlinx.android.synthetic.main.item_group.view.*
import kotlinx.android.synthetic.main.item_group.view.ivProduct
import kotlinx.android.synthetic.main.item_group.view.tvAdd
import kotlinx.android.synthetic.main.item_group.view.tvOriginalPrice
import kotlinx.android.synthetic.main.item_group.view.tvProductName
import kotlinx.android.synthetic.main.item_home_dashboard1.view.*
import kotlinx.android.synthetic.main.menu_cart.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ProductDetailActivity2 : AppBaseActivity() {
    private var mPId = 0
    private val mImages = ArrayList<String>()
    private var isAddedToCart: Boolean = false
    private var mIsInWishList = false
    private var mIsExternalProduct = false
    private var mExternalURL: String = ""
    private var mAttributeAdapter: BaseAdapter<String>? = null
    private var mYearAdapter: ArrayAdapter<String>? = null
    private var mQuantity: String = "1"
    private var image: String = ""

    private val mProductAdapter = BaseAdapter<StoreUpSale>(
        R.layout.item_home_dashboard1,
        onBind = { view, model, _ ->
            setProductItem1(view, model)
        })

    private val mCategoryAdapter = BaseAdapter<StoreCategory>(
        R.layout.item_category,
        onBind = { view, model, _ ->
            view.tvCategoryName.text = model.name
            view.tvCategoryName.changeTextSecondaryColor()
            view.onClick {
                launchActivity<ViewAllProductActivity> {
                    putExtra(Constants.KeyIntent.TITLE, model.name)
                    putExtra(Constants.KeyIntent.VIEWALLID, CATEGORY)
                    putExtra(Constants.KeyIntent.KEYID, model.id)

                }
            }
        })

    private fun setProductItem1(
        view: View,
        model: StoreUpSale,
        params: Boolean = false
    ) {
        if (!params) {
            view.ivProduct.layoutParams = productLayoutParams()
        } else {
            view.ivProduct.layoutParams = productLayoutParamsForDealOffer()
        }

        if (model.images!![0].src!!.isNotEmpty()) {
            view.ivProduct.loadImageFromUrl(model.images!![0].src!!)
            image = model.images!![0].src!!
        }

        val mName = model.name!!.split(",")

        view.tvProductName.text = mName[0]
        view.tvProductWeight.changeAccentColor()
        view.tvProductName.changeTextPrimaryColor()
        view.tvOriginalPrice.changeTextSecondaryColor()
        view.tvDiscountPrice.changeTextPrimaryColor()
        view.tvAdd.changeBackgroundTint(getAccentColor())

        if (model.sale_price!!.isNotEmpty()) {
            view.tvSaleLabel.visibility = View.VISIBLE
            view.tvDiscountPrice.text = model.sale_price!!.currencyFormat()
            view.tvOriginalPrice.applyStrike()
            view.tvOriginalPrice.text = model.regular_price!!.currencyFormat()
            view.tvOriginalPrice.visibility = View.VISIBLE
        } else {
            view.tvSaleLabel.visibility = View.GONE
            view.tvOriginalPrice.visibility = View.VISIBLE
            if (model.regular_price!!.isEmpty()) {
                view.tvOriginalPrice.text = ""
                view.tvDiscountPrice.text = model.price!!.currencyFormat()
            } else {
                view.tvOriginalPrice.text = ""
                view.tvDiscountPrice.text = model.regular_price!!.currencyFormat()
            }
        }

        view.onClick {
            launchActivity<ProductDetailActivity2> {
                putExtra(PRODUCT_ID, model.id)
            }

        }
        view.tvAdd.onClick {
            mAddCart(model)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_product_detail2)
        setDetailToolbar(toolbar)
        changeColor()
        if (intent?.extras?.get(DATA) == null && intent?.extras?.get(PRODUCT_ID) == null) {
            toast(R.string.error_something_went_wrong)
            finish()
            return
        }
        mPId = intent?.getIntExtra(PRODUCT_ID, 0)!!

        BroadcastReceiverExt(this) {
            onAction(CART_COUNT_CHANGE) {
                setCartCountFromPref()
            }

        }
        rvLike?.setHorizontalLayout()
        rvLike?.adapter = mProductAdapter

        rvCategory.apply {
            layoutManager = GridLayoutManager(this@ProductDetailActivity2, 2)
            setHasFixedSize(true)
            adapter = mCategoryAdapter
        }

        getProductDetail()
        if (isLoggedIn()) {
            loadApis()
        }
        tvItemProductOriginalPrice.applyStrike()

        btnAddCard.onClick {
            if (isLoggedIn()) {
                if (mIsExternalProduct) {
                    launchActivity<WebViewExternalProductActivity> {
                        putExtra(EXTERNAL_URL, mExternalURL)
                    }
                } else {
                    if (isAddedToCart) removeCartItem() else addItemToCart()
                }

            } else launchActivity<SignInUpActivity> { }

        }

        llReviews.onClick {
            launchActivity<ReviewsActivity> {
                putExtra(PRODUCT_ID, intent?.getIntExtra(PRODUCT_ID, 0)!!)
            }
        }

        ivFavourite.onClick { onFavouriteClick() }

        setCartCount()
        rlCartMain.onClick {
            when {
                isLoggedIn() -> launchActivity<MyCartActivity>()
                else -> launchActivity<SignInUpActivity> { }
            }
        }

    }

    private fun mAddCart(model: StoreUpSale) {
        if (isLoggedIn()) {
            val requestModel = RequestModel()
            requestModel.pro_id = model.id
            requestModel.quantity = 1
            addItemToCart(requestModel, onApiSuccess = {
            })
        } else launchActivity<SignInUpActivity> { }
    }

    private fun setCartCountFromPref() {
        if (isLoggedIn()) {
            val count = getCartCount()
            ivCart?.changeBackgroundImageTint(getButtonColor())
            tvNotificationCount?.changeTint(getButtonColor())
            tvNotificationCount?.text = count
            if (count.checkIsEmpty() || count == "0") {
                tvNotificationCount?.hide()
            } else {
                tvNotificationCount?.show()
            }
        }

    }

    private fun addItemToCart() {
        val requestModel = RequestModel()
        requestModel.pro_id = mPId
        requestModel.quantity = mQuantity.toInt()
        addItemToCart(requestModel, onApiSuccess = {
            btnAddCard.text = getString(R.string.lbl_remove_cart)
            isAddedToCart = true
            fetchAndStoreCartData()

        })
    }

    private fun addItemToCartGroupItem(id: Int) {
        val requestModel = RequestModel()
        requestModel.pro_id = id
        requestModel.quantity = mQuantity.toInt()
        addItemToCart(requestModel, onApiSuccess = {
            fetchAndStoreCartData()

        })

    }

    private fun setCartCount() {
        val count = getCartCount()
        tvNotificationCount.text = count
        if (count.checkIsEmpty() || count == "0") {
            tvNotificationCount.hide()
        } else {
            tvNotificationCount.show()
        }
    }

    private fun removeCartItem() {
        val requestModel = RequestModel()
        requestModel.pro_id = mPId
        removeCartItem(requestModel, onApiSuccess = {
            btnAddCard.text = getString(R.string.lbl_add_to_cart)
            isAddedToCart = false
            fetchAndStoreCartData()

        })
    }

    private fun loadApis() {
        if (isNetworkAvailable()) {
            fetchAndStoreCartData()
        }
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun getProductDetail() {
        scrollView.hide()
        if (isNetworkAvailable()) {
            showProgress(true)

            getRestApiImpl().productDetail(mPId, onApiSuccess = {
                showProgress(false)
                scrollView.show()

                viewVariableImage(it[0])

                tvItemProductOriginalPrice.applyStrike()
                /**
                 * Other Information
                 *
                 */
                tvName.text = it[0].name
                tvItemProductRating.rating = it[0].averageRating!!.toFloat()
                tvTotalReview.text = "(" + it[0].averageRating!! + ")"
                tvTags.text = it[0].description?.getHtmlString().toString()

                /**
                 * check stock
                 */
                if (it[0].in_stock) {
                    btnOutOfStock.hide()
                    btnAddCard.show()
                } else {
                    btnOutOfStock.show()
                    btnAddCard.hide()
                }

                /**
                 * Additional information
                 *
                 */
                if (it[0].attributes != null) {
                    for (att in it[0].attributes!!) {
                        val vi =
                            applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val v: View = vi.inflate(R.layout.view_attributes, null)
                        val textView =
                            v.findViewById<View>(R.id.txtAttName) as TextView
                        textView.changeTextSecondaryColor()
                        textView.text = att.name.toString() + " : "

                        val sizeList = ArrayList<String>()
                        val sizes = att.options
                        sizes?.forEachIndexed { _, s ->
                            sizeList.add(s.trim())
                        }
                        mAttributeAdapter =
                            BaseAdapter(R.layout.item_attributes, onBind = { vv, item, position ->
                                if (item.isNotEmpty()) {
                                    val attSize =
                                        vv.findViewById<View>(R.id.tvSize) as TextView
                                    attSize.typeface = fontRegular()
                                    attSize.changeTextSecondaryColor()
                                    if (sizeList.size - 1 == position) {
                                        attSize.text = item
                                    } else {
                                        attSize.text = "$item ,"
                                    }
                                }
                            })
                        mAttributeAdapter?.clearItems()
                        mAttributeAdapter?.addItems(sizeList)
                        val recycleView =
                            v.findViewById<View>(R.id.rvAttributeView) as RecyclerView
                        recycleView.setHorizontalLayout()
                        recycleView.adapter = mAttributeAdapter

                        llAttributeView.addView(
                            v,
                            0,
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.FILL_PARENT,
                                ViewGroup.LayoutParams.FILL_PARENT
                            )
                        )
                    }
                }

                /**
                 *  Attribute Information
                 */
                if (it[0].type == "simple") {
                    if (it[0].attributes!!.isNotEmpty()) {
                        tvAvailability.text = it[0].attributes!![0].name.toString()
                    }
                    llAttribute.hide()
                    setPriceDetail(it[0])
                } else if (it[0].type == "variable") {
                    llAttribute.show()
                    if (it[0].attributes != null && it[0].attributes?.isNotEmpty()!!) {

                        val sizeList = ArrayList<String>()
                        val mVariationsList = ArrayList<Int>()

                        val mVariations = it[0].variations!!

                        it.forEachIndexed { i, details ->
                            if (i > 0) {
                                var option = ""
                                it[i].attributes!!.forEach { attr ->
                                    option = if (option.isNotBlank()) {
                                        option + " - " + attr.optionsString.toString()
                                    } else {
                                        attr.optionsString.toString()
                                    }
                                }
                                if (details.onSale) {
                                    option = "$option [Sale]"
                                }
                                sizeList.add(option)
                            }
                        }

                        mVariations.forEachIndexed { _, s ->
                            mVariationsList.add(s)
                        }
                        mYearAdapter = SpinnerAdapter(this,  sizeList)
                        spAttribute.adapter = this.mYearAdapter

                        spAttribute.onItemSelectedListener = object :
                            AdapterView.OnItemSelectedListener {

                            override fun onItemSelected(
                                parent: AdapterView<*>, view: View,
                                position: Int, id: Long
                            ) {
                                it.forEach { its ->
                                    if (mVariationsList[position] == its.id) {
                                        setPriceDetail(its)
                                        tvAvailability.text = its.attributes!![0].name.toString()
                                        mYearAdapter!!.notifyDataSetChanged()
                                    }
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {
                            }
                        }

                    } else {
                        llAttribute.hide()
                    }
                } else if (it[0].type == "grouped") {
                    llAttribute.hide()
                    upcomingSale.hide()
                    groupItems.show()

                    extraProduct.setVerticalLayout()
                    extraProduct.adapter = mGroupCartAdapter

                    mGroupCartAdapter.clearItems()
                    it.forEachIndexed { i, details ->
                        if (i > 0) {
                            mGroupCartAdapter.addItem(details)
                        }
                    }
                    mGroupCartAdapter.notifyDataSetChanged()
                } else if (it[0].type == "external") {
                    llAttribute.hide()
                    setPriceDetail(it[0])
                    mIsExternalProduct = true
                    btnAddCard.show()
                    btnAddCard.text = it[0].buttonText
                    mExternalURL = it[0].externalUrl.toString()
                } else {
                    toast(R.string.invalid_product)
                    finish()
                }

                // Purchasable
                if (!it[0].purchasable) {
                    if (mIsExternalProduct) {
                        banner_container.show()
                    } else {
                        banner_container.hide()
                    }
                } else {
                    banner_container.show()
                }

                // Review
                if (it[0].reviewsAllowed == true) {
                    tvAllReviews.show()
                    llReviews.show()
                    tvAllReviews.onClick {
                        launchActivity<ReviewsActivity> {
                            putExtra(PRODUCT_ID, intent?.getIntExtra(PRODUCT_ID, 0)!!)
                        }
                    }
                } else {
                    llReviews.hide()
                    tvAllReviews.hide()
                }

                // like data
                if (it[0].upsellIds.isNullOrEmpty()) {
                    lbl_like.hide()
                    rvLike.hide()
                } else {
                    lbl_like.show()
                    rvLike.show()
                    mProductAdapter.addItems(it[0].upsell_id!!)
                }

                // Category data
                if (it[0].categories.isNullOrEmpty()) {
                    lblCategory.hide()
                    rvCategory.hide()
                } else {
                    lblCategory.show()
                    rvCategory.show()
                    mCategoryAdapter.clearItems()
                    mCategoryAdapter.addItems(it[0].categories!!)
                }

                // check cart & wish list
                when {
                    it[0].is_added_cart -> {
                        if (mIsExternalProduct) {
                            btnAddCard.text = it[0].buttonText
                        } else {
                            isAddedToCart = true
                            btnAddCard.text = getString(R.string.lbl_remove_cart)
                        }
                    }
                    else -> {
                        if (mIsExternalProduct) {
                            btnAddCard.text = it[0].buttonText
                        } else {
                            isAddedToCart = false
                            btnAddCard.text = getString(R.string.lbl_add_to_cart)
                        }
                    }
                }
                when {
                    it[0].is_added_wishlist -> {
                        mIsInWishList = true
                        changeFavIcon(
                            R.drawable.ic_heart_fill,
                            getAccentColor()
                        )
                    }
                    else -> {
                        mIsInWishList = false
                        changeFavIcon(R.drawable.ic_heart, getAccentColor())
                    }
                }

            }, onApiError = {
                showProgress(false)
                snackBar(it)
            })

        }
    }

    private fun calculateDiscount(salePrice: String?, regularPrice: String?): Float {
        return (100f - (salePrice!!.toFloat() * 100f) / regularPrice!!.toFloat())
    }

    /**
     * Header Images
     *
     */

    private fun viewVariableImage(its: StoreProductModel) {
        mImages.clear()
        for (i in its.images!!.indices) {
            its.images?.get(i)?.src?.let { it1 -> mImages.add(it1) }
        }
        val adapter1 = ProductImageAdapter(mImages)
        productViewPager.adapter = adapter1
        dots.attachViewPager(productViewPager)
        dots.setDotDrawable(R.drawable.bg_circle_primary, R.drawable.black_dot)

        adapter1.setListener(object : ProductImageAdapter.OnClickListener {
            override fun onClick(position: Int) {
                launchActivity<ZoomImageActivity> {
                    putExtra(DATA, its)
                }
            }

        })
    }

    @SuppressLint("SetTextI18n")
    private fun setPriceDetail(its: StoreProductModel) {
        mPId = its.id
        if (its.onSale) {
            tvPrice.text = its.price?.currencyFormat()
            tvSaleLabel.show()
            tvItemProductOriginalPrice.applyStrike()
            tvItemProductOriginalPrice.text =
                its.regularPrice?.currencyFormat()
            upcomingSale.hide()

            val discount =
                calculateDiscount(its.salePrice, its.regularPrice)
            if (discount > 0.0) {
                tvSaleDiscount.show()
                tvSaleDiscount.text =
                    String.format("%.2f", discount) + getString(R.string.lbl_off)
            }
            onSaleOffer(its)
        } else {
            tvSaleDiscount.hide()
            tvItemProductOriginalPrice.text = ""
            tvPrice.text = its.regularPrice?.currencyFormat()
            tvSaleLabel.hide()
            showUpComingSale(its)
            tvSaleOffer.hide()
        }
    }

    /**
     * Showing Special Price Label
     *
     */
    @SuppressLint("SimpleDateFormat")
    private fun onSaleOffer(its: StoreProductModel) {
        if (its.dateOnSaleFrom != "") {
            tvSaleOffer.show()
            val endTime = its.dateOnSaleTo.toString() + " 23:59:59"
            val dateFormat = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss"
            )
            try {
                val endDate: Date = dateFormat.parse(endTime)
                val currentDate = Date()
                val different: Long = endDate.time - currentDate.time
                object : CountDownTimer(different, 1000) {
                    @SuppressLint("SetTextI18n")
                    override fun onTick(millisUntilFinished: Long) {
                        var differenta: Long = millisUntilFinished
                        val secondsInMilli: Long = 1000
                        val minutesInMilli = secondsInMilli * 60
                        val hoursInMilli = minutesInMilli * 60
                        val daysInMilli = hoursInMilli * 24

                        val elapsedDays: Long = differenta / daysInMilli
                        differenta %= daysInMilli

                        val elapsedHours: Long = differenta / hoursInMilli
                        differenta %= hoursInMilli

                        val elapsedMinutes: Long = differenta / minutesInMilli
                        differenta %= minutesInMilli

                        val elapsedSeconds: Long = differenta / secondsInMilli
                        if (elapsedDays > 0) {
                            tvSaleOffer.text =
                                getString(R.string.lbl_special_price_ends_in_less_then) + " " + elapsedDays + getString(
                                    R.string.lbl_d
                                ) + " " + elapsedHours + getString(R.string.lbl_h) + " " + elapsedMinutes + getString(
                                    R.string.lbl_m
                                ) + " " + elapsedSeconds + getString(R.string.lbl_s)
                        } else {
                            tvSaleOffer.text =
                                getString(R.string.lbl_special_price_ends_in_less_then) + " " + elapsedHours + getString(
                                    R.string.lbl_h
                                ) + " " + elapsedMinutes + getString(R.string.lbl_m) + " " + elapsedSeconds + getString(
                                    R.string.lbl_s
                                )
                        }
                    }

                    override fun onFinish() {
                        tvSaleOffer.hide()
                    }
                }.start()
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        } else {
            tvSaleOffer.hide()
        }
    }

    /**
     * Show Upcoming sale details
     *
     */
    @SuppressLint("SetTextI18n")
    private fun showUpComingSale(its: StoreProductModel) {
        if (its.dateOnSaleFrom != "") {
            upcomingSale.show()
            tvUpcomingSale.text =
                getString(R.string.lbl_sale_start_from) + " " + its.dateOnSaleFrom + " " + getString(
                    R.string.lbl_to
                ) + " " + its.dateOnSaleTo + ". " + getString(R.string.lbl_ge_amazing_discounts_on_the_products)
        } else {
            upcomingSale.hide()
        }
    }

    private fun onFavouriteClick() {
        if (mIsInWishList) {
            changeFavIcon(
                R.drawable.ic_heart,
                getAccentColor()
            ); ivFavourite.isClickable = false

            val requestModel = RequestModel(); requestModel.pro_id =
                mPId
            removeFromWishList(requestModel) {
                ivFavourite.isClickable = true
                mIsInWishList = false
                if (it) changeFavIcon(
                    R.drawable.ic_heart,
                    getAccentColor()
                ) else changeFavIcon(
                    R.drawable.ic_heart_fill,
                    getAccentColor()
                )
            }
        } else {
            if (isLoggedIn()) {
                changeFavIcon(
                    R.drawable.ic_heart_fill,
                    getAccentColor()
                ); ivFavourite.isClickable = false

                val requestModel = RequestModel()
                requestModel.pro_id = mPId
                addToWishList(requestModel) {
                    ivFavourite.isClickable = true
                    mIsInWishList = true
                    if (it) changeFavIcon(
                        R.drawable.ic_heart_fill,
                        getAccentColor()
                    ) else changeFavIcon(R.drawable.ic_heart, getAccentColor())
                }
            } else {
                launchActivity<SignInUpActivity>()
            }
        }
    }

    private fun changeFavIcon(
        drawable: Int,
        iconTint: String = getAccentColor()
    ) {
        ivFavourite.setImageResource(drawable)
        ivFavourite.changeBackgroundImageTint(iconTint)
    }

    /**
     * Grouped Items DisplayM
     *
     */
    private val mGroupCartAdapter =
        BaseAdapter<StoreProductModel>(R.layout.item_group, onBind = { view, model, _ ->
            view.tvProductName.text = model.name
            view.tvProductName.changeTextPrimaryColor()
            view.tvAdd.changeBackgroundTint(getAccentColor())
            view.tvPrice.changeAccentColor()
            view.tvOriginalPrice.changeTextSecondaryColor()
            view.tvOriginalPrice.applyStrike()
            if (model.images!![0].src!!.isNotEmpty()) {
                view.ivProduct.loadImageFromUrl(model.images!![0].src!!)
            }
            if (model.onSale) {
                view.tvPrice.text = model.salePrice?.currencyFormat()
                view.tvOriginalPrice.applyStrike()
                view.tvOriginalPrice.text =
                    model.regularPrice?.currencyFormat()
            } else {
                view.tvOriginalPrice.text = ""
                if (model.regularPrice.equals("")) {
                    view.tvPrice.text = model.price?.currencyFormat()
                } else {
                    view.tvPrice.text = model.regularPrice?.currencyFormat()
                }
            }

            view.tvAdd.onClick {
                if (isLoggedIn()) {
                    addItemToCartGroupItem(model.id)
                } else launchActivity<SignInUpActivity> { }
            }
        })

    private fun changeColor() {
        tvAvailability.changeAccentColor()
        tvSaleDiscount.changeTextPrimaryColor()
        tvName.changeTextPrimaryColor()
        tvPrice.changeAccentColor()
        tvItemProductOriginalPrice.changeTextSecondaryColor()
        tvSaleOffer.changeAccentColor()
        lblProductInclude.changeTextPrimaryColor()
        lblAvailable.changeTextPrimaryColor()
        lblAdditionInformation.changeTextPrimaryColor()
        lblUpcomingSale.changeTextPrimaryColor()
        tvUpcomingSale.changeTextSecondaryColor()
        lblDescription.changeTextPrimaryColor()
        tvTags.changeTextSecondaryColor()
        lblCategory.changeTextPrimaryColor()
        lblCategory.changeTextPrimaryColor()
        lbl_like.changeTextPrimaryColor()
        tvAllReviews.changeTextPrimaryColor()
        btnAddCard.backgroundTintList = ColorStateList.valueOf(Color.parseColor(getButtonColor()))
        htab_maincontent.changeBackgroundColor()
        tvTotalReview.changeTextPrimaryColor()
        ivCart.changeBackgroundImageTint(getButtonColor())
    }
}

