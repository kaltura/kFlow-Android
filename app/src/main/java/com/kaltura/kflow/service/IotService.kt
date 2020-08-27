package com.kaltura.kflow.service

import com.kaltura.client.utils.request.RequestBuilder
import com.kaltura.kflow.entity.Iot
import com.kaltura.kflow.entity.IotClientConfiguration

/**
 * Created by alex_lytvynenko on 25.08.2020.
 */
object IotService {

    class GetClientConfigurationIotBuilder :
            RequestBuilder<IotClientConfiguration, IotClientConfiguration.Tokenizer, GetClientConfigurationIotBuilder>(IotClientConfiguration::class.java, "Iot", "GetClientConfiguration")

    class RegisterIotBuilder :
            RequestBuilder<Iot, Iot.Tokenizer, RegisterIotBuilder>(Iot::class.java, "Iot", "Register")

}