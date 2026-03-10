package com.monstergame.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.monstergame.engine.model.Direction
import com.monstergame.engine.model.Position
import com.monstergame.engine.overworld.TileMap
import com.monstergame.engine.overworld.TileType

private const val TILE_PX = 32  // pixels per tile

/**
 * Renders a top-down tile map on a Canvas.
 * Viewport is centred on the player.
 */
@Composable
fun MapRenderer(
    tileMap: TileMap,
    playerPos: Position,
    playerDirection: Direction,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.background(Color(0xFF1A3B1A))) {
        val canvasW = size.width
        val canvasH = size.height
        val tileSz = TILE_PX.dp.toPx()

        val viewTilesX = (canvasW / tileSz).toInt() + 2
        val viewTilesY = (canvasH / tileSz).toInt() + 2

        val startCol = playerPos.x - viewTilesX / 2
        val startRow = playerPos.y - viewTilesY / 2

        for (row in 0..viewTilesY) {
            for (col in 0..viewTilesX) {
                val mapCol = startCol + col
                val mapRow = startRow + row
                val tileType = tileMap.tiles.getOrNull(mapRow)?.getOrNull(mapCol)
                    ?: TileType.WALL

                val screenX = col * tileSz - (playerPos.x - startCol) * tileSz + canvasW / 2 - tileSz / 2
                val screenY = row * tileSz - (playerPos.y - startRow) * tileSz + canvasH / 2 - tileSz / 2

                drawTile(tileType, screenX, screenY, tileSz)
            }
        }

        // Draw player sprite (simple arrow showing direction)
        val px = canvasW / 2f - tileSz / 2
        val py = canvasH / 2f - tileSz / 2
        drawPlayerSprite(playerDirection, px, py, tileSz)
    }
}

private fun DrawScope.drawTile(type: TileType, x: Float, y: Float, size: Float) {
    val color = tileColor(type)
    drawRect(color, topLeft = Offset(x, y), size = Size(size, size))

    // Tile decorations
    when (type) {
        TileType.TALL_GRASS -> {
            drawRect(Color(0xFF2D5A2D), topLeft = Offset(x + 2, y + 2), size = Size(size - 4, size - 4))
            // Grass blades
            for (i in 0..2) {
                drawLine(Color(0xFF4A8A3A),
                    start = Offset(x + 6f + i * 8, y + size - 4),
                    end   = Offset(x + 4f + i * 8, y + 4),
                    strokeWidth = 2f)
            }
        }
        TileType.WATER -> {
            drawRect(Color(0xFF1565C0), topLeft = Offset(x, y), size = Size(size, size))
            drawLine(Color(0xFF1976D2).copy(alpha = 0.5f),
                start = Offset(x + 4, y + size / 3),
                end   = Offset(x + size - 4, y + size / 3),
                strokeWidth = 2f)
        }
        TileType.WALL -> {
            drawRect(Color(0xFF424242), topLeft = Offset(x + 1, y + 1), size = Size(size - 2, size - 2))
        }
        TileType.DOOR -> {
            drawRect(Color(0xFF795548), topLeft = Offset(x + size * 0.2f, y),
                size = Size(size * 0.6f, size * 0.8f))
        }
        else -> {}
    }
}

private fun tileColor(type: TileType): Color = when (type) {
    TileType.GROUND      -> Color(0xFF8BC34A)
    TileType.WALL        -> Color(0xFF546E7A)
    TileType.WATER       -> Color(0xFF1565C0)
    TileType.GRASS       -> Color(0xFF558B2F)
    TileType.TALL_GRASS  -> Color(0xFF2E7D32)
    TileType.DOOR        -> Color(0xFF6D4C41)
    TileType.WARP        -> Color(0xFF4A148C)
    TileType.LEDGE_DOWN  -> Color(0xFF795548)
    TileType.LEDGE_LEFT  -> Color(0xFF795548)
    TileType.LEDGE_RIGHT -> Color(0xFF795548)
    TileType.NPC         -> Color(0xFF8BC34A)
    TileType.ITEM_BALL   -> Color(0xFF8BC34A)
}

private fun DrawScope.drawPlayerSprite(direction: Direction, x: Float, y: Float, size: Float) {
    val cx = x + size / 2
    val cy = y + size / 2
    // Body
    drawCircle(Color(0xFF2196F3), radius = size * 0.28f, center = Offset(cx, cy + size * 0.05f))
    // Head
    drawCircle(Color(0xFFFFCC80), radius = size * 0.2f, center = Offset(cx, cy - size * 0.22f))
    // Direction indicator
    val (dx, dy) = when (direction) {
        Direction.UP    -> 0f to -1f
        Direction.DOWN  -> 0f to 1f
        Direction.LEFT  -> -1f to 0f
        Direction.RIGHT -> 1f to 0f
    }
    drawCircle(
        Color(0xFFFF5722),
        radius = size * 0.08f,
        center = Offset(cx + dx * size * 0.22f, cy - size * 0.22f + dy * size * 0.18f)
    )
}

/** Generates a minimal placeholder tile map when no external map is loaded. */
fun generatePlaceholderMap(areaId: String, width: Int = 20, height: Int = 20): TileMap {
    val rows = List(height) { row ->
        List(width) { col ->
            when {
                row == 0 || row == height - 1 || col == 0 || col == width - 1 -> TileType.WALL
                row in 5..7 && col in 5..14 -> TileType.TALL_GRASS
                row == 9 && col == width / 2 -> TileType.DOOR
                else -> TileType.GROUND
            }
        }
    }
    return TileMap(areaId = areaId, width = width, height = height, tiles = rows)
}
