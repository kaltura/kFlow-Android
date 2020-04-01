package com.kaltura.kflow.presentation.mediaPage

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.view.isGone
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import kotlinx.android.synthetic.main.fragment_media_page.*
import org.jetbrains.anko.support.v4.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class MediaPageFragment : DebugFragment(R.layout.fragment_media_page) {

    private val viewModel: MediaPageViewModel by viewModel()
    private var parentalRuleId = 0
    private var mediaEntry: MediaEntry? = null

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playAsset.setOnClickListener {
            navigate(MediaPageFragmentDirections.navigateToPlayer(mediaEntry!!))
        }
        getProductPrice.setOnClickListener {
            hideKeyboard()
            getProductPriceRequest(mediaId.string)
        }
        getBookmark.setOnClickListener {
            hideKeyboard()
            getBookmarkRequest(mediaId.string)
        }
        getAssetRules.setOnClickListener {
            hideKeyboard()
            getAssetRulesRequest(mediaId.string)
        }
        checkAll.setOnClickListener {
            hideKeyboard()
            checkAllTogetherRequest(mediaId.string)
        }
        insertPin.setOnClickListener {
            hideKeyboard()
            if (pinInputLayout.isGone) {
                showPinInput()
            } else {
                checkPinRequest(pin.string)
            }
        }
        get.setOnClickListener {
            hideKeyboard()
            getAssetRequest(mediaId.string)
        }
        validateButtons()
    }

    override fun subscribeUI() {
        observeResource(viewModel.mediaEntry) {
            mediaEntry = it
            validateButtons()
        }
//        observeResource(viewModel.userAssetRules) {
//            handleUserRules(it)
//            validateButtons()
//        }
    }

    private fun getAssetRequest(assetId: String) {
        withInternetConnection {
            mediaEntry = null
            parentalRuleId = 0
            pin.string = ""
            validateButtons()
            clearDebugView()
            viewModel.getMediaEntry(assetId)
        }
    }

    private fun getProductPriceRequest(assetId: String) {
        withInternetConnection {
            clearDebugView()
            viewModel.getProductPrice(assetId)
        }
    }

    private fun getBookmarkRequest(assetId: String) {
        withInternetConnection {
            clearDebugView()
            viewModel.getBookmark(assetId)
        }
    }

    private fun getAssetRulesRequest(assetId: String) {
        withInternetConnection {
            if (TextUtils.isDigitsOnly(assetId)) {
                clearDebugView()
                viewModel.getAssetRules(assetId)
            } else {
                toast("Wrong input")
            }
        }
    }

    private fun checkAllTogetherRequest(assetId: String) {
        withInternetConnection {
            if (TextUtils.isDigitsOnly(assetId)) {
                clearDebugView()
                viewModel.checkAllTogether(assetId)
            } else {
                Toast.makeText(requireContext(), "Wrong input", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPinRequest(pin: String) {
        withInternetConnection {
            if (TextUtils.isDigitsOnly(pin)) {
                clearDebugView()
                viewModel.checkPin(pin, parentalRuleId)
            } else {
                toast("Wrong input")
            }
        }
    }

    private fun showPinInput() {
        pinInputLayout.visible()
        insertPin.text = "Check pin"
        showKeyboard(pin)
    }

    private fun validateButtons() {
        val isVisible = mediaEntry != null
        playAsset.visibleOrGone(isVisible)
//        getProductPrice.visibleOrGone(isVisible)
//        getBookmark.visibleOrGone(isVisible)
//        getAssetRules.visibleOrGone(isVisible)
//        checkAll.visibleOrGone(isVisible)
        getProductPrice.gone()
        getBookmark.gone()
        getAssetRules.gone()
        checkAll.gone()
        validatePinLayout()
    }

    private fun validatePinLayout() {
        if (parentalRuleId > 0) {
            pinLayout.visible()
        } else {
            pin.string = ""
            pinLayout.gone()
            pinInputLayout.gone()
            insertPin.text = "Insert pin"
        }
    }

//    private fun handleUserRules(userAssetRules: List<UserAssetRule>) {
//        userAssetRules.forEach {
//            if (it.ruleType == RuleType.PARENTAL) {
//                parentalRuleId = it.id.toInt()
//            }
//        }
//    }
}