package com.solofit.app.ui.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solofit.app.domain.model.SoloFitModule
import com.solofit.app.ui.theme.SolAccent
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText
import androidx.compose.material3.MaterialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleManagementScreen(
    onBack: () -> Unit,
    viewModel: ModuleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val enabledModules = state.enabledModules
    val allModules = SoloFitModule.entries.filter { it != SoloFitModule.PROGRESS }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Manage Modules", fontWeight = FontWeight.SemiBold, color = PrimaryText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("Enable or disable modules. Drag to reorder.", fontSize = 14.sp, color = SecondaryText)
                Spacer(Modifier.height(8.dp))
            }
            itemsIndexed(allModules, key = { _, m -> m.id }) { index, module ->
                val enabled = module in enabledModules
                ModuleManageCard(
                    module = module,
                    enabled = enabled,
                    canMoveUp = index > 0,
                    canMoveDown = index < allModules.lastIndex,
                    onToggle = { viewModel.toggle(module) },
                    onMoveUp = {
                        val list = allModules.toMutableList()
                        val item = list.removeAt(index)
                        list.add(index - 1, item)
                        viewModel.reorder(list)
                    },
                    onMoveDown = {
                        val list = allModules.toMutableList()
                        val item = list.removeAt(index)
                        list.add(index + 1, item)
                        viewModel.reorder(list)
                    }
                )
            }
            item {
                Spacer(Modifier.height(24.dp))
                Text("Progress is always enabled.", fontSize = 12.sp, color = SecondaryText)
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ModuleManageCard(
    module: SoloFitModule,
    enabled: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggle: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape)
                        .background(if (enabled) SolAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        moduleIcon(module), null,
                        tint = if (enabled) SolAccent else SecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(module.displayName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = PrimaryText)
                    Text(module.description, fontSize = 11.sp, color = SecondaryText)
                }
            }
        }
        if (canMoveUp) {
            IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.KeyboardArrowUp, "Move up", tint = SecondaryText, modifier = Modifier.size(20.dp))
            }
        } else {
            Spacer(Modifier.width(32.dp))
        }
        if (canMoveDown) {
            IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.KeyboardArrowDown, "Move down", tint = SecondaryText, modifier = Modifier.size(20.dp))
            }
        } else {
            Spacer(Modifier.width(32.dp))
        }
        Switch(
            checked = enabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(checkedTrackColor = SolAccent)
        )
    }
}
