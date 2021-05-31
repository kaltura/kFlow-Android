package com.kaltura.kflow.presentation.epg

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.types.Asset
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_epg.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

/**
 * Created by alex_lytvynenko on 17.01.2019.
 */
class EpgFragment : SharedTransitionFragment(R.layout.fragment_epg) {

    private val viewModel: EpgViewModel by viewModel()
    private var channels = ArrayList<Asset>()
    private var selectedDateFilter = DateFilter.TODAY

    enum class DateFilter {
        YESTERDAY, TODAY, TOMORROW, HOURS48
    }

    override fun debugView(): DebugView = debugView
    override val feature = Feature.EPG

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showChannel.navigateOnClick { EpgFragmentDirections.navigateToAssetList(assets = channels.toTypedArray()) }
//        yesterday.setOnClickListener { makeGetChannelsRequest(linearMediaId.string, DateFilter.YESTERDAY) }
//        today.setOnClickListener { makeGetChannelsRequest(linearMediaId.string, DateFilter.TODAY) }
//        tomorrow.setOnClickListener { makeGetChannelsRequest(linearMediaId.string, DateFilter.TOMORROW) }
        hours48.setOnClickListener { makeGetChannelsRequest(itemPerPage.string, DateFilter.HOURS48) }
    }

    override fun subscribeUI() {
        observeResource(viewModel.getAssetList,
                error = {
                    when (selectedDateFilter) {
                        DateFilter.YESTERDAY -> yesterday
                        DateFilter.TODAY -> today
                        DateFilter.TOMORROW -> tomorrow
                        DateFilter.HOURS48 -> hours48
                    }.error(lifecycleScope)
                },
                success = {
                    when (selectedDateFilter) {
                        DateFilter.YESTERDAY -> yesterday
                        DateFilter.TODAY -> today
                        DateFilter.TOMORROW -> tomorrow
                        DateFilter.HOURS48 -> hours48
                    }.success(lifecycleScope)
                    channels = it
                    showChannel.text = getQuantityString(R.plurals.show_programs, channels.size)
                    showChannel.visible()
                })
    }

    private fun makeGetChannelsRequest(epgChannelId: String, dateFilter: DateFilter) {
        withInternetConnection {
            hideKeyboard()
            showChannel.gone()
            clearDebugView()
            clearInputLayouts()
            selectedDateFilter = dateFilter

            if (epgChannelId.isEmpty()) {
                itemPerPageInputLayout.showError("Empty items per page")
                return@withInternetConnection
            }

            when (selectedDateFilter) {
                DateFilter.YESTERDAY -> yesterday
                DateFilter.TODAY -> today
                DateFilter.TOMORROW -> tomorrow
                DateFilter.HOURS48 -> hours48
            }.startAnimation {
                viewModel.cancel()
                viewModel.getChannelsRequest(epgChannelId.toInt(), selectedDateFilter)
            }
        }
    }

    private fun clearInputLayouts() {
        itemPerPageInputLayout.hideError()
    }
}