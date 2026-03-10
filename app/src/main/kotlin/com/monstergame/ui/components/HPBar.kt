package com.monstergame.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monstergame.engine.model.CreatureInstance
import com.monstergame.ui.theme.*

@Composable
fun HPBar(
    creature: CreatureInstance,
    showNumbers: Boolean = true,
    modifier: Modifier = Modifier
) {
    val percent = creature.hpPercent
    val animatedPercent by animateFloatAsState(
        targetValue = percent,
        animationSpec = tween(durationMillis = 500),
        label = "hp_bar"
    )
    val barColor = when {
        percent > 0.5f -> HpHigh
        percent > 0.2f -> HpMid
        else           -> HpLow
    }

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "HP",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(20.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF3A3A3A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedPercent)
                        .clip(RoundedCornerShape(4.dp))
                        .background(barColor)
                )
            }
        }
        if (showNumbers) {
            Text(
                text = "${creature.currentHp}/${creature.maxHp}",
                color = TextSecondary,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
