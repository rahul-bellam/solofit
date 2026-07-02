package com.solofit.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.solofit.app.core.BitmapUtils
import com.solofit.app.data.local.dao.ProgressPhotoDao
import com.solofit.app.data.local.entity.ProgressPhotoEntity
import com.solofit.app.domain.repository.ProgressPhotoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Stores progress-photo bitmaps in the app's PRIVATE internal storage
 * (filesDir/progress_photos). Nothing leaves the device. The DB only keeps file
 * names + metadata. Images are downscaled before saving to keep storage small.
 */
@Singleton
class ProgressPhotoRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: ProgressPhotoDao,
    @Named("io") private val io: CoroutineDispatcher
) : ProgressPhotoRepository {

    private val dir: File by lazy {
        File(context.filesDir, "progress_photos").apply { mkdirs() }
    }

    override fun observeAll(): Flow<List<ProgressPhotoEntity>> = dao.observeAll()

    override fun observeByPose(pose: String): Flow<List<ProgressPhotoEntity>> =
        dao.observeByPose(pose)

    override suspend fun savePhoto(bitmap: Bitmap, date: String, pose: String): Long =
        withContext(io) {
            // Cap to a sensible resolution so progress photos don't bloat storage.
            val capped = BitmapUtils.capInMemory(bitmap, maxEdge = 1080)
            val fileName = "pp_${pose}_${System.currentTimeMillis()}.jpg"
            FileOutputStream(File(dir, fileName)).use { out ->
                capped.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            dao.insert(ProgressPhotoEntity(date = date, pose = pose, fileName = fileName))
        }

    override fun fileFor(fileName: String): File = File(dir, File(fileName).name)

    override suspend fun delete(id: Long) = withContext(io) {
        dao.getById(id)?.let { entity ->
            runCatching { File(dir, File(entity.fileName).name).delete() }
            dao.delete(id)
        }
        Unit
    }
}
