package ca.utoronto.megaapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ca.utoronto.megaapp.ui.screens.AppViewModel
import ca.utoronto.megaapp.ui.screens.homeScreen.HomeScreen
import ca.utoronto.megaapp.ui.screens.rssFeed.RssScreen
import ca.utoronto.megaapp.ui.screens.settingsScreen.SettingsPage
import ca.utoronto.megaapp.ui.theme.UofTMobileTheme
import ca.utoronto.megaapp.ui.theme.extraLightBlue
import ca.utoronto.megaapp.ui.theme.lightBlue
import coil.compose.AsyncImage
import kotlinx.coroutines.delay


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
    startDestination: String = "migrateBookmarks",
    application: Application
) {
    val appViewModel = viewModel { AppViewModel(application) }
    NavHost(
        modifier = modifier, navController = navController, startDestination = startDestination
    ) {
        composable("migrateBookmarks") {
            UofTMobileTheme {
                MigrateBookmarks(appViewModel, navController)
            }
        }
        composable("home") {
            UofTMobileTheme {
                HomeScreen(
                    appViewModel,
                    { navController.navigate("rssScreen") },
                    { navController.navigate("settings") }
                )
            }
        }
        composable("rssScreen") {
            UofTMobileTheme { RssScreen(appViewModel, navController) }
        }
        composable("settings") {
            UofTMobileTheme { SettingsPage(appViewModel, navController) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrateBookmarks(appViewModel: AppViewModel, navcontroller: NavHostController) {
    val TAG = "MigrateBookmarks"
    var currentProgress by remember { mutableStateOf(0f) }
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(key1 = Unit) {
        loadProgress { progress ->
            currentProgress = progress
        }
        loading = false
    }
    Scaffold(topBar = {
        TopAppBar(
            colors = topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.surface,
                actionIconContentColor = MaterialTheme.colorScheme.surface
            ),
            title = {
                AsyncImage(
                    model = R.drawable.uoftcrst_stacked_white_webp,
                    contentDescription = "University of Toronto Logo",
                    modifier = Modifier.height(48.dp)
                )
            })
    },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                extraLightBlue,
                                lightBlue,
                            ), start = Offset.Zero, end = Offset(0f, Float.POSITIVE_INFINITY)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(start = 16.dp, end = 16.dp, top = 24.dp)
                ) {
                    Text(
                        "Migrating your data",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (loading) {
                        LinearProgressIndicator(
                            progress = { currentProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            trackColor = extraLightBlue
                        )
                    } else {
                        navcontroller.navigate("home")
                    }
                }
            }
        })
}

suspend fun loadProgress(updateProgress: (Float) -> Unit) {
    for (i in 1..100) {
        updateProgress(i.toFloat() / 100)
        delay(32)
    }
}