package za.co.etaxi.app

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import za.co.etaxi.app.core.theme.EtaxiTheme
import za.co.etaxi.app.feature.splash.SplashScreen

@Composable
fun App() {
    EtaxiTheme {
        Navigator(SplashScreen) { nav ->
            SlideTransition(nav)
        }
    }
}
