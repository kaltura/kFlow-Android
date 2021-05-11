package com.kaltura.kflow.presentation.appToken

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.enums.AppTokenHashType
import com.kaltura.client.services.*
import com.kaltura.client.types.AppToken
import com.kaltura.client.types.HouseholdDevice
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.presentation.extension.toHex
import com.kaltura.kflow.utils.Resource
import com.kaltura.kflow.utils.sha256
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by alex_lytvynenko on 2020-01-09.
 */
class AppTokenViewModel(
    private val apiManager: PhoenixApiManager,
    private val preferenceManager: PreferenceManager
) : BaseViewModel(apiManager) {

    private val EXCEEDED_LIMIT_ERROR_CODE = "1001"

    val loginRequest = MutableLiveData<Resource<Unit>>()
    val anonymousLoginRequest = MutableLiveData<Resource<Unit>>()
    val revokeSessionRequest = MutableLiveData<Resource<Unit>>()

    fun makeExplicitLoginRequest(email: String, password: String, udid: String) {
        apiManager.ks = null
        preferenceManager.clearKs()
        apiManager.execute(OttUserService.login(
            preferenceManager.partnerId,
            email,
            password,
            null,
            udid
        ).setCompletion {
            if (it.isSuccess) {
                preferenceManager.clearIotInfo()

                preferenceManager.ks = it.results.loginSession.ks
                apiManager.ks = it.results.loginSession.ks

                getHouseholdDevices(udid, true)
            } else {
                loginRequest.value = Resource.Error(it.error)
            }
        })
    }

    fun makeAppTokenStartRequest(udid: String) {
        if (preferenceManager.ks.isNullOrEmpty() || preferenceManager.appToken.isNullOrEmpty()) {
            anonymousLogin(udid)
        } else {
            apiManager.execute(OttUserService.get().setCompletion {
                if (it.isSuccess) getHouseholdDevices(udid, false)
                else startSession(udid)
            })
        }
    }

    fun makeRevokeSessionRequest() {
        apiManager.execute(SessionService.revoke().setCompletion {
            revokeSessionRequest.value =
                if (it.isSuccess) Resource.Success(Unit) else Resource.Error(it.error)
        })
    }

    fun getSavedUsername() = preferenceManager.authUser

    fun getSavedPassword() = preferenceManager.authPassword

    fun saveUserCreds(user: String, password: String) {
        preferenceManager.authUser = user
        preferenceManager.authPassword = password
    }

    private fun startSession(udid: String) {
        apiManager.ks = null
        apiManager.execute(
            OttUserService.anonymousLogin(preferenceManager.partnerId, udid).setCompletion {
                if (it.isSuccess) {
                    preferenceManager.ks = it.results.ks
                    apiManager.ks = it.results.ks

                    apiManager.execute(AppTokenService.startSession(
                        preferenceManager.appTokenId,
                        sha256(it.results.ks + preferenceManager.appToken).toHex()
                    ).setCompletion {
                        if (it.isSuccess) {
                            preferenceManager.ks = it.results.ks
                            apiManager.ks = it.results.ks
                            getHouseholdDevices(udid, false)
                        } else {
                            loginRequest.value = Resource.Error(it.error)
                        }
                    })
                } else {
                    loginRequest.value = Resource.Error(it.error)
                }
            })
    }

    private fun getHouseholdDevices(currentUdid: String, fromExplicitLogin: Boolean) {
        apiManager.execute(HouseholdDeviceService.list().setCompletion {
            if (it.isSuccess && it.results.objects != null) {
                if (it.results.objects.any { it.udid == currentUdid }) {
                    if (fromExplicitLogin) {
                        addAppToken()
                    } else {
                        loginRequest.value = Resource.Success(Unit)
                    }
                } else addDeviceToHousehold(currentUdid, fromExplicitLogin)
            } else {
                loginRequest.value = Resource.Error(it.error)
            }
        })
    }

    private fun removeDeviceFromHousehold(
        currentUdid: String,
        udidToRemove: String,
        fromExplicitLogin: Boolean
    ) {
        apiManager.execute(HouseholdDeviceService.delete(udidToRemove).setCompletion {
            if (it.isSuccess) addDeviceToHousehold(currentUdid, fromExplicitLogin)
            else logout(currentUdid)
        })
    }

    private fun addDeviceToHousehold(udid: String, fromExplicitLogin: Boolean) {
        apiManager.execute(DeviceBrandService.list().setCompletion {
            if (it.isSuccess) {
                addDeviceToHousehold(
                    udid,
                    it.results.objects.firstOrNull { it.name == "Android" }?.id?.toInt() ?: 1,
                    fromExplicitLogin
                )
            } else addDeviceToHousehold(udid, 1, fromExplicitLogin)
        })
    }

    private fun addDeviceToHousehold(udid: String, brandId: Int, fromExplicitLogin: Boolean) {
        val device = HouseholdDevice().apply {
            setBrandId(brandId)
            name = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
            setUdid(udid)
        }
        apiManager.execute(HouseholdDeviceService.add(device).setCompletion {
            if (it.isSuccess) {
                if (fromExplicitLogin) {
                    addAppToken()
                } else {
                    loginRequest.value = Resource.Success(Unit)
                }
            } else {
                if (it.error.code == EXCEEDED_LIMIT_ERROR_CODE) {
                    apiManager.execute(HouseholdDeviceService.list().setCompletion {
                        if (it.isSuccess && it.results.objects != null)
                            removeDeviceFromHousehold(
                                udid,
                                it.results.objects.last().udid,
                                fromExplicitLogin
                            )
                        else logout(udid)
                    })
                } else {
                    logout(udid)
                }
            }
        })
    }

    private fun addAppToken() {
        val appToken = AppToken().apply {
            // 10 years from current time
            expiry = ((Date().time + 10 * TimeUnit.DAYS.toMillis(365)) / 1000).toInt()
            sessionDuration = 604800
            hashType = AppTokenHashType.SHA256
        }
        apiManager.execute(AppTokenService.add(appToken).setCompletion {
            if (it.isSuccess) {
                preferenceManager.appToken = it.results.token
                preferenceManager.appTokenId = it.results.id
                loginRequest.value = Resource.Success(Unit)
            } else {
                loginRequest.value = Resource.Error(it.error)
            }
        })
    }

    private fun logout(udid: String) {
        apiManager.execute(OttUserService.logout().setCompletion {
            preferenceManager.clearKs()
            anonymousLogin(udid)
        })
    }

    private fun anonymousLogin(udid: String) {
        apiManager.ks = null
        apiManager.execute(OttUserService.anonymousLogin(preferenceManager.partnerId, udid)
            .setCompletion {
                if (it.isSuccess) {
                    preferenceManager.ks = it.results.ks
                    apiManager.ks = it.results.ks
                    anonymousLoginRequest.value = Resource.Success(Unit)
                } else {
                    anonymousLoginRequest.value = Resource.Error(it.error)
                }
            })
    }
}