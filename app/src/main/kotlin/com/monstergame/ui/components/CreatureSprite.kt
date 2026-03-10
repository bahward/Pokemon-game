package com.monstergame.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.monstergame.engine.model.CreatureInstance
import com.monstergame.engine.model.ElementType
import com.monstergame.ui.components.typeColor

/**
 * Procedurally-generated placeholder sprite.
 * Each creature gets a deterministic geometric shape based on its species ID.
 * Replace this with actual sprite assets when available.
 */
@Composable
fun CreatureSprite(
    creature: CreatureInstance,
    primaryType: ElementType,
    size: Dp = 80.dp,
    isOpponent: Boolean = false,
    modifier: Modifier = Modifier
) {
    val primaryColor = typeColor(primaryType)
    val seed = creature.speciesId

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = minOf(w, h) * 0.38f

        // Shape variant based on creature ID
        val variant = seed % 6
        val bodyColor = primaryColor
        val outlineColor = primaryColor.copy(alpha = 0.6f)
        val accentColor = Color(
            red   = (primaryColor.red   * 0.6f + 0.4f).coerceIn(0f, 1f),
            green = (primaryColor.green * 0.6f + 0.4f).coerceIn(0f, 1f),
            blue  = (primaryColor.blue  * 0.6f + 0.4f).coerceIn(0f, 1f),
            alpha = 1f
        )

        when (variant) {
            0 -> { // Round body
                drawCircle(color = bodyColor, radius = r, center = Offset(cx, cy + r * 0.1f))
                drawCircle(color = outlineColor, radius = r, center = Offset(cx, cy + r * 0.1f), style = Stroke(3f))
                // "Head"
                drawCircle(color = accentColor, radius = r * 0.45f, center = Offset(cx, cy - r * 0.55f))
                // Eyes
                drawCircle(color = Color.White, radius = r * 0.1f, center = Offset(cx - r * 0.15f, cy - r * 0.6f))
                drawCircle(color = Color.White, radius = r * 0.1f, center = Offset(cx + r * 0.15f, cy - r * 0.6f))
                drawCircle(color = Color.Black, radius = r * 0.05f, center = Offset(cx - r * 0.13f, cy - r * 0.58f))
                drawCircle(color = Color.Black, radius = r * 0.05f, center = Offset(cx + r * 0.13f, cy - r * 0.58f))
            }
            1 -> { // Quadruped
                // Body
                drawOval(color = bodyColor, topLeft = Offset(cx - r * 0.9f, cy - r * 0.4f),
                    size = androidx.compose.ui.geometry.Size(r * 1.8f, r * 0.9f))
                // Head
                drawCircle(color = accentColor, radius = r * 0.42f, center = Offset(cx + r * 0.8f, cy - r * 0.3f))
                // Legs
                repeat(4) { i ->
                    val lx = cx - r * 0.65f + (i % 2) * r * 1.3f
                    val ly = cy + r * 0.4f
                    drawRect(color = bodyColor.copy(alpha = 0.9f),
                        topLeft = Offset(lx, ly),
                        size = androidx.compose.ui.geometry.Size(r * 0.22f, r * 0.5f))
                }
                // Eyes
                drawCircle(color = Color.White, radius = r * 0.09f, center = Offset(cx + r * 1.05f, cy - r * 0.35f))
                drawCircle(color = Color.Black, radius = r * 0.05f, center = Offset(cx + r * 1.05f, cy - r * 0.35f))
            }
            2 -> { // Bird/Flying shape
                // Wing
                val wingPath = Path().apply {
                    moveTo(cx, cy)
                    lineTo(cx - r * 1.2f, cy - r * 0.3f)
                    lineTo(cx - r * 0.8f, cy + r * 0.3f)
                    close()
                }
                drawPath(wingPath, bodyColor, style = Fill)
                val wing2 = Path().apply {
                    moveTo(cx, cy)
                    lineTo(cx + r * 1.2f, cy - r * 0.3f)
                    lineTo(cx + r * 0.8f, cy + r * 0.3f)
                    close()
                }
                drawPath(wing2, accentColor, style = Fill)
                // Body
                drawCircle(color = bodyColor, radius = r * 0.35f, center = Offset(cx, cy))
                // Head
                drawCircle(color = accentColor, radius = r * 0.28f, center = Offset(cx, cy - r * 0.55f))
                drawCircle(color = Color.White, radius = r * 0.08f, center = Offset(cx + r * 0.1f, cy - r * 0.6f))
                drawCircle(color = Color.Black, radius = r * 0.04f, center = Offset(cx + r * 0.1f, cy - r * 0.6f))
            }
            3 -> { // Serpent/dragon
                // Coiled body
                repeat(3) { i ->
                    drawCircle(
                        color = bodyColor.copy(alpha = 0.6f + i * 0.15f),
                        radius = r * (0.6f - i * 0.12f),
                        center = Offset(cx + (i - 1) * r * 0.2f, cy + (i - 1) * r * 0.15f)
                    )
                }
                // Head
                drawCircle(color = accentColor, radius = r * 0.38f, center = Offset(cx + r * 0.3f, cy - r * 0.6f))
                drawCircle(color = Color.White, radius = r * 0.08f, center = Offset(cx + r * 0.4f, cy - r * 0.65f))
                drawCircle(color = Color.Red, radius = r * 0.04f, center = Offset(cx + r * 0.4f, cy - r * 0.65f))
            }
            4 -> { // Humanoid
                // Torso
                drawRect(color = bodyColor,
                    topLeft = Offset(cx - r * 0.35f, cy - r * 0.3f),
                    size = androidx.compose.ui.geometry.Size(r * 0.7f, r * 0.7f))
                // Head
                drawCircle(color = accentColor, radius = r * 0.32f, center = Offset(cx, cy - r * 0.55f))
                // Arms
                drawRect(color = bodyColor.copy(alpha = 0.8f),
                    topLeft = Offset(cx - r * 0.7f, cy - r * 0.2f),
                    size = androidx.compose.ui.geometry.Size(r * 0.28f, r * 0.55f))
                drawRect(color = bodyColor.copy(alpha = 0.8f),
                    topLeft = Offset(cx + r * 0.42f, cy - r * 0.2f),
                    size = androidx.compose.ui.geometry.Size(r * 0.28f, r * 0.55f))
                // Legs
                drawRect(color = bodyColor.copy(alpha = 0.9f),
                    topLeft = Offset(cx - r * 0.32f, cy + r * 0.4f),
                    size = androidx.compose.ui.geometry.Size(r * 0.27f, r * 0.5f))
                drawRect(color = bodyColor.copy(alpha = 0.9f),
                    topLeft = Offset(cx + r * 0.05f, cy + r * 0.4f),
                    size = androidx.compose.ui.geometry.Size(r * 0.27f, r * 0.5f))
                // Eyes
                drawCircle(color = Color.White, radius = r * 0.08f, center = Offset(cx - r * 0.12f, cy - r * 0.57f))
                drawCircle(color = Color.White, radius = r * 0.08f, center = Offset(cx + r * 0.12f, cy - r * 0.57f))
                drawCircle(color = Color.Black, radius = r * 0.04f, center = Offset(cx - r * 0.11f, cy - r * 0.57f))
                drawCircle(color = Color.Black, radius = r * 0.04f, center = Offset(cx + r * 0.11f, cy - r * 0.57f))
            }
            else -> { // Crystal/geometric
                val path = Path().apply {
                    val pts = 6
                    for (i in 0 until pts) {
                        val angle = Math.PI * 2 * i / pts - Math.PI / 2
                        val x = cx + r * Math.cos(angle).toFloat()
                        val y = cy + r * Math.sin(angle).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(path, bodyColor.copy(alpha = 0.8f))
                drawPath(path, outlineColor, style = Stroke(3f))
                // Inner crystal
                val innerPath = Path().apply {
                    val pts = 6
                    for (i in 0 until pts) {
                        val angle = Math.PI * 2 * i / pts + Math.PI / 2
                        val x = cx + r * 0.5f * Math.cos(angle).toFloat()
                        val y = cy + r * 0.5f * Math.sin(angle).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(innerPath, accentColor)
            }
        }

        // Status indicator dot
    }
}
