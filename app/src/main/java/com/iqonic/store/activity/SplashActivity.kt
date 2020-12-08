package com.iqonic.store.activity

import android.annotation.SuppressLint
import android.os.Bundle
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.R
import android.view.WindowManager
import com.iqonic.store.utils.Constants.SharedPref.MODE
import com.iqonic.store.utils.extensions.*
import kotlinx.android.synthetic.main.activity_splash_new.*

class SplashActivity : AppBaseActivity() {

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_new)

        val w = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        runDelayed(1500) {
            launchActivity<WalkThroughActivity>()
            finish()
        }
        rlMain.changeBackgroundColor()
        getSharedPrefInstance().removeKey(MODE)
        getSharedPrefInstance().setValue(MODE,2)
    }
}