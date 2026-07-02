package com.solofit.app.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoUtil @Inject constructor() {

    companion object {
        private const val KEYSTORE_ALIAS = "solo_identity_key"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
        private const val SOLO_ID_PREFIX = "SF"

        fun generateSoloId(): String {
            val random = java.security.SecureRandom()
            val segments = (0 until 3).map {
                val bytes = ByteArray(2)
                random.nextBytes(bytes)
                String.format("%04X", ((bytes[0].toInt() and 0xFF) shl 8) or (bytes[1].toInt() and 0xFF))
            }
            return "$SOLO_ID_PREFIX-${segments[0]}-${segments[1]}-${segments[2]}"
        }

        fun toBase64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
        fun fromBase64(encoded: String): ByteArray = Base64.decode(encoded, Base64.NO_WRAP)
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
    }

    fun generateKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, KEYSTORE_PROVIDER)
        generator.initialize(
            KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_SIGN
            )
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setUserAuthenticationRequired(false)
                .build()
        )
        return generator.generateKeyPair()
    }

    fun getPublicKeyBytes(): ByteArray? {
        return runCatching {
            val entry = keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.PrivateKeyEntry
            entry?.certificate?.publicKey?.encoded
        }.getOrNull()
    }

    fun sign(data: ByteArray): ByteArray? {
        return runCatching {
            val entry = keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.PrivateKeyEntry
                ?: return null
            val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
            signature.initSign(entry.privateKey)
            signature.update(data)
            signature.sign()
        }.getOrNull()
    }

    fun verify(data: ByteArray, signatureBytes: ByteArray, publicKeyBytes: ByteArray): Boolean {
        return runCatching {
            val keyFactory = java.security.KeyFactory.getInstance("EC")
            val pubKeySpec = java.security.spec.X509EncodedKeySpec(publicKeyBytes)
            val publicKey = keyFactory.generatePublic(pubKeySpec)
            val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
            signature.initVerify(publicKey)
            signature.update(data)
            signature.verify(signatureBytes)
        }.getOrDefault(false)
    }

    fun keyExists(): Boolean {
        return runCatching { keyStore.containsAlias(KEYSTORE_ALIAS) }.getOrDefault(false)
    }

    fun deleteKey() {
        runCatching { keyStore.deleteEntry(KEYSTORE_ALIAS) }
    }

    fun sha256(data: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(data)

    fun generateNonce(): ByteArray {
        val random = java.security.SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes
    }
}
