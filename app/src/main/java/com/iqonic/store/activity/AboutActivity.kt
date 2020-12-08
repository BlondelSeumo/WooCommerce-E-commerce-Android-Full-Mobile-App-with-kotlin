package com.iqonic.store.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.BuildConfig
import com.iqonic.store.R
import com.iqonic.store.utils.Constants.SharedPref.CONTACT
import com.iqonic.store.utils.Constants.SharedPref.COPYRIGHT_TEXT
import com.iqonic.store.utils.Constants.SharedPref.FACEBOOK
import com.iqonic.store.utils.Constants.SharedPref.INSTAGRAM
import com.iqonic.store.utils.Constants.SharedPref.PRIVACY_POLICY
import com.iqonic.store.utils.Constants.SharedPref.TERM_CONDITION
import com.iqonic.store.utils.Constants.SharedPref.TWITTER
import com.iqonic.store.utils.Constants.SharedPref.WHATSAPP
import com.iqonic.store.utils.extensions.*
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.toolbar.*

class AboutActivity : AppBaseActivity() {

    private var whatsUp: String = ""
    private var instagram: String = ""
    private var twitter: String = ""
    private var facebook: String = ""
    private var contact: String = ""
    private var copyRight: String = ""
    private var privacy: String = ""
    private var toc: String = ""

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setToolbar(toolbar)
        title = getString(R.string.lbl_about)
        mAppBarColor()
        changeColor()
        getSharedPrefInstance().apply {
            whatsUp = getStringValue(WHATSAPP)
            instagram = getStringValue(INSTAGRAM)
            twitter = getStringValue(TWITTER)
            facebook = getStringValue(FACEBOOK)
            contact = getStringValue(CONTACT)
            copyRight = getStringValue(COPYRIGHT_TEXT)
            privacy = getStringValue(PRIVACY_POLICY)
            toc = getStringValue(TERM_CONDITION)
        }
        if (copyRight.isEmpty()) {
            tvCopyRight.hide()
        } else {
            tvCopyRight.text = copyRight
            tvCopyRight.show()
        }

        tvVersion.text = "Version: " + BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE

        if (whatsUp.isEmpty()) iv_whatsapp.hide()
        if (privacy.isEmpty()) tvPrivacyPolicy.hide()
        if (toc.isEmpty()) tvTOC.hide()
        if (instagram.isEmpty()) iv_instagram.hide()
        if (twitter.isEmpty()) iv_twitter_sign.hide()
        if (facebook.isEmpty()) iv_facebook.hide()
        if (contact.isEmpty()) iv_contact.hide()
        llBottom.show()

        iv_whatsapp.onClick { openCustomTab("https://wa.me/${whatsUp}") }
        iv_instagram.onClick { openCustomTab(instagram) }
        iv_twitter_sign.onClick { openCustomTab(twitter) }
        iv_facebook.onClick { openCustomTab(facebook) }
        iv_contact.onClick { dialNumber(contact) }
        tvPrivacyPolicy.onClick { openCustomTab(privacy) }
        tvTOC.onClick { openCustomTab(toc) }

        if (whatsUp.isEmpty() && instagram.isEmpty() && twitter.isEmpty() && facebook.isEmpty() && contact.isEmpty()) {
            llFollow.hide()
        } else {
            llFollow.show()
        }
    }
    private fun changeColor(){
        tvAppName.changePrimaryColor()
        tvVersion.changePrimaryColor()
        tvCopyRight.changePrimaryColor()
        tvTOC.changePrimaryColor()
        tvPrivacyPolicy.changePrimaryColor()
        llFollow.changeTextPrimaryColor()
        rlMain.changeBackgroundColor()
    }
}
