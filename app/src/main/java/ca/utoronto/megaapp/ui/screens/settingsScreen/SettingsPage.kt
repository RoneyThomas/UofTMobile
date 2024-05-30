package ca.utoronto.megaapp.ui.screens.settingsScreen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ca.utoronto.megaapp.R
import ca.utoronto.megaapp.ui.screens.AppViewModel
import coil.compose.AsyncImage

class SettingsPage
    (private var appViewModel: AppViewModel, private var navController: NavHostController) {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AboutPageMain() {
        // Sets the navigationBarColor, remove this in future when switching to dynamic theming
        (LocalView.current.context as Activity).window.navigationBarColor =
            Color.Transparent.toArgb()

        val context = LocalContext.current
        Log.d("MainActivity", "SettingsPage: ")
        Scaffold(topBar = {
            TopAppBar(colors = topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.surface,
            ), title = {
                Text("Settings")
            }, navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        tint = MaterialTheme.colorScheme.surface,
                        contentDescription = "Back"
                    )
                }
            })
        }) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AboutPageSection(mainText = "Settings", subText = "")

                Text("Reset UofT Mobile", modifier = Modifier.clickable {
                    Toast.makeText(context, "Bookmarks Reset", Toast.LENGTH_SHORT).show()
                    appViewModel.resetBookmarks()
                })
                Text("Refresh Index", modifier = Modifier.clickable {
                    Toast.makeText(context, "Refreshed Index", Toast.LENGTH_SHORT).show()
                    appViewModel.refresh()
                })

                HorizontalDivider()

                AboutPageSection(mainText = "Version", subText = "Version 3.0, Build 1")

                HorizontalDivider()

                AboutPageSection(
                    mainText = "Feedback",
                    subText = "Have any comments or suggestions on the content or layout of U of T Mobile? " + "We'd love to hear it!"
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:") // Only email apps handle this.
                                putExtra(Intent.EXTRA_EMAIL, "mad.lab@utoronto.ca")
                                putExtra(
                                    Intent.EXTRA_SUBJECT, "UofT Mobile Feedback (v3.0, 4)"
                                )
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 8.dp),
                    ) {
                        Text("Submit Feedback")
                    }
                }

                AsyncImage(model = R.drawable.madlab,
                    contentDescription = "Mobile Application Lab logo",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            val url = "https://mobile.utoronto.ca/"
                            val intent = CustomTabsIntent
                                .Builder()
                                .build()
                            intent.launchUrl(context, Uri.parse(url))
                        })
            }
        }
    }

    @Composable
    private fun AboutPageSection(mainText: String, subText: String) {
        Text(
            text = mainText,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondaryContainer
        )
        if (subText.isNotEmpty()) Text(text = subText)
    }
}