package eu.mihaibadea.requestlab.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

private const val KEY_ALIAS = "requestlab_secret_key"
private const val PROVIDER = "AndroidKeyStore"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_TAG_LENGTH = 128
private const val IV_SIZE = 12

@Singleton
class KeystoreSecretCipher @Inject constructor() : SecretCipher {

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(PROVIDER).also { it.load(null) }
        keyStore.getKey(KEY_ALIAS, null)?.let { return it as SecretKey }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return keyGenerator.generateKey()
    }

    override fun encrypt(plaintext: String): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv  // GCM generates a random 12-byte IV
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return iv + ciphertext  // prepend IV to ciphertext for storage
    }

    override fun rotateKey() {
        val keyStore = KeyStore.getInstance(PROVIDER).also { it.load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) keyStore.deleteEntry(KEY_ALIAS)
        // New key is created lazily on next encrypt call
    }

    override fun decrypt(ciphertext: ByteArray): String {
        require(ciphertext.size > IV_SIZE) { "Ciphertext too short" }
        val iv = ciphertext.copyOfRange(0, IV_SIZE)
        val data = ciphertext.copyOfRange(IV_SIZE, ciphertext.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return String(cipher.doFinal(data), Charsets.UTF_8)
    }
}
