package com.kaltura.kflow.presentation.mediaPage

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import br.com.simplepass.loadingbutton.presentation.State
import com.kaltura.client.enums.RuleType
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_media_page.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.jetbrains.anko.support.v4.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class MediaPageFragment : SharedTransitionFragment(R.layout.fragment_media_page) {

    private val viewModel: MediaPageViewModel by viewModel()
    private val args: MediaPageFragmentArgs by navArgs()
    private var parentalRuleId = 0
    private var asset: Asset? = null

    override fun debugView(): DebugView = debugView
    override val feature by lazy {
        when {
            args.isKeepAlive -> Feature.KEEP_ALIVE
            args.isPPV -> Feature.PPV
            else -> Feature.MEDIA_PAGE
        }
    }

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
        observeResource(viewModel.asset,
                error = { get.error(lifecycleScope) },
                success = {
                    get.success(lifecycleScope)
                    asset = it
                    validateButtons()
                })
        observeResource(viewModel.productPrices,
                error = { getProductPrice.error(lifecycleScope) },
                success = { getProductPrice.success(lifecycleScope) })
        observeResource(viewModel.bookmarks,
                error = { getBookmark.error(lifecycleScope) },
                success = { getBookmark.success(lifecycleScope) })
        observeResource(viewModel.userAssetRules,
                error = {
                    if (getAssetRules.getState() == State.PROGRESS) getAssetRules.error(lifecycleScope)
                    else checkAll.error(lifecycleScope)
                },
                success = {
                    if (getAssetRules.getState() == State.PROGRESS) getAssetRules.success(lifecycleScope)
                    else checkAll.success(lifecycleScope)
                    handleUserRules(it)
                    validateButtons()
                })
    }

    private fun getAssetRequest(assetId: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (assetId.isEmpty()) {
                mediaIdInputLayout.showError("Empty media ID")
                return@withInternetConnection
            }

            asset = null
            parentalRuleId = 0
            pin.string = ""
            validateButtons()
            get.startAnimation {
                viewModel.getAsset(assetId)
            }
        }
    }

    private fun getProductPriceRequest(assetId: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (assetId.isEmpty()) {
                mediaIdInputLayout.showError("Empty media ID")
                return@withInternetConnection
            }

            getProductPrice.startAnimation {
                viewModel.getProductPrice(assetId)
            }
        }
    }

    private fun getBookmarkRequest(assetId: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (assetId.isEmpty()) {
                mediaIdInputLayout.showError("Empty media ID")
                return@withInternetConnection
            }

            getBookmark.startAnimation {
                viewModel.getBookmark(assetId)
            }
        }
    }

    private fun getAssetRulesRequest(assetId: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (assetId.isEmpty()) {
                mediaIdInputLayout.showError("Empty media ID")
                return@withInternetConnection
            }
            if (TextUtils.isDigitsOnly(assetId).not()) {
                mediaIdInputLayout.showError("Wrong input")
                return@withInternetConnection
            }

            getAssetRules.startAnimation {
                viewModel.getAssetRules(assetId)
            }
        }
    }

    private fun checkAllTogetherRequest(assetId: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (assetId.isEmpty()) {
                mediaIdInputLayout.showError("Empty media ID")
                return@withInternetConnection
            }
            if (TextUtils.isDigitsOnly(assetId).not()) {
                mediaIdInputLayout.showError("Wrong input")
                return@withInternetConnection
            }

            checkAll.startAnimation {
                viewModel.checkAllTogether(assetId)
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

    private fun clearInputLayouts() {
        mediaIdInputLayout.hideError()
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