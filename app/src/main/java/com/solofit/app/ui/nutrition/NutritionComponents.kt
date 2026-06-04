package com.solofit.app.ui.nutrition

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solofit.app.ui.theme.ProteinColor
import com.solofit.app.ui.theme.CarbsColor
import com.solofit.app.ui.theme.FatsColor

private val green = Color(0xFF5F8E5A)
private val lightGreen = Color(0xFFa3c9a1)
private val muted = Color(0xFF7a786d)
private val darkText = Color(0xFF3f3e36)

// ─── Nutrition Colors (light/dark aware) ───

data class NutritionColors(
    val bg: Color,
    val surface: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val headerBg: Brush,
    val cardBg: Color,
    val iconBg: Color,
    val border: Color,
    val green: Color,
    val greenLight: Color,
    val trackColor: Color
)

fun nutritionColors(isDark: Boolean) = if (isDark) NutritionColors(
    bg = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    textPrimary = Color(0xFFE8E4D9),
    textMuted = Color(0xFF9E9A8E),
    headerBg = Brush.verticalGradient(listOf(Color(0xFF1F2A1F), Color(0xFF162016))),
    cardBg = Color(0xFF2A2A2A),
    iconBg = Color(0xFF333333),
    border = Color(0xFF444444),
    green = Color(0xFF9CC99C),
    greenLight = Color(0xFF2A3A2A),
    trackColor = muted.copy(alpha = 0.2f)
) else NutritionColors(
    bg = Color(0xFFF8F6F1),
    surface = Color.White,
    textPrimary = Color(0xFF3F3E36),
    textMuted = Color(0xFF7A786D),
    headerBg = Brush.verticalGradient(listOf(Color(0xFFF1F7EB), Color(0xFFE8F0E0))),
    cardBg = Color.White,
    iconBg = Color(0xFFE8E4D9),
    border = Color(0xFFD4D1C4),
    green = Color(0xFF5F8E5A),
    greenLight = Color(0xFFF1F7EB),
    trackColor = muted.copy(alpha = 0.2f)
)

// ─── Header ───

@Composable
fun NutritionHeader(
    colors: NutritionColors,
    greeting: String,
    tagline: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.headerBg)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(
            greeting,
            color = colors.textMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            tagline,
            color = colors.textPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── Daily Progress Card ───

@Composable
fun DailyProgressCard(
    colors: NutritionColors,
    progressPercent: Int,
    calories: Int,
    caloriesGoal: Int,
    protein: Int,
    proteinGoal: Int,
    carbs: Int,
    carbsGoal: Int,
    fat: Int,
    fatGoal: Int,
    modifier: Modifier = Modifier
) {
    val progressFraction = calories.toFloat() / caloriesGoal.coerceAtLeast(1)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .offset(y = (-20).dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBg)
                .border(1.dp, colors.border.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular progress
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(Modifier.size(80.dp)) {
                        val stroke = 6.dp.toPx()
                        val arcSize = size.width - stroke
                        val tl = Offset(stroke / 2, stroke / 2)
                        drawArc(
                            color = colors.trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = tl,
                            size = Size(arcSize, arcSize),
                            style = Stroke(stroke)
                        )
                        drawArc(
                            color = colors.green,
                            startAngle = -90f,
                            sweepAngle = 360f * progressFraction,
                            useCenter = false,
                            topLeft = tl,
                            size = Size(arcSize, arcSize),
                            style = Stroke(stroke, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$progressPercent",
                            color = colors.textPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "%",
                            color = colors.textMuted,
                            fontSize = 10.sp,
                            modifier = Modifier.offset(y = (-4).dp)
                        )
                    }
                }

                // Calorie bar + macros
                Column(Modifier.weight(1f)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🔥", fontSize = 12.sp)
                            Text(
                                "CALORIES",
                                color = colors.textMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "$calories",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                " / $caloriesGoal",
                                color = colors.textMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(colors.trackColor)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(colors.green)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MacroChip("Protein", "${protein}g", "/ ${proteinGoal}g", colors, ProteinColor)
                        MacroChip("Carbs", "${carbs}g", "/ ${carbsGoal}g", colors, CarbsColor)
                        MacroChip("Fat", "${fat}g", "/ ${fatGoal}g", colors, FatsColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroChip(label: String, current: String, goal: String, colors: NutritionColors, dotColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(dotColor))
            Text(label, color = colors.textMuted, fontSize = 11.sp)
        }
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(current, color = colors.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(goal, color = colors.green, fontSize = 10.sp)
        }
    }
}

// ─── Quick Add Button ───

@Composable
fun QuickAddButton(
    colors: NutritionColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.greenLight)
            .border(1.dp, colors.green.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("+", color = colors.green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "Add to today's log",
                color = colors.green,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

// ─── Action Card ───

@Composable
fun NutritionActionCard(
    colors: NutritionColors,
    icon: String,
    title: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(colors.cardBg)
            .border(1.dp, colors.border.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (badge != null) {
                Box(
                    Modifier
                        .align(Alignment.End)
                        .offset(x = 4.dp, y = (-4).dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8A17E))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }

            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.greenLight),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 28.sp)
            }

            Spacer(Modifier.height(12.dp))
            Text(title, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                color = colors.textMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

// ─── Section Header ───

@Composable
fun NutritionSectionHeader(
    colors: NutritionColors,
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = colors.textMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
        if (actionLabel != null && onAction != null) {
            Row(
                modifier = Modifier.clickable(onClick = onAction),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(actionLabel, color = colors.green, fontSize = 13.sp)
                Text("›", color = colors.green, fontSize = 14.sp)
            }
        }
    }
}

// ─── Food Log Item ───

@Composable
fun FoodLogItem(
    colors: NutritionColors,
    emoji: String,
    name: String,
    quantity: String,
    time: String,
    calories: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.cardBg)
            .border(1.dp, colors.border.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.greenLight),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 20.sp)
        }

        Column(Modifier.weight(1f)) {
            Text(name, color = colors.textPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(
                "$quantity • $time",
                color = colors.textMuted,
                fontSize = 11.sp
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "$calories",
                color = colors.green,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "kcal",
                color = colors.textMuted.copy(alpha = 0.6f),
                fontSize = 9.sp
            )
        }
    }
}

// ─── Empty State ───

@Composable
fun NutritionEmptyState(
    colors: NutritionColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🍽️", fontSize = 40.sp, modifier = Modifier.alpha(0.3f))
        Spacer(Modifier.height(12.dp))
        Text("No meals logged yet", color = colors.textMuted, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        Text("Tap to log your first meal", color = colors.green, fontSize = 12.sp)
    }
}

// ─── FAB Scanner ───

@Composable
fun ScannerFloatingButton(
    colors: NutritionColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.green)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("📷", fontSize = 24.sp)
    }
}
