package com.iqonic.store.activity


import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.R
import com.iqonic.store.fragments.MyCartFragment
import com.iqonic.store.utils.BroadcastReceiverExt
import com.iqonic.store.utils.Constants
import com.iqonic.store.utils.extensions.addFragment
import com.iqonic.store.utils.extensions.changeBackgroundColor
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.toolbar.*

class MyCartActivity : AppBaseActivity() {

    private var myCartFragment: MyCartFragment = MyCartFragment()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_cart)
        setToolbar(toolbar)
        title = getString(R.string.menu_my_cart)
        mAppBarColor()
        rlMain.changeBackgroundColor()
        BroadcastReceiverExt(this) {
            onAction(Constants.AppBroadcasts.CARTITEM_UPDATE) {
                myCartFragment.invalidateCartLayout()
            }
        }
        addFragment(myCartFragment, R.id.container)
    }


}
