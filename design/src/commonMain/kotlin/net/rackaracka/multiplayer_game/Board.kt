package net.rackaracka.multiplayer_game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import multiplayergame.design.generated.resources.Res
import multiplayergame.design.generated.resources.mine
import multiplayergame.design.generated.resources.submarine
import org.jetbrains.compose.resources.painterResource

interface BoardScope {
    @Composable
    fun Submarine(point: Point)

    @Composable
    fun Mine(point: Point)

    @Composable
    fun NumberedMine(point: Point, mineIndex: Int)

    @Composable
    fun DetonatedMine(point: Point)
}

/*
 * The contents are not perfectly placed at the point.
 */
@Composable
fun Board(
    verticalTilesCount: Int = 10,
    horizontalTilesCount: Int = 10,
    boardScope: @Composable BoardScope.() -> Unit
) {
    val density = LocalDensity.current
    val gridThickness = 2.dp
    Column(modifier = Modifier.aspectRatio(1f)) {
        Row {
            Text("11", modifier = Modifier.alpha(0f))
            // Hacky fix to avoid measure the right side numbers (Will probably do in future)
            repeat(horizontalTilesCount) {
                Text(
                    ('A' + it).toString(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        Row {
            Column {
                repeat(verticalTilesCount) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text((it + 1).toString())
                    }
                }
            }
            var boardWidth by remember { mutableStateOf(0.dp) }
            var boardHeight by remember { mutableStateOf(0.dp) }
            var tileWidth by remember { mutableStateOf(0.dp) }
            var tileHeight by remember { mutableStateOf(0.dp) }

            val tileWidthPx = remember(tileWidth) { with(density) { tileWidth.toPx() } }
            val tileHeightPx = remember(tileHeight) { with(density) { tileHeight.toPx() } }
            val gridThicknessPx = remember(gridThickness) { with(density) { gridThickness.toPx() } }
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFFCDECF9))
                    .onGloballyPositioned {
                        boardWidth = with(density) { it.size.width.toDp() }
                        boardHeight = with(density) { it.size.height.toDp() }
                    }) {
                Row {
                    repeat(horizontalTilesCount) {
                        Box(
                            modifier = Modifier.fillMaxHeight().width(gridThickness)
                                .background(Color.White)
                        )
                        Spacer(Modifier.weight(1f).onGloballyPositioned {
                            tileWidth = with(density) { it.size.width.toDp() }
                        })
                    }
                }
                Column {
                    repeat(verticalTilesCount) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(gridThickness)
                                .background(Color.White)
                        )
                        Spacer(Modifier.weight(1f).onGloballyPositioned {
                            tileHeight = with(density) { it.size.height.toDp() }
                        })
                    }
                }

                boardScope(object : BoardScope {
                    @Composable
                    override fun Submarine(point: Point) {
                        BoardContent(point) {
                            Image(
                                painter = painterResource(Res.drawable.submarine),
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    @Composable
                    override fun Mine(point: Point) {
                        BoardContent(point) {
                            Image(
                                painter = painterResource(Res.drawable.mine),
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    @Composable
                    override fun NumberedMine(point: Point, mineIndex: Int) {
                        BoardContent(point) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.mine),
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                                Text(mineIndex.toString(), color = Color.White)
                            }
                        }
                    }

                    @Composable
                    override fun DetonatedMine(point: Point) {
                        BoardContent(point) {
                            var showDetonatedMine by remember { mutableStateOf(true) }
                            LaunchedEffect(Unit) {
                                delay(500)
                                showDetonatedMine = false
                            }

                            androidx.compose.animation.AnimatedVisibility(showDetonatedMine) {
                                Text("🔥")
                            }
                        }
                    }

                    @Composable
                    private fun BoardContent(
                        point: Point,
                        content: @Composable BoxScope.() -> Unit
                    ) {
                        Box(
                            modifier = Modifier
                                .width(tileWidth - gridThickness)
                                .height(tileHeight - gridThickness)
                                .offset {
                                    IntOffset(
                                        x = (tileWidthPx * point.x + (gridThicknessPx * (point.x + 1))).toInt(),
                                        y = (tileHeightPx * point.y + (gridThicknessPx * (point.y + 1))).toInt()
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            content()
                        }
                    }
                })
            }
        }
    }
}
