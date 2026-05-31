package com.solofit.app.ui.photos

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.core.DateUtils
import com.solofit.app.data.local.entity.ProgressPhotoEntity
import com.solofit.app.domain.repository.ProgressPhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class Pose(val key: String, val display: String) {
    FRONT("FRONT", "Front"),
    SIDE("SIDE", "Side"),
    BACK("BACK", "Back")
}

data class PhotosState(
    val front: List<ProgressPhotoEntity> = emptyList(),
    val side: List<ProgressPhotoEntity> = emptyList(),
    val back: List<ProgressPhotoEntity> = emptyList()
) {
    fun forPose(p: Pose) = when (p) {
        Pose.FRONT -> front
        Pose.SIDE -> side
        Pose.BACK -> back
    }
}

@HiltViewModel
class ProgressPhotosViewModel @Inject constructor(
    private val repository: ProgressPhotoRepository
) : ViewModel() {

    val state = repository.observeAll()
        .map { all ->
            PhotosState(
                front = all.filter { it.pose == Pose.FRONT.key }.sortedBy { it.date },
                side = all.filter { it.pose == Pose.SIDE.key }.sortedBy { it.date },
                back = all.filter { it.pose == Pose.BACK.key }.sortedBy { it.date }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PhotosState())

    fun save(bitmap: Bitmap, pose: Pose) {
        viewModelScope.launch {
            repository.savePhoto(bitmap, DateUtils.today(), pose.key)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }

    fun fileFor(fileName: String): File = repository.fileFor(fileName)
}
