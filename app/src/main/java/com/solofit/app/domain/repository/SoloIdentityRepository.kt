package com.solofit.app.domain.repository

import com.solofit.app.data.local.entity.SoloIdentityEntity

interface SoloIdentityRepository {
    suspend fun get(): SoloIdentityEntity?
    suspend fun createIfNeeded(displayName: String): SoloIdentityEntity
    suspend fun delete()
}
