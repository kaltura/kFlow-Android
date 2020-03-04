package com.kaltura.kflow.presentation.debug

import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import androidx.annotation.LayoutRes
import androidx.core.view.doOnLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseFragment
import com.kaltura.kflow.presentation.extension.invisible
import com.kaltura.kflow.presentation.extension.shareFile
import com.kaltura.kflow.presentation.extension.visible
import com.kaltura.kflow.utils.saveToFile
import com.kaltura.kflow.utils.screenWidth
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.json.JSONObject
import org.koin.android.ext.android.inject

/**
 * Created by alex_lytvynenko on 20.11.2018.
 */
abstract class DebugFragment(@LayoutRes contentLayoutId: Int) : BaseFragment(contentLayoutId), DebugListener {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private val apiManager: PhoenixApiManager by inject()
    private var maxTitleWidth = 0
    private var minTitleWidth = 0

    protected abstract fun debugView(): DebugView

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (slideOffset > 0) {
                val width = (minTitleWidth + (maxTitleWidth - minTitleWidth) * slideOffset).toInt()
                title.width = width
            }
        }

        override fun onStateChanged(bottomSheet: View, @BottomSheetBehavior.State newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    title.width = minTitleWidth
                    share.invisible()
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    title.width = maxTitleWidth
                    share.visible()
                }
                else -> share.invisible()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        maxTitleWidth = screenWidth()
        title.doOnLayout { minTitleWidth = title.width }

        apiManager.setDebugListener(this)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        title.setOnClickListener { bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED }
        share.setOnClickListener { share() }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        apiManager.removeDebugListener()
    }

    override fun setRequestInfo(url: String, method: String, code: Int) {
        debugView().requestUrl = url
        debugView().requestMethod = method
        debugView().responseCode = code
    }

    override fun setRequestBody(jsonObject: JSONObject) {
        debugView().setRequestBody(jsonObject)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun setResponseBody(jsonObject: JSONObject) {
        debugView().setResponseBody(jsonObject)
    }

    override fun onError() {
        debugView().onUnknownError()
    }

    private fun share() {
        val file = saveToFile(requireContext(), debugView().sharedData)
        requireActivity().shareFile(file)
    }

    protected fun clearDebugView() {
        debugView().clear()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }
}