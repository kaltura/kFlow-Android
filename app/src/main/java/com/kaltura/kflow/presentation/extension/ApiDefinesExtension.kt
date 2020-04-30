package com.kaltura.kflow.presentation.extension

import com.kaltura.playkit.providers.api.phoenix.APIDefines

/**
 * Created by alex_lytvynenko on 30.04.2020.
 */
fun playbackContextTypeFromString(key: String): APIDefines.PlaybackContextType? {
    val map = APIDefines.PlaybackContextType.values().associateBy(APIDefines.PlaybackContextType::value)
    return map[key]
}
