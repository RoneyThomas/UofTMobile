package ca.utoronto.megaapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ca.utoronto.megaapp.ui.screens.AppViewModel
import ca.utoronto.megaapp.ui.screens.homeScreen.HomeScreen
import ca.utoronto.megaapp.ui.screens.licenseScreen.ThirdPartyNotices
import ca.utoronto.megaapp.ui.screens.rssFeed.RssScreen
import ca.utoronto.megaapp.ui.screens.settingsScreen.SettingsPage
import ca.utoronto.megaapp.ui.theme.UofTMobileTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UofTMobileNavHost(
                application = application
            )
        }
    }
}


@Composable
fun UofTMobileNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "home",
    application: Application
) {
    val appViewModel = viewModel { AppViewModel(application) }
    NavHost(
        modifier = modifier, navController = navController, startDestination = startDestination
    ) {
        composable("home") {
            UofTMobileTheme {
                HomeScreen(
                    appViewModel,
                    navController
                )
            }
        }
        composable("rssScreen") {
            UofTMobileTheme { RssScreen(appViewModel, navController) }
        }
        composable("settings") {
            UofTMobileTheme { SettingsPage(appViewModel, navController) }
        }
        composable("thirdPartyNotices") {
            UofTMobileTheme {
                ThirdPartyNotices(navController)
            }
        }
    }
}