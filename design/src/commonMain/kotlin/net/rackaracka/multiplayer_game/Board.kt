package net.rackaracka.multiplayer_game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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

    @Composable
    fun HighlightSector(color: Color, sector: Sector)
}

/*
 * The contents are not perfectly placed at the point.
 */
@OptIn(ExperimentalLayoutApi::class)
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

            FlowRow(
                maxItemsInEachRow = horizontalTilesCount,
                modifier = Modifier.fillMaxSize().onGloballyPositioned {
                    boardWidth = with(density) { it.size.width.toDp() }
                    boardHeight = with(density) { it.size.height.toDp() }
                }
            ) {
                repeat(verticalTilesCount) { yIndex ->
                    repeat(horizontalTilesCount) { xIndex ->
                        var targetBorderColor by remember { mutableStateOf(Color.White) }
                        val borderColor by animateColorAsState(
                            targetValue = targetBorderColor,
                            animationSpec = tween(1000)
                        )
                        Box(
                            Modifier.width(boardWidth / horizontalTilesCount)
                                .height(boardHeight / verticalTilesCount)
                                .border(2.dp, borderColor)
                                .background(Color(0xFFCDECF9))
                        ) {
                            boardScope(object : BoardScope {
                                @Composable
                                override fun Submarine(point: Point) {
                                    if (point.x != xIndex || point.y != yIndex) return
                                    Image(
                                        painter = painterResource(Res.drawable.submarine),
                                        contentDescription = null,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }


                                @Composable
                                override fun Mine(point: Point) {
                                    if (point.x != xIndex || point.y != yIndex) return
                                    Image(
                                        painter = painterResource(Res.drawable.mine),
                                        contentDescription = null,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }


                                @Composable
                                override fun NumberedMine(point: Point, mineIndex: Int) {
                                    if (point.x != xIndex || point.y != yIndex) return
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

                                @Composable
                                override fun DetonatedMine(point: Point) {
                                    if (point.x != xIndex || point.y != yIndex) return

                                    var showDetonatedMine by remember { mutableStateOf(true) }
                                    LaunchedEffect(Unit) {
                                        delay(500)
                                        showDetonatedMine = false
                                    }

                                    androidx.compose.animation.AnimatedVisibility(showDetonatedMine) {
                                        Text("🔥")
                                    }
                                }

                                @Composable
                                override fun HighlightSector(color: Color, sector: Sector) {
                                    if (Point(xIndex, yIndex).isInside(sector)) {
                                        DisposableEffect(color) {
                                            targetBorderColor = color
                                            onDispose {
                                                targetBorderColor = Color.White
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

private fun Point.isInside(sector: Sector): Boolean {
    return x in sector.topLeft.x..sector.bottomRight.x &&
            y in sector.topLeft.y..sector.bottomRight.y
}

