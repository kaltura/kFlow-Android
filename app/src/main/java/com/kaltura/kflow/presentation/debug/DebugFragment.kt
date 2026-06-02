package com.kaltura.kflow.presentation.debug

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import androidx.activity.addCallback
import androidx.annotation.LayoutRes
import androidx.core.view.doOnLayout
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kaltura.kflow.databinding.ViewBottomDebugBinding
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseFragment
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.utils.saveToFile
import com.kaltura.kflow.utils.screenWidth
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
    public var requestBodyString = ""
    public var responseBodyString = ""

    private var _binding: ViewBottomDebugBinding? = null
    private val bottomDebugBinding get() = _binding!!
    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (slideOffset > 0) {
                val width = (minTitleWidth + (maxTitleWidth - minTitleWidth) * slideOffset).toInt()
                bottomDebugBinding.debugTitle.width = width
            }
        }

        override fun onStateChanged(bottomSheet: View, @BottomSheetBehavior.State newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    bottomDebugBinding.debugTitle.width = minTitleWidth
                    bottomDebugBinding.share.invisible()
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    bottomDebugBinding.debugTitle.width = maxTitleWidth
                    bottomDebugBinding.share.visible()
                }
                else -> bottomDebugBinding.share.invisible()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = ViewBottomDebugBinding.bind(view)

        runOnMobile {
            initMobile()
        }
        bottomDebugBinding.share.setOnClickListener {
            share()
        }
        apiManager.setDebugListener(this)
    }

    private fun initMobile() {
        maxTitleWidth = screenWidth()
        bottomDebugBinding.debugTitle.doOnLayout { minTitleWidth = bottomDebugBinding.debugTitle.width }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomDebugBinding.bottomSheetLayout)
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        bottomDebugBinding.debugTitle.setOnClickListener {
            bottomSheetBehavior.state =
                    if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                        BottomSheetBehavior.STATE_COLLAPSED
                    else
                        BottomSheetBehavior.STATE_EXPANDED
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        runOnMobile {
            bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        }
        apiManager.removeDebugListener()
    }

    override fun setRequestInfo(url: String, method: String, code: Int) {
        bottomDebugBinding.debugView.requestUrl = url
        bottomDebugBinding.debugView.requestMethod = method
        bottomDebugBinding.debugView.responseCode = code
    }

    override fun setRequestBody(requestBody: JSONObject) {
        bottomDebugBinding.debugView.setRequestBody(requestBody)
        runOnMobile {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun setResponseBody(responseBody: JSONObject) {
        responseBodyString = responseBody.toString()
        bottomDebugBinding.debugView.setResponseBody(responseBody)
    }

    override fun onError() {
        bottomDebugBinding.debugView.onUnknownError()
    }

    private fun share() {
        val file = saveToFile(requireContext(), bottomDebugBinding.debugView.sharedData)
        requireActivity().shareFile(file)
    }

    protected fun clearDebugView() {
        bottomDebugBinding.debugView.clear()
        runOnMobile {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }
}