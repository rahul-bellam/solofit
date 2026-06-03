package com.solofit.app.ui.photos

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.core.BitmapUtils
import com.solofit.app.core.DateUtils
import com.solofit.app.data.local.entity.ProgressPhotoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressPhotosScreen(
    onBack: () -> Unit,
    viewModel: ProgressPhotosViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pose by remember { mutableStateOf(Pose.FRONT) }

    val captureUri = remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = captureUri.value
        if (success && uri != null) {
            val bmp = try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (_: Exception) { null }
            if (bmp != null) viewModel.save(bmp, pose)
        }
    }

    val launchCamera: () -> Unit = {
        try {
            val file = File(context.cacheDir, "camera").apply { mkdirs() }
            val photo = File(file, "progress_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photo
            )
            captureUri.value = uri
            cameraLauncher.launch(uri)
        } catch (_: Exception) { }
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) launchCamera() }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Decode the picked image (downsampled) then save to private storage.
            val bmp = BitmapUtils.decodeSampled(context.contentResolver, uri, maxEdge = 1080)
            if (bmp != null) viewModel.save(bmp, pose)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Photos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            TabRow(selectedTabIndex = pose.ordinal) {
                Pose.entries.forEach { p ->
                    Tab(
                        selected = p == pose,
                        onClick = { pose = p },
                        text = { Text(p.display) }
                    )
                }
            }

            Column(Modifier.padding(16.dp)) {
                // Consistency guidance — the key to useful progress photos.
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        "📸 For comparable photos: same spot, same lighting, same time of day " +
                            "(morning is best), same distance, relaxed pose.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { permLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PhotoCamera, null)
                        Text("  Camera")
                    }
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, null)
                        Text("  Gallery")
                    }
                }

                val photos = state.forPose(pose)
                Spacer(Modifier.height(16.dp))

                if (photos.isEmpty()) {
                    Text(
                        "No ${pose.display.lowercase()} photos yet. Add your first to start a baseline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // ---- Side-by-side compare: first vs latest ----
                    if (photos.size >= 2) {
                        Text("Then vs Now", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ComparePane("First", photos.first(), viewModel, Modifier.weight(1f))
                            ComparePane("Latest", photos.last(), viewModel, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    Text("Timeline", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(photos.reversed(), key = { it.id }) { photo ->
                            Column {
                                PhotoThumb(photo, viewModel, Modifier.height(180.dp).aspectRatio(0.75f))
                                Text(
                                    DateUtils.prettyMedium(photo.date),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = { viewModel.delete(photo.id) },
                                    modifier = Modifier.height(32.dp)
                                ) { Text("Delete", style = MaterialTheme.typography.labelSmall) }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ComparePane(
    label: String,
    photo: ProgressPhotoEntity,
    viewModel: ProgressPhotosViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        PhotoThumb(photo, viewModel, Modifier.fillMaxWidth().aspectRatio(0.75f))
        Text(
            DateUtils.prettyMedium(photo.date),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PhotoThumb(
    photo: ProgressPhotoEntity,
    viewModel: ProgressPhotosViewModel,
    modifier: Modifier = Modifier
) {
    // Decode the private file off the main thread, downsampled.
    val bitmap by produceState<Bitmap?>(initialValue = null, photo.fileName) {
        value = withContext(Dispatchers.IO) {
            val f = viewModel.fileFor(photo.fileName)
            if (!f.exists()) null
            else {
                val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                runCatching { BitmapFactory.decodeFile(f.absolutePath, opts) }.getOrNull()
            }
        }
    }
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        val bmp = bitmap
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Progress photo from ${photo.date}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text("…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

