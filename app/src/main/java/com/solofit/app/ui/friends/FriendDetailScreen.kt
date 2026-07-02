package com.solofit.app.ui.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solofit.app.sol.RelationshipType
import com.solofit.app.ui.theme.SurfaceBg

private val PERMISSION_CATEGORIES = listOf(
    "Workout", "Walking", "Recovery", "Nutrition",
    "Meditation", "Journal", "Body", "Habits", "Milestones", "Status"
)
private val PERMISSION_LEVELS = listOf("private", "specific", "group")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    friendId: Long,
    viewModel: FriendViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit
) {
    val detailState by viewModel.detailState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(friendId) {
        viewModel.loadFriendDetail(friendId)
    }

    if (showDeleteConfirm && detailState.friend != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove ${detailState.friend!!.displayName}?") },
            text = { Text("This will remove them from your circle and all groups.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.removeFriend(friendId); onBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detailState.friend?.displayName ?: "Friend") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBg)
            )
        },
        containerColor = SurfaceBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 12.dp))
                        Column {
                            Text(detailState.friend?.displayName ?: "", style = MaterialTheme.typography.titleMedium)
                            Text(detailState.friend?.soloId ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text("Relationship", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                RelationshipTypeSelector(
                    currentType = detailState.friend?.relationshipType ?: "accountability_partner",
                    onTypeSelected = { type -> viewModel.setRelationshipType(friendId, type) }
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text("Sharing Permissions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("Control what this friend can see", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            items(PERMISSION_CATEGORIES) { category ->
                PermissionRow(
                    category = category,
                    currentLevel = detailState.permissions[category] ?: "private",
                    onLevelChanged = { level ->
                        if (level == "private") {
                            viewModel.setPermission(friendId, category, "private")
                        } else {
                            viewModel.setPermission(friendId, category, level)
                        }
                    }
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Remove Friend", color = MaterialTheme.colorScheme.error)
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RelationshipTypeSelector(currentType: String, onTypeSelected: (String) -> Unit) {
    val selected = runCatching { RelationshipType.fromValue(currentType) }.getOrDefault(RelationshipType.ACCOUNTABILITY_PARTNER)
    var expanded by remember { androidx.compose.runtime.mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text("Type", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = true }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selected.displayName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text("Change", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    if (expanded) {
        AlertDialog(
            onDismissRequest = { expanded = false },
            title = { Text("Relationship Type") },
            text = {
                Column {
                    RelationshipType.entries.forEach { type ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onTypeSelected(type.name); expanded = false }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(type.displayName, modifier = Modifier.weight(1f))
                            if (type == selected) Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { expanded = false }) { Text("Close") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionRow(category: String, currentLevel: String, onLevelChanged: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(category, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                PERMISSION_LEVELS.forEachIndexed { index, level ->
                    SegmentedButton(
                        selected = currentLevel == level,
                        onClick = { onLevelChanged(level) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = PERMISSION_LEVELS.size
                        )
                    ) {
                        Text(
                            when (level) {
                                "private" -> "Private"
                                "specific" -> "Share"
                                "group" -> "Group"
                                else -> level
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
