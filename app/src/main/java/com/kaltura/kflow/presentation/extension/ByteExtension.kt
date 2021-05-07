package com.kaltura.kflow.presentation.extension

/**
 * Created by alex_lytvynenko on 07.05.2021.
 */
fun ByteArray.toHex(): String {
    val HEX_CHARS = "0123456789abcdef".toCharArray()

    val result = StringBuffer()

    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(HEX_CHARS[firstIndex])
        result.append(HEX_CHARS[secondIndex])
    }

    return result.toString()
}