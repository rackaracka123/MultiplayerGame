package net.rackaracka.multiplayer_game

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import multiplayergame.design.generated.resources.Res
import multiplayergame.design.generated.resources.mine
import org.jetbrains.compose.resources.painterResource
import kotlin.reflect.KFunction0

sealed class DashboardItem {
    data object Cancel : DashboardItem()
    data object ReleaseMine : DashboardItem()
    data object DetonateMine : DashboardItem()
}

@Composable
fun Dashboard(dashboardItems: List<DashboardItem>) {
    Column {
        dashboardItems.forEach { dashboardItem ->
            when (dashboardItem) {
                DashboardItem.Cancel -> Text("Avbryt (Escape)")
                DashboardItem.DetonateMine -> Text("Detonera (F)")
                DashboardItem.ReleaseMine -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Släpp mina (1)")
                    Image(
                        painter = painterResource(Res.drawable.mine),
                        contentDescription = null,
                        modifier = Modifier.sizeIn(
                            maxWidth = 100.dp,
                            maxHeight = 100.dp
                        )
                    )
                }
            }
        }
    }
}