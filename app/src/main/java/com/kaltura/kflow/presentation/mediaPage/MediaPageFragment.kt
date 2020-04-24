package com.kaltura.kflow.presentation.mediaPage

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.view.isGone
import androidx.navigation.fragment.navArgs
import com.kaltura.client.enums.RuleType
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
    private val args: MediaPageFragmentArgs by navArgs()
    private var parentalRuleId = 0
    private var asset: Asset? = null

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playAsset.setOnClickListener {
            if (args.isPPV && asset !is MediaAsset)
                toast("Invalid asset for PPV")
            else navigate(MediaPageFragmentDirections.navigateToPlayer(args.isKeepAlive,
                    args.isPPV, asset = asset!!))
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
        observeResource(viewModel.asset) {
            asset = it
            validateButtons()
        }
        observeResource(viewModel.userAssetRules) {
            handleUserRules(it)
            validateButtons()
        }
    }

    private fun getAssetRequest(assetId: String) {
        withInternetConnection {
            asset = null
            parentalRuleId = 0
            pin.string = ""
            validateButtons()
            clearDebugView()
            viewModel.getAsset(assetId)
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
        val isVisible = asset != null
        playAsset.visibleOrGone(isVisible)
        getProductPrice.visibleOrGone(isVisible)
        getBookmark.visibleOrGone(isVisible)
        getAssetRules.visibleOrGone(isVisible)
        checkAll.visibleOrGone(isVisible)
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

    private fun handleUserRules(userAssetRules: List<UserAssetRule>) {
        userAssetRules.forEach {
            if (it.ruleType == RuleType.PARENTAL) {
                parentalRuleId = it.id.toInt()
            }
        }
    }
}