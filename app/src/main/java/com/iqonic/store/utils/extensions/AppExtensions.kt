package com.iqonic.store.utils.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.*
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.R
import com.iqonic.store.ShopHopApp
import com.iqonic.store.ShopHopApp.Companion.getAppInstance
import com.iqonic.store.ShopHopApp.Companion.noInternetDialog
import com.iqonic.store.models.*
import com.iqonic.store.utils.Constants
import com.iqonic.store.utils.Constants.AppBroadcasts.CARTITEM_UPDATE
import com.iqonic.store.utils.Constants.AppBroadcasts.CART_COUNT_CHANGE
import com.iqonic.store.utils.Constants.AppBroadcasts.ORDER_COUNT_CHANGE
import com.iqonic.store.utils.Constants.AppBroadcasts.PROFILE_UPDATE
import com.iqonic.store.utils.Constants.AppBroadcasts.WISHLIST_UPDATE
import com.iqonic.store.utils.Constants.SharedPref.ACCENTCOLOR
import com.iqonic.store.utils.Constants.SharedPref.BACKGROUNDCOLOR
import com.iqonic.store.utils.Constants.SharedPref.BILLING
import com.iqonic.store.utils.Constants.SharedPref.COUNTRY
import com.iqonic.store.utils.Constants.SharedPref.DASHBOARDDATA
import com.iqonic.store.utils.Constants.SharedPref.DEFAULT_CURRENCY
import com.iqonic.store.utils.Constants.SharedPref.DEFAULT_CURRENCY_FORMATE
import com.iqonic.store.utils.Constants.SharedPref.IS_LOGGED_IN
import com.iqonic.store.utils.Constants.SharedPref.KEY_CART_COUNT
import com.iqonic.store.utils.Constants.SharedPref.KEY_ORDER_COUNT
import com.iqonic.store.utils.Constants.SharedPref.KEY_PRODUCT_DETAIL
import com.iqonic.store.utils.Constants.SharedPref.KEY_USER_ADDRESS
import com.iqonic.store.utils.Constants.SharedPref.KEY_WISHLIST_COUNT
import com.iqonic.store.utils.Constants.SharedPref.LANGUAGE
import com.iqonic.store.utils.Constants.SharedPref.PRIMARYCOLOR
import com.iqonic.store.utils.Constants.SharedPref.TEXTPRIMARYCOLOR
import com.iqonic.store.utils.Constants.SharedPref.TEXTSECONDARYCOLOR
import com.iqonic.store.utils.Constants.SharedPref.USER_DISPLAY_NAME
import com.iqonic.store.utils.Constants.SharedPref.USER_EMAIL
import com.iqonic.store.utils.Constants.SharedPref.USER_FIRST_NAME
import com.iqonic.store.utils.Constants.SharedPref.USER_ID
import com.iqonic.store.utils.Constants.SharedPref.USER_LAST_NAME
import com.iqonic.store.utils.Constants.SharedPref.USER_NICE_NAME
import com.iqonic.store.utils.Constants.SharedPref.USER_PROFILE
import com.iqonic.store.utils.Constants.SharedPref.USER_ROLE
import com.iqonic.store.utils.Constants.SharedPref.USER_TOKEN
import com.iqonic.store.utils.Constants.SharedPref.USER_USERNAME
import com.iqonic.store.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.layout_network.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


fun isLoggedIn(): Boolean = getSharedPrefInstance().getBooleanValue(IS_LOGGED_IN)
fun getUserId(): String = getSharedPrefInstance().getStringValue(USER_ID)
fun getDefaultCurrency(): String = getSharedPrefInstance().getStringValue(DEFAULT_CURRENCY)
fun getDefaultCurrencyFormate(): String = getSharedPrefInstance().getStringValue(
    DEFAULT_CURRENCY_FORMATE
)

fun getLanguage(): String = getSharedPrefInstance().getStringValue(LANGUAGE)
fun getUserName(): String = getSharedPrefInstance().getStringValue(USER_USERNAME)
fun getFirstName(): String = getSharedPrefInstance().getStringValue(USER_FIRST_NAME)
fun getDisplayName(): String = getSharedPrefInstance().getStringValue(USER_DISPLAY_NAME)
fun getLastName(): String = getSharedPrefInstance().getStringValue(USER_LAST_NAME)
fun getEmail(): String = getSharedPrefInstance().getStringValue(USER_EMAIL)
fun getCartCount(): String = getSharedPrefInstance().getIntValue(KEY_CART_COUNT, 0).toString()
fun getWishListCount(): String =
    getSharedPrefInstance().getIntValue(KEY_WISHLIST_COUNT, 0).toString()

fun getOrderCount(): String = getSharedPrefInstance().getIntValue(KEY_ORDER_COUNT, 0).toString()
fun getApiToken(): String = getSharedPrefInstance().getStringValue(USER_TOKEN)
fun getUserProfile(): String = getSharedPrefInstance().getStringValue(USER_PROFILE)

@SuppressLint("ResourceType")
fun getPrimaryColor(): String {
    return getSharedPrefInstance().getStringValue(PRIMARYCOLOR)
}

@SuppressLint("ResourceType")
fun getAccentColor(): String {
    return getSharedPrefInstance().getStringValue(ACCENTCOLOR)
}

@SuppressLint("ResourceType")
fun getButtonColor(): String {
    return getSharedPrefInstance().getStringValue(PRIMARYCOLOR)
}

@SuppressLint("ResourceType")
fun getTextPrimaryColor(): String {
    return getSharedPrefInstance().getStringValue(TEXTPRIMARYCOLOR)
}

@SuppressLint("ResourceType")
fun getTextSecondaryColor(): String {

    return getSharedPrefInstance().getStringValue(TEXTSECONDARYCOLOR)

}

@SuppressLint("ResourceType")
fun getBackgroundColor(): String {
    return getSharedPrefInstance().getStringValue(BACKGROUNDCOLOR)

}

@SuppressLint("ResourceType")
fun getTextTitleColor(): String {
    return getAppInstance().resources.getString(R.color.common_white)
}

fun getProductDetailConstant(): Int = getSharedPrefInstance().getIntValue(KEY_PRODUCT_DETAIL, 0)

fun getUserFullName(): String {
    return when {
        isLoggedIn() -> (getSharedPrefInstance().getStringValue(USER_FIRST_NAME) + " " + getSharedPrefInstance().getStringValue(
            USER_LAST_NAME
        )).toCamelCase()
        else -> "Guest User"
    }
}

fun Context.openCustomTab(url: String) =
    CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(url))


fun getProfile(): String = getSharedPrefInstance().getStringValue(USER_PROFILE)
fun Activity.fetchAndStoreCartData() {
    getRestApiImpl().getCart(onApiSuccess = {
        getSharedPrefInstance().setValue(KEY_CART_COUNT, it.size)
        sendCartCountChangeBroadcast()
    }, onApiError = {
        getSharedPrefInstance().setValue(KEY_CART_COUNT, 0)
        sendCartCountChangeBroadcast()
    })

}


fun convertOrderDataToLocalDate(ourDate: String): String? {
    var ourDate: String? = ourDate
    ourDate = try {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.000000", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("Etc/UTC")
        val value: Date = formatter.parse(ourDate)
        val dateFormatter =
            SimpleDateFormat("dd-MM-yyyy hh:mm a") //this format changeable
        dateFormatter.timeZone = TimeZone.getDefault()
        dateFormatter.format(value)
    } catch (e: Exception) {
        e.printStackTrace()
        "00-00-0000 00:00"
    }
    return ourDate
}

fun convertToLocalDate(ourDate: String): String? {
    var ourDate: String? = ourDate
    ourDate = try {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("Etc/UTC")
        val value: Date = formatter.parse(ourDate)
        val dateFormatter =
            SimpleDateFormat("dd-MM-yyyy hh:mm a") //this format changeable
        dateFormatter.timeZone = TimeZone.getDefault()
        dateFormatter.format(value)
    } catch (e: Exception) {
        e.printStackTrace()
        "00-00-0000 00:00"
    }
    return ourDate
}

/**
 * Add shared preference related to user session here
 */
fun clearLoginPref() {
    getSharedPrefInstance().removeKey(IS_LOGGED_IN)
    getSharedPrefInstance().removeKey(USER_ID)
    getSharedPrefInstance().removeKey(USER_DISPLAY_NAME)
    getSharedPrefInstance().removeKey(USER_EMAIL)
    getSharedPrefInstance().removeKey(USER_NICE_NAME)
    getSharedPrefInstance().removeKey(USER_TOKEN)
    getSharedPrefInstance().removeKey(USER_FIRST_NAME)
    getSharedPrefInstance().removeKey(USER_LAST_NAME)
    getSharedPrefInstance().removeKey(USER_PROFILE)
    getSharedPrefInstance().removeKey(USER_ROLE)
    getSharedPrefInstance().removeKey(USER_USERNAME)
    getSharedPrefInstance().removeKey(KEY_USER_ADDRESS)
    getSharedPrefInstance().removeKey(KEY_CART_COUNT)
    getSharedPrefInstance().removeKey(KEY_ORDER_COUNT)
    getSharedPrefInstance().removeKey(KEY_WISHLIST_COUNT)
}

fun getSharedPrefInstance(): SharedPrefUtils {
    return if (ShopHopApp.sharedPrefUtils == null) {
        ShopHopApp.sharedPrefUtils = SharedPrefUtils()
        ShopHopApp.sharedPrefUtils!!
    } else {
        ShopHopApp.sharedPrefUtils!!
    }
}

fun RecyclerView.rvItemAnimation() {
    layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)
}


fun ImageView.displayBlankImage(aContext: Context, aPlaceHolderImage: Int) {
    Glide.with(aContext)
        .load(aPlaceHolderImage)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

fun Context.fontSemiBold(): Typeface? {
    return Typeface.createFromAsset(assets, getString(R.string.font_SemiBold))
}

fun Context.fontBold(): Typeface? {
    return Typeface.createFromAsset(assets, getString(R.string.font_Bold))
}

fun Context.fontRegular(): Typeface? {
    return Typeface.createFromAsset(assets, getString(R.string.font_regular))
}

fun Activity.getAlertDialog(
    aMsgText: String,
    aTitleText: String = getString(R.string.lbl_dialog_title),
    aPositiveText: String = getString(R.string.lbl_yes),
    aNegativeText: String = getString(R.string.lbl_no),
    onPositiveClick: (dialog: DialogInterface, Int) -> Unit,
    onNegativeClick: (dialog: DialogInterface, Int) -> Unit
): AlertDialog {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(aTitleText)
    builder.setMessage(aMsgText)
    builder.setPositiveButton(aPositiveText) { dialog, which ->
        onPositiveClick(dialog, which)
    }

    builder.setNegativeButton(aNegativeText) { dialog, which ->
        onNegativeClick(dialog, which)
    }
    return builder.create()
}

fun Activity.productLayoutParams(): RelativeLayout.LayoutParams {
    val width = (getDisplayWidth() / 4.2).toInt()
    val imgHeight = width + (width / 12)
    return RelativeLayout.LayoutParams(width, imgHeight)
}

fun Activity.productLayoutParamsForDealOffer(): RelativeLayout.LayoutParams {
    val width = (getDisplayWidth() / 3.2).toInt()
    val imgHeight = width + (width / 10)
    return RelativeLayout.LayoutParams(width, imgHeight)
}

fun Activity.sendCartCountChangeBroadcast() {
    sendBroadcast(CART_COUNT_CHANGE)
}


fun Activity.sendProfileUpdateBroadcast() {
    sendBroadcast(PROFILE_UPDATE)
}

fun Activity.sendWishlistBroadcast() {
    sendBroadcast(WISHLIST_UPDATE)
}

fun Activity.sendCartBroadcast() {
    sendBroadcast(CARTITEM_UPDATE)
}

fun Activity.sendOrderCountChangeBroadcast() {
    sendBroadcast(ORDER_COUNT_CHANGE)
}

fun Activity.sendBroadcast(action: String) {
    val intent = Intent()
    intent.action = action
    sendBroadcast(intent)
}

fun Activity.registerBroadCastReceiver(action: String, receiver: BroadcastReceiver) {
    val intent = IntentFilter()
    intent.addAction(action)
    registerReceiver(receiver, intent)
}

fun getbillingList(): Billing {
    val string = getSharedPrefInstance().getStringValue(BILLING, "")
    if (string.isEmpty()) {
        return Billing()
    }
    return Gson().fromJson(string, object : TypeToken<Billing>() {}.type)
}

fun getShippingList(): Shipping {
    val string = getSharedPrefInstance().getStringValue(Constants.SharedPref.SHIPPING, "")
    if (string.isEmpty()) {
        return Shipping()
    }
    return Gson().fromJson(string, object : TypeToken<Shipping>() {}.type)
}

fun ImageView.loadImageFromDrawable(@DrawableRes aPlaceHolderImage: Int) {
    Glide.with(getAppInstance()).load(aPlaceHolderImage).diskCacheStrategy(DiskCacheStrategy.NONE)
        .into(this)
}

fun fetchCountry(
    onApiSuccess: (ArrayList<CountryModel>) -> Unit,
    onApiError: (aError: String) -> Unit
) {
    val string = getSharedPrefInstance().getStringValue(COUNTRY, "")
    Log.e("country", string)
    if (string.isEmpty()) {
        getRestApiImpl().listAllCountry(onApiSuccess = { its ->
            getSharedPrefInstance().setValue(
                COUNTRY,
                Gson().toJson(its)
            )
            onApiSuccess(its)
        }, onApiError = {
            onApiError(it)
        })
    } else {
        onApiSuccess(Gson().fromJson(string, object : TypeToken<ArrayList<CountryModel>>() {}.type))
    }

}

fun Activity.openNetworkDialog(onClick: () -> Unit) {
    try {
        if (noInternetDialog == null) {
            noInternetDialog = Dialog(this, R.style.FullScreenDialog)
            noInternetDialog?.setContentView(R.layout.layout_network); noInternetDialog?.setCanceledOnTouchOutside(
                false
            ); noInternetDialog?.setCancelable(false)
            noInternetDialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            noInternetDialog?.rlMain?.onClick {
                if (!isNetworkAvailable()) {
                    snackBarError(getAppInstance().getString(R.string.error_no_internet))
                    return@onClick
                }
                noInternetDialog?.dismiss()
                onClick()
            }
        }
        if (!this.isFinishing && !noInternetDialog!!.isShowing) {
            noInternetDialog?.show()
        }
    } catch (e: Exception) {

    }

}

fun Context.fromJson(file: String): String =
    assets.open(file).bufferedReader().use {
        it.readText()
    }

fun Context.mainContent(onSuccess: (DashBoardResponse) -> Unit) =
    onSuccess(Gson().fromJson(fromJson("builder.json"), DashBoardResponse::class.java))

fun getBuilderDashboard(): BuilderDashboard {
    val string = getSharedPrefInstance().getStringValue(DASHBOARDDATA, "")
    if (string.isEmpty()) {
        return BuilderDashboard()
    }
    return Gson().fromJson(string, object : TypeToken<BuilderDashboard>() {}.type)
}