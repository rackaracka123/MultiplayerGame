package net.rackaracka.multiplayer_game.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import multiplayergame.composeapp.generated.resources.Res
import multiplayergame.composeapp.generated.resources.cover
import multiplayergame.composeapp.generated.resources.start_button
import org.jetbrains.compose.resources.painterResource

@Composable
fun StartScreen(
    onClickStart: () -> Unit
) {
    val density = LocalDensity.current
    var screenHeight by remember { mutableStateOf(0.dp) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            painter = painterResource(Res.drawable.cover),
            contentDescription = null,
            modifier = Modifier.matchParentSize().onGloballyPositioned {
                screenHeight = with(density) { it.size.height.toDp() }
            },
            contentScale = ContentScale.Crop
        )
        Image(
            painter = painterResource(Res.drawable.start_button),
            contentDescription = null,
            modifier = Modifier.padding(bottom = screenHeight / 4).fillMaxSize(.2f).aspectRatio(1f).clip(CircleShape)
                .clickable {
                    onClickStart()
                },
        )
    }
}

@Composable
fun Button() {

}