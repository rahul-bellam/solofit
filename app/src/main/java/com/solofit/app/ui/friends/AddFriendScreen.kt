package com.solofit.app.ui.friends

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solofit.app.ui.theme.SurfaceBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendScreen(
    viewModel: FriendViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var showQr by remember { mutableStateOf(false) }
    val error by viewModel.codeError.collectAsState()
    val success by viewModel.addSuccess.collectAsState()

    LaunchedEffect(success) {
        if (success) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Friend") },
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
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Share your Solo ID or scan a friend's code",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showQr = !showQr },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.QrCodeScanner, null)
                Text(if (showQr) "Hide QR Code" else "Show My QR Code")
            }

            if (showQr) {
                MyQrCode(viewModel = viewModel)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Or enter their Solo ID manually",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )

            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase() },
                label = { Text("Solo ID (SF-XXXX-XXXX-XXXX)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = error != null
            )

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            TextButton(
                onClick = { viewModel.addFriendByCode(code) },
                enabled = code.length >= 17
            ) {
                Text("Send Friend Request")
            }
        }
    }
}

@Composable
private fun MyQrCode(viewModel: FriendViewModel) {
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var size by remember { mutableIntStateOf(256) }
    var identity by remember { mutableStateOf<com.solofit.app.data.local.entity.SoloIdentityEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadIdentity { id ->
            identity = id
            if (id != null) {
                qrBitmap = viewModel.generateQrBitmap(id, size)
            }
        }
    }

    if (qrBitmap != null && identity != null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                bitmap = qrBitmap!!.asImageBitmap(),
                contentDescription = "Your Solo ID QR code",
                modifier = Modifier.size(size.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(identity!!.soloId, style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Ask a friend to scan this code",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Text("Loading your identity...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
