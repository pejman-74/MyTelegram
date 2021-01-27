package com.mytelegram.util.encryption

import android.util.Base64
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/*
* this class will be used for encrypt the socket.io communication
* */
class EncryptUtil {

    fun aesEncrypt(text: String,key:String): String {
        val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(Base64.decode(key, Base64.NO_WRAP), "AES")
        val randomSecureRandom = SecureRandom()
        val iv = ByteArray(cipher.blockSize)
        randomSecureRandom.nextBytes(iv)
        val ivParams = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParams)
        return iv.asHexString + ":" + Base64.encodeToString(
            cipher.doFinal(text.toByteArray(Charsets.UTF_8)),
            Base64.NO_WRAP
        )

    }

    fun aesDecrypt(encrypted: String,key:String): String {
        val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(Base64.decode(key, Base64.NO_WRAP), "AES")
        val sEncrypt = encrypted.split(":").toTypedArray()
        val ivParams = IvParameterSpec(sEncrypt[0].hexAsByteArray)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParams)
        return cipher.doFinal(Base64.decode(sEncrypt[1], Base64.NO_WRAP))
            .toString(Charsets.UTF_8)
    }


    private val ByteArray.asHexString
        inline get() =
            this.joinToString(separator = "") { String.format("%02X", (it.toInt() and 0xFF)) }

    private val String.hexAsByteArray
        inline get() =
            this.chunked(2).map { it.toUpperCase(Locale.getDefault()).toInt(16).toByte() }
                .toByteArray()
}