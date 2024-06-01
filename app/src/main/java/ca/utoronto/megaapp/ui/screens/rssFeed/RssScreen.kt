package ca.utoronto.megaapp.ui.screens.rssFeed

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.navigation.NavHostController
import ca.utoronto.megaapp.ui.screens.AppViewModel
import ca.utoronto.megaapp.ui.util.rssDateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RssScreen(appViewModel: AppViewModel, navController: NavHostController) {
    // Sets the navigationBarColor, remove this in future when switching to dynamic theming
    (LocalView.current.context as Activity).window.navigationBarColor = Color.Transparent.toArgb()

    val context = LocalContext.current
    val rssFeed = appViewModel.getRssFeed().observeAsState().value
    Log.d("MainActivity", "RssScreen: " + rssFeed?.title)
    Scaffold(topBar = {
        TopAppBar(colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.surface,
        ), title = {
            Text(rssFeed?.title ?: "UofT Engineering News")
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
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if ((rssFeed?.items?.size ?: 0) > 0) {
                items(rssFeed?.items?.size ?: 0) { item ->
                    Column(modifier = Modifier.clickable {
                        val url = rssFeed!!.items[item].link
                        val intent = CustomTabsIntent.Builder().build()
                        intent.launchUrl(context, Uri.parse(url))
                    }) {
                        Text(
                            text = rssDateFormatter(rssFeed!!.items[item].pubDate ?: ""),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        )
                        Text(
                            text = rssFeed.items[item].title!!,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "by ${rssFeed.items[item].author}",
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        val description = HtmlCompat.fromHtml(
                            rssFeed.items[item].description!!, HtmlCompat.FROM_HTML_MODE_LEGACY
                        ).toString().split("\n\n")[0]
                        Text(
                            text = description, modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                item {
                    Column(
                        modifier = Modifier.fillParentMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Not Connected to Internet", fontWeight = FontWeight.Light,
                        )
                    }
                }
            }
        }
    }
}