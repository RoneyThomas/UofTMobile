package ca.utoronto.megaapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ca.utoronto.megaapp.ui.screens.AppViewModel
import ca.utoronto.megaapp.ui.screens.homeScreen.HomeScreen
import ca.utoronto.megaapp.ui.screens.rssFeed.RssScreen
import com.example.compose.UofTMobileTheme


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
    val appViewModel = AppViewModel(application)
    NavHost(
        modifier = modifier, navController = navController, startDestination = startDestination
    ) {
        composable("home") {
            UofTMobileTheme {
                HomeScreen(
                    appViewModel
                ) { navController.navigate("rssScreen") }
            }
        }
        composable("rssScreen") {
            UofTMobileTheme { RssScreen(appViewModel, navController) }
        }

    }
}


//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    UofTMobileTheme {
//        HomeScreen(AppViewModel(LocalContext.current.applicationContext))
//    }
//}