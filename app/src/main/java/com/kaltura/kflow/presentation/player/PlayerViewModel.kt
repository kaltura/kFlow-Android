package com.kaltura.kflow.presentation.player

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.enums.AssetReferenceType
import com.kaltura.client.enums.AssetType
import com.kaltura.client.enums.PinType
import com.kaltura.client.enums.SocialActionType
import com.kaltura.client.services.*
import com.kaltura.client.types.*
import com.kaltura.client.utils.request.MultiRequestBuilder
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 2020-01-13.
 */
class PlayerViewModel(private val apiManager: PhoenixApiManager,
                      private val preferenceManager: PreferenceManager) : BaseViewModel(apiManager) {

    val asset = MutableLiveData<Resource<Asset>>()
    val userAssetRules = MutableLiveData<Resource<List<UserAssetRule>>>()
    val favoriteList = MutableLiveData<Resource<List<Favorite>>>()
    val getLike = MutableLiveData<Resource<SocialAction>>()
    val doLike = MutableLiveData<Resource<SocialAction>>()
    val doUnlike = MutableLiveData<Resource<Unit>>()
    val doFavorite = MutableLiveData<Resource<Unit>>()
    val doUnfavorite = MutableLiveData<Resource<Unit>>()

    fun loadAsset(assetId: Long) {
        apiManager.execute(AssetService.get(assetId.toString(), AssetReferenceType.EPG_INTERNAL).setCompletion {
            if (it.isSuccess) {
                asset.value = Resource.Success(it.results)
            }
        })
    }

    fun like(assetId: Long) {
        val socialAction = SocialAction().apply {
            this.assetId = assetId
            actionType = SocialActionType.LIKE
        }

        val requestBuilder = SocialActionService.add(socialAction).setCompletion {
            if (it.isSuccess) doLike.value = Resource.Success(it.results.socialAction)
            else doLike.value = Resource.Error(it.error)
        }
        apiManager.execute(requestBuilder)
    }

    fun unlike(likeId: String) {
        val requestBuilder = SocialActionService.delete(likeId).setCompletion {
            if (it.isSuccess) doUnlike.value = Resource.Success(Unit)
            else doUnlike.value = Resource.Error(it.error)
        }
        apiManager.execute(requestBuilder)
    }

    fun favorite(assetId: Long) {
        val favoriteEntity = Favorite().apply { this.assetId = assetId }
        val requestBuilder = FavoriteService.add(favoriteEntity).setCompletion {
            if (it.isSuccess) doFavorite.value = Resource.Success(Unit)
            else doFavorite.value = Resource.Error(it.error)
        }
        apiManager.execute(requestBuilder)
    }

    fun unfavorite(assetId: Long) {
        val requestBuilder = FavoriteService.delete(assetId).setCompletion {
            if (it.isSuccess) doUnfavorite.value = Resource.Success(Unit)
            else doUnfavorite.value = Resource.Error(it.error)
        }
        apiManager.execute(requestBuilder)
    }

    fun getLike(assetId: Long) {
        val socialActionFilter = SocialActionFilter().apply { assetIdIn = assetId.toString() }
        apiManager.execute(SocialActionService.list(socialActionFilter).setCompletion {
            if (it.isSuccess) {
                it.results.objects.forEach {
                    if (it.actionType == SocialActionType.LIKE) {
                        getLike.value = Resource.Success(it)
                        return@forEach
                    }
                }
            }
        })
    }

    fun getFavoriteList(assetId: Long) {
        val favoriteFilter = FavoriteFilter().apply { mediaIdIn = assetId.toString() }
        apiManager.execute(FavoriteService.list(favoriteFilter).setCompletion {
            if (it.isSuccess) {
                if (it.results.objects != null && it.results.objects.isNotEmpty()) {
                    favoriteList.value = Resource.Success(it.results.objects)
                }
            }
        })
    }

    fun checkPinCode(pin: String, parentalRulelId: Int) {
        apiManager.execute(PinService.validate(pin, PinType.PARENTAL, parentalRulelId))
    }

    fun checkAllValidations(assetId: Long) {
        val multiRequestBuilder = MultiRequestBuilder()
        // product price request
        val productPriceFilter = ProductPriceFilter().apply { fileIdIn = assetId.toString() }
        multiRequestBuilder.add(ProductPriceService.list(productPriceFilter))
        // bookmark request
        val bookmarkFilter = BookmarkFilter().apply {
            assetIdIn = assetId.toString()
            assetTypeEqual = AssetType.MEDIA
        }

        multiRequestBuilder.add(BookmarkService.list(bookmarkFilter))
        // asset rules request
        val userAssetRuleFilter = UserAssetRuleFilter().apply {
            assetTypeEqual = 1
            assetIdEqual = assetId
        }

        multiRequestBuilder.add(UserAssetRuleService.list(userAssetRuleFilter))
        multiRequestBuilder.setCompletion {
            if (it.isSuccess) {
                if (it.results != null && it.results[2] != null) {
                    userAssetRules.value = Resource.Success((it.results[2] as ListResponse<UserAssetRule>).objects)
                }
            }
        }
        apiManager.execute(multiRequestBuilder)
    }

    fun getPartnerId() = preferenceManager.partnerId

    fun getBaseUrl() = preferenceManager.baseUrl

    fun getMediaFileFormat() = preferenceManager.mediaFileFormat

    fun getKs() = apiManager.ks

    var urlType: String
        get() = preferenceManager.urlType
        set(value) {
            preferenceManager.urlType = value
        }

    var streamerType: String
        get() = preferenceManager.streamerType
        set(value) {
            preferenceManager.streamerType = value
        }

    var mediaProtocol: String
        get() = preferenceManager.mediaProtocol
        set(value) {
            preferenceManager.mediaProtocol = value
        }

    var codec: String
        get() = preferenceManager.codec
        set(value) {
            preferenceManager.codec = value
        }

    var drm: Boolean
        get() = preferenceManager.drm
        set(value) {
            preferenceManager.drm = value
        }

    var quality: String
        get() = preferenceManager.quality
        set(value) {
            preferenceManager.quality = value
        }
}