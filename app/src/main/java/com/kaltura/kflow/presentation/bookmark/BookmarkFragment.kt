package com.kaltura.kflow.presentation.bookmark

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.kaltura.client.enums.AssetType
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentBookmarkBinding
import com.kaltura.kflow.databinding.FragmentRecordingsBinding
import com.kaltura.kflow.databinding.FragmentVodListBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class BookmarkFragment : SharedTransitionFragment(R.layout.fragment_bookmark) {

    private val viewModel: BookmarkViewModel by viewModel()
    private var bookmarks = arrayListOf<Bookmark>()
    private val assetTypes = arrayListOf(AssetType.MEDIA, AssetType.EPG, AssetType.RECORDING)

    override val feature = Feature.BOOKMARK
    private var _binding: FragmentBookmarkBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentBookmarkBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAssetTypes(assetTypes)

        binding.play.setOnClickListener {
            hideKeyboard()
            loadAsset()
        }
        binding.get.setOnClickListener {
            hideKeyboard()
            getBookmarksRequest(binding.assetId.string, assetTypes[binding.assetType.selectedItemPosition])
        }
    }

    private fun initAssetTypes(types: List<AssetType>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types.map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.assetType.adapter = adapter
        binding.assetType.setSelection(0)
    }

    override fun subscribeUI() {
        observeResource(viewModel.bookmarkList,
                error = { binding.get.error(lifecycleScope) },
                success = {
                    binding.get.success(lifecycleScope)
                    bookmarks = it
                    if (bookmarks.isEmpty()) {
                        binding.noBookmarks.visible()
                        binding.position.gone()
                        binding.play.gone()
                    } else {
                        binding.noBookmarks.gone()
                        binding.position.visible()
                        binding.play.visible()
                        binding.position.text = "Position: ${bookmarks.first().position}"
                    }
                })
        observeResource(viewModel.asset,
                error = {
                    Snackbar.make(requireView(), "Error fetching asset", Snackbar.LENGTH_LONG).show()
                },
                success = {
                    binding.play.isEnabled = true
                    playAssets(it)
                })
        observeResource(viewModel.recording,
                error = {
                    Snackbar.make(requireView(), "Error fetching recording", Snackbar.LENGTH_LONG).show()
                },
                success = {
                    binding.play.isEnabled = true
                    playRecording(it)
                })
    }

    private fun getBookmarksRequest(assetId: String, assetType: AssetType) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (assetId.isEmpty()) {
                binding.assetIdInputLayout.showError("Empty asset ID")
                return@withInternetConnection
            }

            binding.noBookmarks.gone()
            binding.position.gone()
            binding.play.gone()

            binding.get.startAnimation {
                viewModel.getBookmarkListRequest(assetId, assetType)
            }
        }
    }

    private fun loadAsset() {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            binding.play.isEnabled = false

            if (assetTypes[binding.assetType.selectedItemPosition] == AssetType.RECORDING)
                viewModel.getRecording(recordingId = binding.assetId.string)
            else
                viewModel.getAsset(assetId = binding.assetId.string, assetType = assetTypes[binding.assetType.selectedItemPosition])
        }
    }

    private fun clearInputLayouts() {
        binding.assetIdInputLayout.hideError()
    }

    private fun playAssets(asset: Asset) {
        Handler().postDelayed({
            navigate(BookmarkFragmentDirections.navigateToPlayer(asset = asset,
                    startPosition = bookmarks.firstOrNull()?.position ?: 0))
        }, 200)
    }

    private fun playRecording(recording: Recording) {
        Handler().postDelayed({
            navigate(BookmarkFragmentDirections.navigateToPlayer(recording = recording,
                    startPosition = bookmarks.firstOrNull()?.position ?: 0))
        }, 200)
    }
}