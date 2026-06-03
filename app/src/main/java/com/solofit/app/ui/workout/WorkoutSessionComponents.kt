package com.solofit.app.ui.workout

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solofit.app.ui.theme.Amber
import com.solofit.app.ui.theme.Moss
import com.solofit.app.ui.theme.Sage
import com.solofit.app.ui.theme.SageDark

private val glassShape = RoundedCornerShape(28.dp)
private val innerShape = RoundedCornerShape(27.dp)

// ─── Intensity ───

enum class IntensityLevel { HIGH, MED, LOW }

private fun intensityForName(name: String): IntensityLevel = when {
    name.contains("bench", true) || name.contains("dips", true) -> IntensityLevel.HIGH
    name.contains("fly", true) || name.contains("plank", true) -> IntensityLevel.LOW
    else -> IntensityLevel.MED
}

private fun intensityGradient(level: IntensityLevel): Pair<Color, Color> = when (level) {
    IntensityLevel.HIGH -> Moss to SageDark
    IntensityLevel.MED -> Color(0xFFFF8A3D) to Color(0xFFE85D04)
    IntensityLevel.LOW -> Color(0xFFFFB703) to Color(0xFFFF8A3D)
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
        RingState.COMPLETE -> Color(0xFF22C55E)
        RingState.ACTIVE -> Color(0xFFE85D04)
        RingState.REST -> Color(0xFFFFB703)
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
                        .background(Color(0xFF22C55E).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                        color = Color(0xFFA8A29E),
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            RingState.REST -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REST", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Amber)
                    Text("45s", fontSize = 11.sp, color = Color(0xFFA8A29E))
                }
            }

            RingState.UPCOMING -> {
                Text("▶", fontSize = 20.sp, color = Color.White.copy(alpha = 0.3f))
            }
        }
    }
}

// ─── Intensity Bar ───

@Composable
fun IntensityBar(
    level: IntensityLevel,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val (from, to) = intensityGradient(level)
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(brush = Brush.verticalGradient(listOf(from, to)))
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.8f))
                .graphicsLayer { alpha = if (isActive) 1f else 0.6f }
                .align(Alignment.TopCenter)
        )
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
    onStartSet: () -> Unit,
    onCompleteSet: () -> Unit,
    onSkip: () -> Unit,
    onRest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = state == RingState.ACTIVE
    val isComplete = state == RingState.COMPLETE
    val intensity = intensityForName(exerciseName)

    val borderGlow = if (isActive) Sage.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.06f)
    val borderOuter = if (isActive) Sage.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.1f)

    val cardModifier = modifier
        .fillMaxWidth()
        .shadow(
            elevation = if (isActive) 12.dp else 6.dp,
            shape = glassShape
        )
        .clip(glassShape)
        .background(
            if (isActive) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.01f),
            glassShape
        )
        .border(1.dp, borderOuter, glassShape)
        .then(
            if (Build.VERSION.SDK_INT >= 31)
                Modifier.background(Color.Transparent, glassShape)
            else Modifier
        )

    val innerModifier = Modifier
        .fillMaxWidth()
        .clip(innerShape)
        .background(Color(0xFF141414))
        .border(1.dp, borderGlow, innerShape)

    Column(modifier = cardModifier) {
        Column(modifier = innerModifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IntensityBar(
                    level = intensity,
                    isActive = isActive,
                    modifier = Modifier.align(Alignment.Top)
                )

                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TagChip(
                            text = "$sets • $reps",
                            textColor = Color(0xFFA8A29E),
                            bgColor = Color.White.copy(alpha = 0.05f),
                            borderColor = Color.White.copy(alpha = 0.1f)
                        )
                        when {
                            isActive -> TagChip(
                                text = "LIVE",
                                textColor = Color(0xFFFF8A3D),
                                bgColor = Amber.copy(alpha = 0.15f),
                                borderColor = Amber.copy(alpha = 0.3f),
                                dotColor = Amber
                            )
                            isComplete -> TagChip(
                                text = "DONE",
                                textColor = Color(0xFF22C55E),
                                bgColor = Color(0xFF22C55E).copy(alpha = 0.15f),
                                borderColor = Color(0xFF22C55E).copy(alpha = 0.3f)
                            )
                            state == RingState.REST -> TagChip(
                                text = "REST",
                                textColor = Amber,
                                bgColor = Amber.copy(alpha = 0.15f),
                                borderColor = Amber.copy(alpha = 0.3f)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        exerciseName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )

                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("⏱", fontSize = 14.sp)
                            Text("${restTime} rest", fontSize = 13.sp, color = Color(0xFFA8A29E))
                        }
                        Box(Modifier.size(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("⚡", fontSize = 14.sp)
                            Text("$kcal kcal", fontSize = 13.sp, color = Color(0xFFA8A29E))
                        }
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
                                            Brush.horizontalGradient(listOf(Moss, SageDark))
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
                                    gradient = Moss to SageDark,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        isComplete -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "✓",
                                    color = Color(0xFF22C55E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    "Completed",
                                    color = Color(0xFF22C55E),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        state == RingState.REST -> {
                            Row(
                                Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Amber.copy(alpha = 0.05f))
                                    .border(1.dp, Amber.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(Modifier.size(8.dp).clip(CircleShape).background(Amber))
                                    Text(
                                        "Recovering",
                                        color = Amber,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    "00:45",
                                    color = Amber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                            }
                        }

                        else -> {
                            SessionActionButton(
                                text = "Start Set",
                                onClick = onStartSet,
                                gradient = Moss to SageDark,
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
            color = if (gradient != null) Color.White else Color(0xFFF5F1EB),
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
    modifier: Modifier = Modifier
) {
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
                    color = Color(0xFFA8A29E)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.3).sp
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(brush = Brush.verticalGradient(listOf(Moss, SageDark))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔥", fontSize = 18.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Streak",
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        color = Color(0xFFA8A29E)
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
                "Session Progress",
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = Color(0xFFA8A29E)
            )
            Text(
                "$completed / $total COMPLETE",
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
                        Brush.horizontalGradient(listOf(Moss, SageDark, Amber))
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
            .background(Color(0xFF171717).copy(alpha = 0.9f))
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
            Brush.verticalGradient(listOf(Moss, SageDark)),
            RoundedCornerShape(16.dp)
        )
    } else {
        Modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
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
            color = if (isPrimary) Color.White else Color(0xFFF5F1EB),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

// ─── Up Next Card ───

@Composable
fun UpNextCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(glassShape)
            .background(Color.White.copy(alpha = 0.01f), glassShape)
            .border(1.dp, Color.White.copy(alpha = 0.1f), glassShape)
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
                    color = Color(0xFFA8A29E)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "COOLDOWN & STRETCH",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "5 min • Lower heart rate",
                    fontSize = 14.sp,
                    color = Color(0xFFA8A29E)
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
                Text("→", color = Color(0xFFF5F1EB), fontSize = 22.sp)
            }
        }
    }
}
