package com.solofit.app.ui.workout

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solofit.app.ui.theme.WorkoutAccent
import com.solofit.app.ui.theme.HighGreen
import com.solofit.app.ui.theme.LowRed
import com.solofit.app.ui.theme.MidAmber
import kotlinx.coroutines.delay

// ─── Intensity ───

enum class IntensityLevel { HIGH, MED, LOW }

private fun intensityForName(name: String): IntensityLevel = when {
    name.contains("bench", true) || name.contains("dips", true) -> IntensityLevel.HIGH
    name.contains("fly", true) || name.contains("plank", true) -> IntensityLevel.LOW
    else -> IntensityLevel.MED
}

// ─── Progress Ring ───

enum class RingState { COMPLETE, ACTIVE, REST, UPCOMING }

@Composable
fun ProgressRing(
    progress: Float,
    state: RingState,
    size: Int = 64,
    modifier: Modifier = Modifier
) {
    val r = 26f
    val stroke = 6f
    val c = 2f * Math.PI.toFloat() * r
    val offset by animateFloatAsState(
        targetValue = c - (progress / 100f) * c,
        animationSpec = tween(600),
        label = "ringOffset"
    )

    val color = when (state) {
        RingState.COMPLETE -> HighGreen
        RingState.ACTIVE -> WorkoutAccent
        RingState.REST -> MidAmber
        RingState.UPCOMING -> Color.White.copy(alpha = 0.2f)
    }

    Box(modifier = modifier.size(size.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size.dp)) {
            val strokePx = stroke.dp.toPx()
            val arcSize = size.dp.toPx() - strokePx
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                style = Stroke(strokePx)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * (progress / 100f),
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                style = Stroke(strokePx, cap = StrokeCap.Round)
            )
        }

        when (state) {
            RingState.COMPLETE -> {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(HighGreen.copy(alpha = 0.15f))
                        .border(1.dp, HighGreen.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = HighGreen, modifier = Modifier.size(20.dp))
                }
            }

            RingState.ACTIVE -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${progress.toInt()}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                    Text(
                        "LIVE",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            RingState.REST -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REST", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WorkoutAccent)
                    Text("45s", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            RingState.UPCOMING -> {
                Text("▶", fontSize = 20.sp, color = Color.White.copy(alpha = 0.3f))
            }
        }
    }
}

// ─── Tag Chip ───

@Composable
private fun TagChip(
    text: String,
    textColor: Color,
    bgColor: Color,
    borderColor: Color,
    dotColor: Color? = null
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (dotColor != null) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(dotColor))
        }
        Text(
            text,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── Exercise Card ───

@Composable
fun WorkoutExerciseCard(
    exerciseName: String,
    muscleGroup: String,
    sets: String,
    reps: String,
    restTime: String,
    kcal: Int,
    progress: Float,
    state: RingState,
    restSecondsRemaining: Int = 0,
    onStartSet: () -> Unit,
    onCompleteSet: () -> Unit,
    onSkip: () -> Unit,
    onRest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = state == RingState.ACTIVE
    val isComplete = state == RingState.COMPLETE
    val lowAccent = MaterialTheme.colorScheme.onSurfaceVariant

    val cardModifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(MaterialTheme.colorScheme.surface)
        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
        .drawBehind {
            val accentColor = when (intensityForName(exerciseName)) {
                IntensityLevel.HIGH -> LowRed
                IntensityLevel.MED -> WorkoutAccent
                IntensityLevel.LOW -> lowAccent
            }
            drawRect(
                color = if (isActive) accentColor else accentColor.copy(alpha = 0.3f),
                topLeft = Offset.Zero,
                size = Size(3.dp.toPx(), size.height)
            )
        }

    Column(modifier = cardModifier.padding(start = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TagChip(
                            text = "$sets • $reps",
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            bgColor = Color.White.copy(alpha = 0.05f),
                            borderColor = Color.White.copy(alpha = 0.1f)
                        )
                        when {
                            isActive -> {
                                val pulse by rememberInfiniteTransition(label = "livePulse").animateFloat(
                                    initialValue = 0.5f, targetValue = 1f,
                                    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                                    label = "livePulse"
                                )
                                TagChip(
                                    text = "Active",
                                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    bgColor = WorkoutAccent.copy(alpha = 0.15f),
                                    borderColor = WorkoutAccent.copy(alpha = 0.3f),
                                    dotColor = WorkoutAccent.copy(alpha = pulse)
                                )
                            }
                            isComplete -> TagChip(
                                text = "Done",
                                textColor = HighGreen,
                                bgColor = HighGreen.copy(alpha = 0.15f),
                                borderColor = HighGreen.copy(alpha = 0.3f)
                            )
                            state == RingState.REST -> TagChip(
                                text = "Rest",
                                textColor = WorkoutAccent,
                                bgColor = WorkoutAccent.copy(alpha = 0.15f),
                                borderColor = WorkoutAccent.copy(alpha = 0.3f)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        exerciseName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("${restTime} rest", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Box(Modifier.size(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)))
                        Text("$kcal kcal", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(Modifier.height(16.dp))

                    when {
                        isActive -> {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(progress / 100f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            Brush.horizontalGradient(listOf(WorkoutAccent, WorkoutAccent))
                                        )
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SessionActionButton(
                                    text = "Skip",
                                    onClick = onSkip,
                                    modifier = Modifier.weight(1f)
                                )
                                SessionActionButton(
                                    text = "Done",
                                    onClick = onCompleteSet,
                                    gradient = WorkoutAccent to WorkoutAccent,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        isComplete -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = HighGreen, modifier = Modifier.size(18.dp))
                                Text(
                                    "Completed",
                                    color = HighGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        state == RingState.REST -> {
                            val restDisplay = if (restSecondsRemaining > 0) {
                                val m = restSecondsRemaining / 60
                                val s = restSecondsRemaining % 60
                                "%d:%02d".format(m, s)
                            } else "REST"
                            Row(
                                Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(WorkoutAccent.copy(alpha = 0.05f))
                                    .border(1.dp, WorkoutAccent.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                    .clickable(onClick = onRest)
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(Modifier.size(8.dp).clip(CircleShape).background(WorkoutAccent))
                                    Text(
                                        "Recovering",
                                        color = WorkoutAccent,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    restDisplay,
                                    color = WorkoutAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                            }
                        }

                        else -> {
                            SessionActionButton(
                                text = "Start Set",
                                onClick = onStartSet,
                                gradient = WorkoutAccent to WorkoutAccent,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(Modifier.width(4.dp))
                ProgressRing(
                    progress = progress,
                    state = state
                )
            }
        }
    }
}

@Composable
private fun SessionActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Pair<Color, Color>? = null
) {
    val bgMod = if (gradient != null) {
        Modifier.background(
            Brush.horizontalGradient(listOf(gradient.first, gradient.second)),
            RoundedCornerShape(16.dp)
        )
    } else {
        Modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(bgMod)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (gradient != null) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (gradient != null) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

// ─── Session Header ───

@Composable
fun SessionHeader(
    subtitle: String,
    title: String,
    streak: Int,
    startedAt: Long = 0L,
    modifier: Modifier = Modifier
) {
    val elapsed by produceState(0L, startedAt) {
        if (startedAt > 0L) {
            while (true) {
                value = System.currentTimeMillis() - startedAt
                delay(1000L)
            }
        }
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.3).sp
                )
                Spacer(Modifier.height(2.dp))
                Text(formatElapsed((elapsed / 1000).toInt()), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, letterSpacing = 0.5.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(WorkoutAccent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = WorkoutAccent, modifier = Modifier.size(20.dp))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Streak",
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$streak",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ─── Session Progress ───

@Composable
fun SessionProgressBar(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val fraction = if (total > 0) completed.toFloat() / total else 0f
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$completed / $total",
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(listOf(WorkoutAccent, WorkoutAccent, WorkoutAccent))
                    )
            )
        }
    }
}

// ─── Bottom Action Bar ───

@Composable
fun BottomActionBar(
    isPaused: Boolean,
    onPause: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.9f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val pauseLabel = if (isPaused) "Resume" else "Pause"
        PauseFinishButton(
            text = pauseLabel,
            onClick = onPause,
            modifier = Modifier.weight(1f),
            isPrimary = false
        )
        PauseFinishButton(
            text = "Finish",
            onClick = onFinish,
            modifier = Modifier.weight(1f),
            isPrimary = true
        )
    }
}

@Composable
private fun PauseFinishButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean
) {
    val bgMod = if (isPrimary) {
        Modifier.background(
            Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.onSurface)),
            RoundedCornerShape(16.dp)
        )
    } else {
        Modifier.border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
    }

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(bgMod)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (isPrimary) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

// ─── Up Next Card ───

@Composable
fun UpNextCard(
    nextExerciseName: String? = null,
    modifier: Modifier = Modifier
) {
    val name = nextExerciseName ?: return
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.01f), RoundedCornerShape(28.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Up Next",
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
            }
        }
    }
}

private fun formatElapsed(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
