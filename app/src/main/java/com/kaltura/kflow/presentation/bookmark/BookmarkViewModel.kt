package com.kaltura.kflow.presentation.bookmark

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.enums.AssetReferenceType
import com.kaltura.client.enums.AssetType
import com.kaltura.client.services.AssetService
import com.kaltura.client.services.BookmarkService
import com.kaltura.client.services.RecordingService
import com.kaltura.client.types.*
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource
import com.kaltura.kflow.utils.SingleLiveEvent

/**
 * Created by alex_lytvynenko on 2020-01-14.
 */
class BookmarkViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val bookmarkList = MutableLiveData<Resource<ArrayList<Bookmark>>>()
    val asset = SingleLiveEvent<Resource<Asset>>()
    val recording = SingleLiveEvent<Resource<Recording>>()

    fun getBookmarkListRequest(assetId: String, assetType: AssetType) {
        apiManager.cancelAll()
        val filter = BookmarkFilter().apply {
            assetIdIn = assetId
            assetTypeEqual = assetType
        }
        apiManager.execute(BookmarkService.list(filter).setCompletion {
            if (it.isSuccess) {
                if (it.results.objects != null) bookmarkList.value = Resource.Success(it.results.objects as ArrayList<Bookmark>)
                else bookmarkList.value = Resource.Success(arrayListOf())
            } else bookmarkList.value = Resource.Error(it.error)
        })
    }

    fun getAsset(assetId: String, assetType: AssetType) {
        val assetReferenceType =
                if (assetType == AssetType.EPG) AssetReferenceType.EPG_INTERNAL
                else AssetReferenceType.MEDIA

        apiManager.execute(AssetService.get(assetId, assetReferenceType).setCompletion {
            if (it.isSuccess) {
                asset.value = Resource.Success(it.results)
            }
        })
    }

    fun getRecording(recordingId: String) {
        apiManager.execute(RecordingService.get(recordingId.toLong()).setCompletion {
            if (it.isSuccess) {
                recording.value = Resource.Success(it.results)
            }
        })
    }
}