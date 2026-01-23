package com.kaltura.kflow.presentation.mediaPage

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.github.leandroborgesferreira.loadingbutton.presentation.State
import com.kaltura.client.enums.RuleType
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentFavoritesBinding
import com.kaltura.kflow.databinding.FragmentMainBinding
import com.kaltura.kflow.databinding.FragmentMediaPageBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class MediaPageFragment : SharedTransitionFragment(R.layout.fragment_media_page) {

    private val viewModel: MediaPageViewModel by viewModel()
    private val args: MediaPageFragmentArgs by navArgs()
    private var parentalRuleId = 0
    private var asset: Asset? = null

    override val feature by lazy {
        when {
            args.isKeepAlive -> Feature.KEEP_ALIVE
            else -> Feature.MEDIA_PAGE
        }
    }
    private var _binding: FragmentMediaPageBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentMediaPageBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.playAsset.setOnClickListener {
            navigate(MediaPageFragmentDirections.navigateToPlayer(args.isKeepAlive, asset = asset!!))
        }
        binding.getProductPrice.setOnClickListener {
            hideKeyboard()
            getProductPriceRequest(binding.mediaId.string)
        }
        binding.getBookmark.setOnClickListener {
            hideKeyboard()
            getBookmarkRequest(binding.mediaId.string)
        }
        binding.getAssetRules.setOnClickListener {
            hideKeyboard()
            getAssetRulesRequest(binding.mediaId.string)
        }
        binding.checkAll.setOnClickListener {
            hideKeyboard()
            checkAllTogetherRequest(binding.mediaId.string)
        }
        binding.insertPin.setOnClickListener {
            hideKeyboard()
            if (binding.pinInputLayout.isGone) {
                showPinInput()
            } else {
                checkPinRequest(binding.pin.string)
            }
        }
        binding.get.setOnClickListener {
            hideKeyboard()
            getAssetRequest(binding.mediaId.string)
        }
        validateButtons()
    }

    override fun subscribeUI() {
        observeResource(viewModel.asset,
                error = { binding.get.error(lifecycleScope) },
                success = {
                    binding.get.success(lifecycleScope)
                    asset = it
                    validateButtons()
                })
        observeResource(viewModel.productPrices,
                error = { binding.getProductPrice.error(lifecycleScope) },
                success = { binding.getProductPrice.success(lifecycleScope) })
        observeResource(viewModel.bookmarks,
                error = { binding.getBookmark.error(lifecycleScope) },
                success = { binding.getBookmark.success(lifecycleScope) })
        observeResource(viewModel.userAssetRules,
                error = {
                    if (binding.getAssetRules.getState() == State.PROGRESS) binding.getAssetRules.error(lifecycleScope)
                    else binding.checkAll.error(lifecycleScope)
                },
                success = {
                    if (binding.getAssetRules.getState() == State.PROGRESS) binding.getAssetRules.success(lifecycleScope)
                    else binding.checkAll.success(lifecycleScope)
                    handleUserRules(it)
                    validateButtons()
                })
    }

    private fun getAssetRequest(assetId: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (assetId.isEmpty()) {
                binding.mediaIdInputLayout.showError("Empty media ID")
                return@withInternetConnection
            }

            asset = null
            parentalRuleId = 0
            binding.pin.string = ""
            validateButtons()
            binding.get.startAnimation {
                viewModel.getAsset(assetId)
            }
        }
    }

    private fun getProductPriceRequest(assetId: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (assetId.isEmpty()) {
                binding.mediaIdInputLayout.showError("Empty media ID")
                return@withInternetConnection
            }

            binding.getProductPrice.startAnimation {
                viewModel.getProductPrice(assetId)
            }
        }
    }

    private fun getBookmarkRequest(assetId: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (assetId.isEmpty()) {
                binding.mediaIdInputLayout.showError("Empty media ID")
                return@withInternetConnection
            }

            binding.getBookmark.startAnimation {
                viewModel.getBookmark(assetId)
            }
        }
    }

    private fun getAssetRulesRequest(assetId: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (assetId.isEmpty()) {
                binding.mediaIdInputLayout.showError("Empty media ID")
                return@withInternetConnection
            }
            if (TextUtils.isDigitsOnly(assetId).not()) {
                binding.mediaIdInputLayout.showError("Wrong input")
                return@withInternetConnection
            }

            binding.getAssetRules.startAnimation {
                viewModel.getAssetRules(assetId)
            }
        }
    }

    private fun checkAllTogetherRequest(assetId: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (assetId.isEmpty()) {
                binding.mediaIdInputLayout.showError("Empty media ID")
                return@withInternetConnection
            }
            if (TextUtils.isDigitsOnly(assetId).not()) {
                binding.mediaIdInputLayout.showError("Wrong input")
                return@withInternetConnection
            }

            binding.checkAll.startAnimation {
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
        binding.mediaIdInputLayout.hideError()
    }

    private fun showPinInput() {
        binding.pinInputLayout.visible()
        binding.insertPin.text = "Check pin"
        showKeyboard(binding.pin)
    }

    private fun validateButtons() {
        val isVisible = asset != null
        binding.playAsset.visibleOrGone(isVisible)
        binding.getProductPrice.visibleOrGone(isVisible)
        binding.getBookmark.visibleOrGone(isVisible)
        binding.getAssetRules.visibleOrGone(isVisible)
        binding.checkAll.visibleOrGone(isVisible)
        validatePinLayout()
    }

    private fun validatePinLayout() {
        if (parentalRuleId > 0) {
            binding.pinLayout.visible()
        } else {
            binding.pin.string = ""
            binding.pinLayout.gone()
            binding.pinInputLayout.gone()
            binding.insertPin.text = "Insert pin"
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