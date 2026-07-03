package com.solofit.app.ui.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import com.solofit.app.data.local.entity.FriendEntity
import com.solofit.app.data.local.entity.FriendGroupEntity
import com.solofit.app.ui.theme.SurfaceBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    viewModel: FriendViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val members by viewModel.selectedGroupMembers.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var manageGroup by remember { mutableStateOf<FriendGroupEntity?>(null) }

    if (showCreate) {
        CreateGroupDialog(
            onDismiss = { showCreate = false },
            onCreate = { name ->
                viewModel.createGroup(name)
                showCreate = false
            }
        )
    }

    manageGroup?.let { group ->
        LaunchedEffect(group.id) { viewModel.selectGroup(group.id) }
        ManageMembersDialog(
            group = group,
            allFriends = state.acceptedFriends,
            memberIds = members.map { it.friendId }.toSet(),
            onAdd = { friendId -> viewModel.addMemberToGroup(group.id, friendId) },
            onRemove = { friendId -> viewModel.removeMemberFromGroup(group.id, friendId) },
            onDismiss = {
                manageGroup = null
                viewModel.selectGroup(null)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Groups") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Filled.Add, "Create Group")
            }
        },
        containerColor = SurfaceBg
    ) { padding ->
        if (state.groups.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No groups yet.\nTap + to create your first accountability circle.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(state.groups) { group ->
                    GroupCard(
                        group = group,
                        onManage = { manageGroup = group },
                        onDelete = { viewModel.deleteGroup(group.id) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun GroupCard(group: FriendGroupEntity, onManage: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onManage,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Groups, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.padding(end = 8.dp))
            Column(Modifier.weight(1f)) {
                Text(group.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = onManage) {
                Icon(Icons.Filled.Group, "Manage members", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ManageMembersDialog(
    group: FriendGroupEntity,
    allFriends: List<FriendEntity>,
    memberIds: Set<Long>,
    onAdd: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(group.name) },
        text = {
            if (allFriends.isEmpty()) {
                Text(
                    "Add friends first, then organise them into this group.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    val current = allFriends.filter { it.id in memberIds }
                    val available = allFriends.filter { it.id !in memberIds }

                    Text("Members", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    if (current.isEmpty()) {
                        Text(
                            "No members yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    current.forEach { friend ->
                        MemberRow(
                            name = friend.displayName,
                            icon = Icons.Filled.Close,
                            contentDescription = "Remove ${friend.displayName}",
                            tint = MaterialTheme.colorScheme.error,
                            onClick = { onRemove(friend.id) }
                        )
                    }

                    if (available.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Add", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        available.forEach { friend ->
                            MemberRow(
                                name = friend.displayName,
                                icon = Icons.Filled.Add,
                                contentDescription = "Add ${friend.displayName}",
                                tint = MaterialTheme.colorScheme.primary,
                                onClick = { onAdd(friend.id) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}

@Composable
private fun MemberRow(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription, tint = tint)
        }
    }
}

@Composable
private fun CreateGroupDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Group") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Group name (e.g. Office Team, Family)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onCreate(name.trim()) }, enabled = name.isNotBlank()) {
                Text("Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
