package com.kaltura.kflow.presentation

import android.os.Bundle
import android.view.View
import com.kaltura.client.enums.AssetOrderBy
import com.kaltura.client.services.AssetService
import com.kaltura.client.types.Asset
import com.kaltura.client.types.FilterPager
import com.kaltura.client.types.SearchAssetFilter
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.assetList.AssetListFragment
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import kotlinx.android.synthetic.main.fragment_epg.*
import java.util.*

/**
 * Created by alex_lytvynenko on 17.01.2019.
 */
class EpgFragment : DebugFragment(R.layout.fragment_epg) {

    private val channels = ArrayList<Asset>()

    private enum class DateFilter {
        YESTERDAY, TODAY, TOMORROW
    }

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showChannel.navigateOnClick(EpgFragmentDirections.navigateToAssetList()) { arrayOf(AssetListFragment.ARG_ASSETS to channels) }
        yesterday.setOnClickListener { makeGetChannelsRequest(linearMediaId.string, DateFilter.YESTERDAY) }
        today.setOnClickListener { makeGetChannelsRequest(linearMediaId.string, DateFilter.TODAY) }
        tomorrow.setOnClickListener { makeGetChannelsRequest(linearMediaId.string, DateFilter.TOMORROW) }

        showChannel.visibleOrGone(channels.isNotEmpty())
        showChannel.text = getQuantityString(R.plurals.show_programs, channels.size)
    }

    private fun makeGetChannelsRequest(epgChannelId: String, dateFilter: DateFilter) {
        withInternetConnection {
            channels.clear()
            showChannel.gone()
            var startDate = 0L
            var endDate = 0L
            val todayMidnightCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                this[Calendar.MILLISECOND] = 0
                this[Calendar.SECOND] = 0
                this[Calendar.MINUTE] = 0
                this[Calendar.HOUR_OF_DAY] = 0
            }

            when (dateFilter) {
                DateFilter.YESTERDAY -> {
                    endDate = todayMidnightCalendar.timeInMillis / 1000
                    todayMidnightCalendar.add(Calendar.DAY_OF_YEAR, -1)
                    startDate = todayMidnightCalendar.timeInMillis / 1000
                }
                DateFilter.TODAY -> {
                    startDate = todayMidnightCalendar.timeInMillis / 1000
                    todayMidnightCalendar.add(Calendar.DAY_OF_YEAR, 1)
                    endDate = todayMidnightCalendar.timeInMillis / 1000
                }
                DateFilter.TOMORROW -> {
                    todayMidnightCalendar.add(Calendar.DAY_OF_YEAR, 1)
                    startDate = todayMidnightCalendar.timeInMillis / 1000
                    todayMidnightCalendar.add(Calendar.DAY_OF_YEAR, 1)
                    endDate = todayMidnightCalendar.timeInMillis / 1000
                }
            }
            val filter = SearchAssetFilter().apply {
                orderBy = AssetOrderBy.START_DATE_DESC.value
                typeIn = "0"
                kSql = "(and linear_media_id = '$epgChannelId' (and start_date > '$startDate' end_date < '$endDate'))"
            }
            val filterPager = FilterPager().apply {
                pageSize = 100
                pageIndex = 1
            }

            clearDebugView()
            PhoenixApiManager.execute(AssetService.list(filter, filterPager)
                    .setCompletion {
                        if (it.isSuccess) {
                            if (it.results.objects != null) channels.addAll(it.results.objects)
                            showChannel.text = getQuantityString(R.plurals.show_programs, channels.size)
                            showChannel.visible()
                        }
                    })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
        PhoenixApiManager.cancelAll()
    }
}