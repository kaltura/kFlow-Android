package com.kaltura.kflow.presentation.aws

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.amazonaws.mobile.client.results.SignInState
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_iot.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class IotFragment : SharedTransitionFragment(R.layout.fragment_iot) {

    //Shadow Messages Keys
    private val KEY_STATE = "state"
    private val KEY_DESIRED = "desired"
    private val KEY_NEW_MESSAGE = "NewMessage"

    //Topic Announcments Keys
    private val KEY_ANNONCMENT_TOPIC = "message"

    private val viewModel: IotViewModel by viewModel()

    override fun debugView(): DebugView = debugView
    override val feature = Feature.IOT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        register.setOnClickListener {
            makeRegisterRequest()
        }
        connect.setOnClickListener {
            viewModel.connect()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.registrationEvent,
                error = { register.error(lifecycleScope) },
                success = {
                    register.success(lifecycleScope)
                    Snackbar.make(requireView(), when (it) {
                        SignInState.DONE -> {
                            connect.visible()
                            viewModel.initMqtt()
                            "AWS Sign-in done."
                        }
                        SignInState.SMS_MFA -> "Please confirm sign-in with SMS."
                        SignInState.NEW_PASSWORD_REQUIRED -> "Please confirm sign-in with new password."
                        else -> "Unsupported sign-in confirmation: $it"
                    }, Snackbar.LENGTH_LONG).show()
                })
        observeResource(viewModel.connectEvent,
                error = {
                    it.printStackTrace()
                    longToast("Mqtt Connection error: $it")
                },
                success = {
                    if (it == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                        longToast("Mqtt Is Connected")
                        viewModel.subscribeToTopicAnnouncement()
                        viewModel.subscribeToThingShadow()
                    }
                })
        observeResource(viewModel.shadowMessageEvent) {
            try {
                val parser = JsonParser()
                val jsonObject = parser.parse(it) as JsonObject
                val setPoint = jsonObject.getAsJsonObject(KEY_STATE).getAsJsonObject(KEY_DESIRED)[KEY_NEW_MESSAGE].asString
                longToast(setPoint)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                longToast("Error Parsing Message : $e")
            }
        }
        observeResource(viewModel.announcementMessageEvent,
                error = {
                    it.printStackTrace()
                    longToast("Subscribe to topic error: $it")
                },
                success = {
                    longToast(it)
                })
    }

    private fun makeRegisterRequest() {
        withInternetConnection {
            clearDebugView()

            register.startAnimation {
                viewModel.register()
            }
        }
    }
}