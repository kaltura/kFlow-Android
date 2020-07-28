package com.kaltura.kflow.presentation.bookmark

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.enums.AssetType
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_bookmark.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class BookmarkFragment : SharedTransitionFragment(R.layout.fragment_bookmark) {

    private val viewModel: BookmarkViewModel by viewModel()
    private var bookmarks = arrayListOf<Bookmark>()
    private val assetTypes = arrayListOf(AssetType.MEDIA, AssetType.EPG, AssetType.RECORDING)

    override fun debugView(): DebugView = debugView
    override val feature = Feature.BOOKMARK

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAssetTypes(assetTypes)

        play.setOnClickListener {
            hideKeyboard()
            loadAsset()
        }
        get.setOnClickListener {
            hideKeyboard()
            getBookmarksRequest(assetId.string, assetTypes[assetType.selectedItemPosition])
        }
    }

    private fun initAssetTypes(types: List<AssetType>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types.map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        assetType.adapter = adapter
        assetType.setSelection(0)
    }

    override fun subscribeUI() {
        observeResource(viewModel.bookmarkList,
                error = { get.error(lifecycleScope) },
                success = {
                    get.success(lifecycleScope)
                    bookmarks = it
                    if (bookmarks.isEmpty()) {
                        noBookmarks.visible()
                        position.gone()
                        play.gone()
                    } else {
                        noBookmarks.gone()
                        position.visible()
                        play.visible()
                        position.text = "Position: ${bookmarks.first().position}"
                    }
                })
        observeResource(viewModel.asset,
                error = { play.error(lifecycleScope) },
                success = {
                    play.success(lifecycleScope)
                    play.postDelayed({ playAssets(it) }, 2000)
                })
        observeResource(viewModel.recording,
                error = { play.error(lifecycleScope) },
                success = {
                    play.success(lifecycleScope)
                    play.postDelayed({ playRecording(it) }, 2000)
                })
    }

    private fun getBookmarksRequest(assetId: String, assetType: AssetType) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (assetId.isEmpty()) {
                assetIdInputLayout.showError("Empty asset ID")
                return@withInternetConnection
            }

            noBookmarks.gone()
            position.gone()
            play.gone()

            get.startAnimation {
                viewModel.getBookmarkListRequest(assetId, assetType)
            }
        }
    }

    private fun loadAsset() {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()
            play.startAnimation {
                if (assetTypes[assetType.selectedItemPosition] == AssetType.RECORDING)
                    viewModel.getRecording(recordingId = assetId.string)
                else
                    viewModel.getAsset(assetId = assetId.string, assetType = assetTypes[assetType.selectedItemPosition])
            }
        }
    }

    private fun clearInputLayouts() {
        assetIdInputLayout.hideError()
    }

    private fun playAssets(asset: Asset) {
        if (isAdded.not()) return
        navigate(BookmarkFragmentDirections.navigateToPlayer(asset = asset,
                startPosition = bookmarks.firstOrNull()?.position ?: 0))
    }

    private fun playRecording(recording: Recording) {
        if (isAdded.not()) return
        navigate(BookmarkFragmentDirections.navigateToPlayer(recording = recording,
                startPosition = bookmarks.firstOrNull()?.position ?: 0))
    }
}