package com.example.requestlab.core.crypto

interface SecretCipher {
    fun encrypt(plaintext: String): ByteArray
    fun decrypt(ciphertext: ByteArray): String
    fun rotateKey()
}
