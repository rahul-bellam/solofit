package com.solofit.app.data.repository

import com.solofit.app.core.crypto.CryptoUtil
import com.solofit.app.data.local.dao.SoloIdentityDao
import com.solofit.app.data.local.entity.SoloIdentityEntity
import com.solofit.app.domain.repository.SoloIdentityRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoloIdentityRepositoryImpl @Inject constructor(
    private val dao: SoloIdentityDao,
    private val crypto: CryptoUtil
) : SoloIdentityRepository {

    override suspend fun get(): SoloIdentityEntity? = dao.get()

    override suspend fun createIfNeeded(displayName: String): SoloIdentityEntity {
        dao.get()?.let { return it }
        val soloId = CryptoUtil.generateSoloId()
        if (!crypto.keyExists()) {
            crypto.generateKeyPair()
        }
        val publicKey = crypto.getPublicKeyBytes()
            ?: error("Failed to retrieve public key after generation")
        val entity = SoloIdentityEntity(
            soloId = soloId,
            displayName = displayName,
            publicKey = publicKey
        )
        dao.insert(entity)
        return entity
    }

    override suspend fun delete() {
        crypto.deleteKey()
        dao.delete()
    }
}
