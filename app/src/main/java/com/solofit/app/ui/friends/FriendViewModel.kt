package com.solofit.app.ui.friends

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.entity.FriendEntity
import com.solofit.app.data.local.entity.FriendEventEntity
import com.solofit.app.data.local.entity.FriendGroupEntity
import com.solofit.app.data.local.entity.FriendPermissionEntity
import com.solofit.app.data.local.entity.GroupMemberEntity
import com.solofit.app.core.crypto.CryptoUtil
import com.solofit.app.data.local.entity.SoloIdentityEntity
import com.solofit.app.domain.repository.FriendRepository
import com.solofit.app.domain.repository.GroupRepository
import com.solofit.app.domain.repository.SoloIdentityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendsUiState(
    val identity: SoloIdentityEntity? = null,
    val acceptedFriends: List<FriendEntity> = emptyList(),
    val pendingRequests: List<FriendEntity> = emptyList(),
    val totalAccepted: Int = 0,
    val groups: List<FriendGroupEntity> = emptyList()
)

data class FriendDetailUiState(
    val friend: FriendEntity? = null,
    val permissions: Map<String, String> = emptyMap(),
    val events: List<FriendEventEntity> = emptyList()
)

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val groupRepository: GroupRepository,
    private val identityRepository: SoloIdentityRepository
) : ViewModel() {

    private val _codeError = MutableStateFlow<String?>(null)
    val codeError: StateFlow<String?> = _codeError.asStateFlow()

    private val _addSuccess = MutableStateFlow(false)
    val addSuccess: StateFlow<Boolean> = _addSuccess.asStateFlow()

    // The local Solo identity is a one-shot read (created during onboarding). Held in
    // a flow so it can feed the combined UI state and refresh if regenerated.
    private val identity = MutableStateFlow<SoloIdentityEntity?>(null)

    init {
        viewModelScope.launch { identity.value = identityRepository.get() }
    }

    val uiState: StateFlow<FriendsUiState> = combine(
        friendRepository.observeAccepted(),
        friendRepository.observePending(),
        friendRepository.observeAcceptedCount(),
        groupRepository.observeGroups(),
        identity
    ) { accepted, pending, count, groups, id ->
        FriendsUiState(
            identity = id,
            acceptedFriends = accepted,
            pendingRequests = pending,
            totalAccepted = count,
            groups = groups
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FriendsUiState())

    // Which friend the detail screen is showing; drives [detailState] reactively so
    // saved permissions, events, and relationship type actually load and stay live.
    private val selectedFriendId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val detailState: StateFlow<FriendDetailUiState> = selectedFriendId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(FriendDetailUiState())
            } else {
                combine(
                    friendRepository.observeById(id),
                    friendRepository.observePermissions(id),
                    friendRepository.observeEvents(id)
                ) { friend, permissions, events ->
                    FriendDetailUiState(
                        friend = friend,
                        permissions = permissions.associate { it.category to it.level },
                        events = events
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FriendDetailUiState())

    fun loadIdentity(callback: (SoloIdentityEntity?) -> Unit) {
        viewModelScope.launch {
            callback(identityRepository.get())
        }
    }

    fun addFriendByCode(code: String) {
        viewModelScope.launch {
            _codeError.value = null
            _addSuccess.value = false
            val trimmed = code.trim().uppercase()
            val pattern = Regex("""^SF-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$""")
            if (!pattern.matches(trimmed)) {
                _codeError.value = "Invalid Solo ID format. Use SF-XXXX-XXXX-XXXX"
                return@launch
            }
            val identity = identityRepository.get()
            if (identity != null && trimmed == identity.soloId) {
                _codeError.value = "You can't add yourself as a friend"
                return@launch
            }
            val existing = friendRepository.getBySoloId(trimmed)
            if (existing != null) {
                _codeError.value = "Already in your friends list"
                return@launch
            }
            _codeError.value = "Friend not found. They need to share their Solo ID with you."
        }
    }

    fun addFriendByData(soloId: String, displayName: String, publicKey: ByteArray) {
        viewModelScope.launch {
            friendRepository.addFriend(soloId, displayName, publicKey)
            _addSuccess.value = true
        }
    }

    fun acceptFriend(id: Long) {
        viewModelScope.launch { friendRepository.acceptFriend(id) }
    }

    fun rejectFriend(id: Long) {
        viewModelScope.launch { friendRepository.rejectFriend(id) }
    }

    fun removeFriend(id: Long) {
        viewModelScope.launch { friendRepository.removeFriend(id) }
    }

    fun setRelationshipType(friendId: Long, type: String) {
        viewModelScope.launch {
            friendRepository.setRelationshipType(friendId, type)
        }
    }

    fun loadFriendDetail(friendId: Long) {
        selectedFriendId.value = friendId
    }

    fun setPermission(friendId: Long, category: String, level: String) {
        viewModelScope.launch {
            friendRepository.setPermission(friendId, category, level)
        }
    }

    // ── Groups ──

    fun createGroup(name: String) {
        viewModelScope.launch {
            groupRepository.createGroup(name.trim())
        }
    }

    fun deleteGroup(id: Long) {
        viewModelScope.launch {
            groupRepository.deleteGroup(id)
        }
    }

    fun addMemberToGroup(groupId: Long, friendId: Long) {
        viewModelScope.launch {
            groupRepository.addMember(groupId, friendId)
        }
    }

    fun removeMemberFromGroup(groupId: Long, friendId: Long) {
        viewModelScope.launch {
            groupRepository.removeMember(groupId, friendId)
        }
    }

    // Members of the group currently being managed. Reactive (replaces the old
    // getGroupMembers(callback), whose collect{} leaked a coroutine per call).
    private val selectedGroupId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedGroupMembers: StateFlow<List<GroupMemberEntity>> = selectedGroupId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else groupRepository.observeMembers(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectGroup(groupId: Long?) {
        selectedGroupId.value = groupId
    }

    fun clearCodeError() {
        _codeError.value = null
    }

    fun clearAddSuccess() {
        _addSuccess.value = false
    }

    fun generateQrBitmap(identity: SoloIdentityEntity, size: Int): Bitmap? {
        val content = "SOLOFIT:${identity.soloId}:${identity.displayName}:${CryptoUtil.toBase64(identity.publicKey)}"
        return runCatching {
            val writer = com.google.zxing.qrcode.QRCodeWriter()
            val bitMatrix = writer.encode(content, com.google.zxing.BarcodeFormat.QR_CODE, size, size)
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bmp
        }.getOrNull()
    }
}
