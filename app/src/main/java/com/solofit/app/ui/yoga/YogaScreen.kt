package com.solofit.app.ui.yoga

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solofit.app.ui.theme.TextPrimary
import com.solofit.app.ui.theme.TextSecondary

data class YogaSession(val name: String, val durationMin: Int, val description: String, val color: Color)

private val sessions = listOf(
    YogaSession("Sun Salutation", 15, "12 classic poses in a flowing sequence", Color(0xFFFFB347)),
    YogaSession("Morning Flow", 10, "Gentle stretches to start the day", Color(0xFFA8D5BA)),
    YogaSession("Midday Reset", 5, "Quick desk-friendly mobility break", Color(0xFFFFD6A5)),
    YogaSession("Hip Openers", 15, "Release tension built from sitting", Color(0xFFB5D8EB)),
    YogaSession("Full Body Stretch", 20, "Complete flexibility routine", Color(0xFFD4B5E0)),
    YogaSession("Evening Wind Down", 10, "Calming poses before bed", Color(0xFFA8D5BA))
)

data class PoseStep(
    val number: Int,
    val sanskrit: String,
    val english: String,
    val breath: String,
    val instruction: String,
    val benefit: String
)

private val sunSalutationPoses = listOf(
    PoseStep(1, "Pranamasana", "Prayer Pose", "Exhale", "Stand tall, feet together. Bring palms together at the chest.", "Improves focus and prepares the mind for practice."),
    PoseStep(2, "Hasta Uttanasana", "Raised Arms Pose", "Inhale", "Lift arms upward and gently arch back, looking up.", "Expands lungs and stretches the abdomen."),
    PoseStep(3, "Pada Hastasana", "Standing Forward Bend", "Exhale", "Fold forward from the hips, hands beside feet.", "Stretches hamstrings and improves blood flow to the brain."),
    PoseStep(4, "Ashwa Sanchalanasana", "Equestrian Pose", "Inhale", "Step right leg back, knee down, chest open.", "Strengthens legs and opens hip flexors."),
    PoseStep(5, "Parvatasana", "Mountain Pose", "Exhale", "Step left leg back, lift hips into inverted V.", "Strengthens shoulders, arms, and calves."),
    PoseStep(6, "Ashtanga Namaskara", "Eight-Limbed Pose", "Exhale", "Lower knees, chest, and chin to the floor.", "Builds upper-body strength and chest stability."),
    PoseStep(7, "Bhujangasana", "Cobra Pose", "Inhale", "Slide forward and lift the chest gently.", "Improves spinal flexibility and relieves back stiffness."),
    PoseStep(8, "Parvatasana", "Mountain Pose", "Exhale", "Lift hips back into inverted V again.", "Re-energizes the body and improves circulation."),
    PoseStep(9, "Ashwa Sanchalanasana", "Equestrian Pose", "Inhale", "Step right foot forward between hands.", "Tones abdominal organs and improves balance."),
    PoseStep(10, "Pada Hastasana", "Standing Forward Bend", "Exhale", "Fold forward from the hips again.", "Calms the mind and stretches the spine."),
    PoseStep(11, "Hasta Uttanasana", "Raised Arms Pose", "Inhale", "Lift arms upward, arch back gently.", "Opens the chest and lifts the spirit."),
    PoseStep(12, "Pranamasana", "Prayer Pose", "Exhale", "Return to standing, palms together at chest.", "Completes the cycle with gratitude and focus.")
)

@Composable
fun YogaScreen(
    onBack: () -> Unit = {}
) {
    var showSunSalutation by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (showSunSalutation) {
            SunSalutationFlow(onBack = { showSunSalutation = false })
        } else {
            SessionList(onBack = onBack, onSunSalutation = { showSunSalutation = true })
        }
    }
}

@Composable
private fun SessionList(onBack: () -> Unit, onSunSalutation: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
            }
            Text("Yoga & Flexibility", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Quick sessions to improve flexibility and mobility.",
                fontSize = 14.sp, color = TextSecondary, lineHeight = 20.sp
            )
            Spacer(Modifier.height(8.dp))

            sessions.forEachIndexed { i, session ->
                YogaSessionCard(
                    session = session,
                    isHighlighted = i == 0,
                    onClick = if (i == 0) onSunSalutation else ({})
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun YogaSessionCard(session: YogaSession, isHighlighted: Boolean = false, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isHighlighted) session.color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(session.color.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.size(16.dp).clip(CircleShape).background(session.color)
                )
            }

            Column(Modifier.weight(1f)) {
                Text(session.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(session.description, fontSize = 12.sp, color = TextSecondary)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${session.durationMin} min", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun SunSalutationFlow(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
            }
            Text("Sun Salutation", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                "12 poses performed in a flowing sequence. Each movement is synchronized with the breath.",
                fontSize = 14.sp, color = TextSecondary, lineHeight = 20.sp
            )

            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFB347).copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Benefits", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "Full-body strength & toning",
                        "Improved digestion & metabolism",
                        "Enhanced flexibility & spinal mobility",
                        "Mental clarity & stress relief",
                        "Cardiovascular workout in 15 min"
                    ).forEach { b ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(4.dp).clip(CircleShape).background(Color(0xFFFFB347)))
                            Spacer(Modifier.width(8.dp))
                            Text(b, fontSize = 12.sp, color = TextSecondary)
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            sunSalutationPoses.forEach { pose ->
                PoseCard(pose)
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PoseCard(pose: PoseStep) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PoseFigure(pose.number, Modifier.size(56.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${pose.number}.", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(pose.sanskrit, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Text(pose.english, fontSize = 12.sp, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                BreathBadge(pose.breath)
                Spacer(Modifier.height(6.dp))
                Text(pose.instruction, fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp)
                if (pose.benefit.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(pose.benefit, fontSize = 11.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun BreathBadge(breath: String) {
    val color = if (breath == "Inhale") Color(0xFF4CAF50) else Color(0xFF42A5F5)
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(breath, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun PoseFigure(poseNumber: Int, modifier: Modifier = Modifier) {
    val color = Color(0xFFFFB347)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val headR = w * 0.10f
        val neckY = headR * 2.2f
        val hipY = h * 0.72f
        val shoulderY = neckY + (hipY - neckY) * 0.25f
        val cx = w / 2f
        val stroke = w * 0.045f
        val c = color

        fun head() {
            drawCircle(c, headR, Offset(cx, headR + 2f))
        }

        fun spine() {
            drawLine(c, Offset(cx, neckY), Offset(cx, hipY), stroke, StrokeCap.Round)
        }

        fun arm(angleDeg: Float, lengthMul: Float = 0.45f) {
            val angle = Math.toRadians(angleDeg.toDouble()).toFloat()
            val armLen = h * lengthMul
            val endX = cx + armLen * kotlin.math.sin(angle)
            val endY = shoulderY + armLen * kotlin.math.cos(angle)
            drawLine(c, Offset(cx, shoulderY), Offset(endX, endY), stroke, StrokeCap.Round)
        }

        fun armLeft(angleDeg: Float, lengthMul: Float = 0.45f) {
            arm(180f - angleDeg, lengthMul)
        }

        fun armRight(angleDeg: Float, lengthMul: Float = 0.45f) {
            arm(angleDeg, lengthMul)
        }

        fun leg(angleDeg: Float, lengthMul: Float = 0.5f) {
            val angle = Math.toRadians(angleDeg.toDouble()).toFloat()
            val legLen = h * lengthMul
            val endX = cx + legLen * kotlin.math.sin(angle)
            val endY = hipY + legLen * kotlin.math.cos(angle)
            drawLine(c, Offset(cx, hipY), Offset(endX, endY), stroke, StrokeCap.Round)
        }

        fun legLeft(angleDeg: Float, lengthMul: Float = 0.5f) {
            leg(-angleDeg, lengthMul)
        }

        fun legRight(angleDeg: Float, lengthMul: Float = 0.5f) {
            leg(angleDeg, lengthMul)
        }

        when (poseNumber) {
            1 -> { head(); spine(); armLeft(30f); armRight(30f); legLeft(10f); legRight(10f) }
            2 -> { head(); spine(); armLeft(0f, 0.6f); armRight(180f, 0.6f); legLeft(10f); legRight(10f) }
            3 -> { head(); drawLine(c, Offset(cx, neckY), Offset(cx, hipY * 0.6f), stroke, StrokeCap.Round); armLeft(170f, 0.55f); armRight(10f, 0.55f); legLeft(10f); legRight(10f) }
            4 -> { head(); spine(); armLeft(30f); armRight(30f); drawLine(c, Offset(cx, hipY), Offset(cx + w * 0.2f, h * 0.95f), stroke, StrokeCap.Round); drawLine(c, Offset(cx, hipY), Offset(cx - w * 0.15f, h * 0.9f), stroke, StrokeCap.Round) }
            5 -> { head(); drawLine(c, Offset(cx, neckY), Offset(cx - w * 0.1f, hipY), stroke, StrokeCap.Round); armLeft(20f, 0.6f); armRight(160f, 0.6f); legLeft(20f, 0.6f); legRight(160f, 0.6f) }
            6 -> { drawCircle(c, headR * 0.7f, Offset(cx, h * 0.15f)); drawLine(c, Offset(cx, h * 0.22f), Offset(cx, h * 0.45f), stroke, StrokeCap.Round); drawLine(c, Offset(cx - w * 0.2f, h * 0.3f), Offset(cx, h * 0.4f), stroke, StrokeCap.Round); drawLine(c, Offset(cx, h * 0.45f), Offset(cx - w * 0.2f, h * 0.85f), stroke, StrokeCap.Round); drawLine(c, Offset(cx, h * 0.45f), Offset(cx + w * 0.2f, h * 0.85f), stroke, StrokeCap.Round) }
            7 -> { head(); drawLine(c, Offset(cx, neckY), Offset(cx, h * 0.8f), stroke * 1.2f, StrokeCap.Round); armLeft(30f, 0.35f); armRight(150f, 0.35f); legLeft(10f, 0.4f); legRight(10f, 0.4f) }
            8 -> { 5.let { head(); drawLine(c, Offset(cx, neckY), Offset(cx - w * 0.1f, hipY), stroke, StrokeCap.Round); armLeft(20f, 0.6f); armRight(160f, 0.6f); legLeft(20f, 0.6f); legRight(160f, 0.6f) } }
            9 -> { 4.let { head(); spine(); armLeft(30f); armRight(30f); drawLine(c, Offset(cx, hipY), Offset(cx + w * 0.2f, h * 0.95f), stroke, StrokeCap.Round); drawLine(c, Offset(cx, hipY), Offset(cx - w * 0.15f, h * 0.9f), stroke, StrokeCap.Round) } }
            10 -> { 3.let { head(); drawLine(c, Offset(cx, neckY), Offset(cx, hipY * 0.6f), stroke, StrokeCap.Round); armLeft(170f, 0.55f); armRight(10f, 0.55f); legLeft(10f); legRight(10f) } }
            11 -> { 2.let { head(); spine(); armLeft(0f, 0.6f); armRight(180f, 0.6f); legLeft(10f); legRight(10f) } }
            12 -> { 1.let { head(); spine(); armLeft(30f); armRight(30f); legLeft(10f); legRight(10f) } }
        }
    }
}

private object Math {
    fun toRadians(deg: Double): Double = deg * kotlin.math.PI / 180.0
}
