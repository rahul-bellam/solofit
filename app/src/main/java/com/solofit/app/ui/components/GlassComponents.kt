package com.solofit.app.ui.components

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

private val glassShape = RoundedCornerShape(16.dp)
private val glassButtonShape = RoundedCornerShape(50)
private val tightLetterSpacing = TextUnit(-0.3f, TextUnitType.Sp)

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accentColor: Color = LocalPageAccent.current.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.then(
            if (Build.VERSION.SDK_INT >= 31) Modifier.clip(glassShape) else Modifier
        ),
        shape = glassShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            content = content
        )
    }
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    accentColor: Color = LocalPageAccent.current.primary,
    shape: RoundedCornerShape = glassShape,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.1f)),
        shadowElevation = 4.dp
    ) {
        content()
    }
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = LocalPageAccent.current.primary,
    text: String = "",
    enabled: Boolean = true
) {
    Box {
        Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            shape = glassButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f),
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            ),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = tightLetterSpacing,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
