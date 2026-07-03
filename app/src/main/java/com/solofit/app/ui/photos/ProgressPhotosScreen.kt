package com.solofit.app.ui.photos

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.HapticFeedbackConstants
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.core.BitmapUtils
import com.solofit.app.core.DateUtils
import com.solofit.app.data.local.entity.ProgressPhotoEntity
import com.solofit.app.ui.components.BodyTheme
import com.solofit.app.ui.theme.SlateBlue
import com.solofit.app.ui.theme.TextPrimary
import com.solofit.app.ui.theme.TextSecondary
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ProgressPhotosScreen(
    onBack: () -> Unit,
    viewModel: ProgressPhotosViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pose by remember { mutableStateOf(Pose.FRONT) }
    val scope = rememberCoroutineScope()
    var blurred by remember { mutableStateOf(true) }

    val captureUri = remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = captureUri.value
        if (success && uri != null) {
            scope.launch(Dispatchers.IO) {
                val bmp = try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                } catch (_: Exception) { null }
                if (bmp != null) {
                    // save() takes ownership of the bitmap and recycles it after
                    // compressing — recycling here would race that background write.
                    withContext(Dispatchers.Main) {
                        viewModel.save(bmp, pose)
                    }
                }
            }
        }
    }

    val launchCamera: () -> Unit = {
        scope.launch(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, "camera").apply { mkdirs() }
                val photo = File(file, "progress_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photo
                )
                withContext(Dispatchers.Main) {
                    captureUri.value = uri
                    cameraLauncher.launch(uri)
                }
            } catch (_: Exception) { /* Camera unavailable */ }
        }
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) launchCamera() }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val bmp = BitmapUtils.decodeSampled(context.contentResolver, uri, maxEdge = 1080)
                if (bmp != null) {
                    // save() owns and recycles the bitmap; don't recycle here.
                    withContext(Dispatchers.Main) {
                        viewModel.save(bmp, pose)
                    }
                }
            }
        }
    }

    BodyTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                    }
                    Text("Progress Photos", style = MaterialTheme.typography.titleLarge, color = PrimaryText)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { blurred = !blurred }) {
                        Icon(
                            if (blurred) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (blurred) "Reveal photos" else "Blur photos",
                            tint = SlateBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            if (blurred) "Reveal" else "Blur",
                            color = SlateBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

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
                    // Consistency guidance
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp)
                    ) {
                        Text(
                            "For comparable photos: same spot, same lighting, same time of day (morning is best), same distance, relaxed pose.",
                            fontSize = 13.sp,
                            color = SecondaryText
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
                            fontSize = 14.sp,
                            color = SecondaryText
                        )
                    } else {
                        if (photos.size >= 2) {
                            Text("Then vs Now", style = MaterialTheme.typography.titleMedium, color = PrimaryText)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                ComparePane("First", photos.first(), viewModel, blurred, Modifier.weight(1f))
                                ComparePane("Latest", photos.last(), viewModel, blurred, Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(20.dp))
                        }

                        Text("Timeline", style = MaterialTheme.typography.titleMedium, color = PrimaryText)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(photos.reversed(), key = { it.id }) { photo ->
                                Column {
                                    PhotoThumb(photo, viewModel, blurred, Modifier.height(180.dp).aspectRatio(0.75f))
                                    Text(
                                        DateUtils.prettyMedium(photo.date),
                                        fontSize = 11.sp,
                                        color = SecondaryText
                                    )
                                    OutlinedButton(
                                        onClick = { viewModel.delete(photo.id) },
                                        modifier = Modifier.height(32.dp)
                                    ) { Text("Delete", fontSize = 11.sp) }
                                }
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
    blurred: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(label, fontWeight = FontWeight.SemiBold, color = PrimaryText, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        PhotoThumb(photo, viewModel, blurred, Modifier.fillMaxWidth().aspectRatio(0.75f))
        Text(
            DateUtils.prettyMedium(photo.date),
            fontSize = 11.sp,
            color = SecondaryText
        )
    }
}

@Composable
private fun PhotoThumb(
    photo: ProgressPhotoEntity,
    viewModel: ProgressPhotosViewModel,
    blurred: Boolean,
    modifier: Modifier = Modifier
) {
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
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { bitmap?.recycle() }
    }
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .then(if (blurred) Modifier.blur(24.dp) else Modifier),
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
            Text("\u2026", color = SecondaryText)
        }
    }
}
