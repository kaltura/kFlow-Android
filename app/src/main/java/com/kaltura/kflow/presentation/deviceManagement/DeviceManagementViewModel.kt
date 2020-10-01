package com.kaltura.kflow.presentation.deviceManagement

import com.kaltura.client.services.*
import com.kaltura.client.types.HouseholdDevice
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource
import com.kaltura.kflow.utils.SingleLiveEvent

/**
 * Created by alex_lytvynenko on 2020-01-14.
 */
class DeviceManagementViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val removeDevice = SingleLiveEvent<Resource<Boolean>>()
    val addDevice = SingleLiveEvent<Resource<Boolean>>()

    fun removeDeviceFromHousehold(udid: String) {
        apiManager.execute(HouseholdDeviceService.delete(udid).setCompletion {
            if (it.isSuccess)
                removeDevice.value = Resource.Success(it.results)
            else removeDevice.value = Resource.Error(it.error)
        })
    }

    fun addDeviceToHousehold(user: String, udid: String) {
        apiManager.execute(DeviceBrandService.list().setCompletion {
            if (it.isSuccess) {
                addDeviceToHousehold(user, udid,
                        it.results.objects.firstOrNull { it.name == "Android" }?.id?.toInt() ?: 1)
            } else addDeviceToHousehold(user, udid, 1)
        })
    }

    private fun addDeviceToHousehold(user: String, udid: String, brandId: Int) {
        val device = HouseholdDevice().apply {
            setBrandId(brandId)
            name = user
            setUdid(udid)
        }
        apiManager.execute(HouseholdDeviceService.add(device).setCompletion {
            if (it.isSuccess)
                addDevice.value = Resource.Success(true)
            else addDevice.value = Resource.Error(it.error)
        })
    }
}