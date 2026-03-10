package com.monstergame.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monstergame.engine.model.ElementType
import com.monstergame.ui.theme.*

@Composable
fun TypeChip(type: ElementType, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(typeColor(type))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = type.name.take(3).uppercase(),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun typeColor(type: ElementType): Color = when (type) {
    ElementType.NEUTRAL  -> TypeNeutral
    ElementType.FLAME    -> TypeFlame
    ElementType.AQUA     -> TypeAqua
    ElementType.VOLT     -> TypeVolt
    ElementType.FLORA    -> TypeFlora
    ElementType.FROST    -> TypeFrost
    ElementType.BRAWL    -> TypeBrawl
    ElementType.VENOM    -> TypeVenom
    ElementType.TERRA    -> TypeTerra
    ElementType.AERO     -> TypeAero
    ElementType.PSYCHE   -> TypePsyche
    ElementType.INSECT   -> TypeInsect
    ElementType.STONE    -> TypeStone
    ElementType.SHADE    -> TypeShade
    ElementType.WYRM     -> TypeWyrm
    ElementType.MURK     -> TypeMurk
    ElementType.IRON     -> TypeIron
    ElementType.RADIANT  -> TypeRadiant
}
