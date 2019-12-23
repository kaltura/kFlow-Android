package com.kaltura.kflow.presentation

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.view.isGone
import com.kaltura.client.enums.AssetReferenceType
import com.kaltura.client.enums.AssetType
import com.kaltura.client.enums.PinType
import com.kaltura.client.enums.RuleType
import com.kaltura.client.services.*
import com.kaltura.client.types.*
import com.kaltura.client.utils.request.MultiRequestBuilder
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.MainActivity
import com.kaltura.kflow.presentation.player.PlayerFragment
import kotlinx.android.synthetic.main.fragment_media_page.*
import org.jetbrains.anko.support.v4.toast

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class MediaPageFragment : DebugFragment(R.layout.fragment_media_page) {

    companion object {
        const val ARG_KEEP_ALIVE = "extra_keep_alive"
    }

    private var asset: Asset? = null
    private var parentalRuleId = 0
    private var isKeepAlive = false

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = "Media page"

        arguments?.let { isKeepAlive = it.getBoolean(ARG_KEEP_ALIVE) }

        playAsset.setOnClickListener {
            hideKeyboard()
            replaceFragment(instanceOf<PlayerFragment>(PlayerFragment.ARG_ASSET to asset!!, PlayerFragment.ARG_KEEP_ALIVE to isKeepAlive), addToBackStack = true)
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

    private fun getAssetRequest(assetId: String) {
        withInternetConnection {
            asset = null
            parentalRuleId = 0
            pin.string = ""
            validateButtons()
            clearDebugView()
            PhoenixApiManager.execute(AssetService.get(assetId, AssetReferenceType.MEDIA).setCompletion {
                if (it.isSuccess) {
                    if (it.results != null) asset = it.results
                    validateButtons()
                }
            })
        }
    }

    private fun getProductPriceRequest(assetId: String) {
        withInternetConnection {
            val productPriceFilter = ProductPriceFilter().apply { fileIdIn = assetId }
            clearDebugView()
            PhoenixApiManager.execute(ProductPriceService.list(productPriceFilter))
        }
    }

    private fun getBookmarkRequest(assetId: String) {
        withInternetConnection {
            val bookmarkFilter = BookmarkFilter().apply {
                assetIdIn = assetId
                assetTypeEqual = AssetType.MEDIA
            }
            clearDebugView()
            PhoenixApiManager.execute(BookmarkService.list(bookmarkFilter))
        }
    }

    private fun getAssetRulesRequest(assetId: String) {
        withInternetConnection {
            if (TextUtils.isDigitsOnly(assetId)) {
                val userAssetRuleFilter = UserAssetRuleFilter().apply {
                    assetTypeEqual = 1
                    assetIdEqual = assetId.toLong()
                }
                clearDebugView()
                PhoenixApiManager.execute(UserAssetRuleService.list(userAssetRuleFilter).setCompletion {
                    if (it.isSuccess) {
                        it.results?.objects?.let { handleUserRules(it) }
                        validateButtons()
                    }
                })
            } else {
                toast("Wrong input")
            }
        }
    }

    private fun checkAllTogetherRequest(assetId: String) {
        withInternetConnection {
            if (TextUtils.isDigitsOnly(assetId)) {
                val multiRequestBuilder = MultiRequestBuilder()
                // product price request
                val productPriceFilter = ProductPriceFilter().apply { fileIdIn = assetId }
                multiRequestBuilder.add(ProductPriceService.list(productPriceFilter))
                // bookmark request
                val bookmarkFilter = BookmarkFilter().apply {
                    assetIdIn = assetId
                    assetTypeEqual = AssetType.MEDIA
                }

                multiRequestBuilder.add(BookmarkService.list(bookmarkFilter))
                // asset rules request
                val userAssetRuleFilter = UserAssetRuleFilter().apply {
                    assetTypeEqual = 1
                    assetIdEqual = assetId.toLong()
                }

                multiRequestBuilder.add(UserAssetRuleService.list(userAssetRuleFilter))
                multiRequestBuilder.setCompletion {
                    if (it.isSuccess) {
                        if (it.results != null && it.results[2] != null) {
                            handleUserRules((it.results[2] as ListResponse<UserAssetRule>).objects)
                        }
                        validateButtons()
                    }
                }
                clearDebugView()
                PhoenixApiManager.execute(multiRequestBuilder)
            } else {
                Toast.makeText(requireContext(), "Wrong input", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPinRequest(pin: String) {
        withInternetConnection {
            if (TextUtils.isDigitsOnly(pin)) {
                clearDebugView()
                PhoenixApiManager.execute(PinService.validate(pin, PinType.PARENTAL, parentalRuleId))
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

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
        PhoenixApiManager.cancelAll()
    }
}