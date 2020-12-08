package com.iqonic.store.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.R
import com.iqonic.store.models.DashboardBanner
import com.iqonic.store.utils.extensions.loadImageFromUrl
import com.iqonic.store.utils.extensions.openCustomTab
import kotlinx.android.synthetic.main.item_slider.view.*

class HomeSliderAdapter(private var mImg: List<DashboardBanner>) : PagerAdapter() {
    var size: Int? = null
    private var mListener: HomeSliderAdapter.OnClickListener? = null
    fun setListener(mListener: HomeSliderAdapter.OnClickListener) {
        this.mListener = mListener
    }
    override fun instantiateItem(parent: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slider, parent, false)

        view.img.loadImageFromUrl(mImg[position].image)
        view.setOnClickListener {
            if (mListener != null) {
                mListener!!.onClick(position)
            }
        }
        parent.addView(view)
        return view
    }

    override fun isViewFromObject(v: View, `object`: Any): Boolean = v === `object` as View

    override fun getCount(): Int = mImg.size

    override fun destroyItem(parent: ViewGroup, position: Int, `object`: Any) = parent.removeView(`object` as View)

    interface OnClickListener {
        fun onClick(position: Int)
    }
}
