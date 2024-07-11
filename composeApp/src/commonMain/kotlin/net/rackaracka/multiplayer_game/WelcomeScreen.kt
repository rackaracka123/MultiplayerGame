package net.rackaracka.multiplayer_game

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WelcomeScreenViewModel : ViewModel(), KoinComponent {
    private val h : Foo by inject()
    init {
        h.bar()
    }
}

@Composable
fun WelcomeScreen(viewModel: WelcomeScreenViewModel = viewModel { WelcomeScreenViewModel() }) {
    Dog()
}