package ca.utoronto.megaapp

import android.app.Application
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.text.HtmlCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ca.utoronto.megaapp.ui.screens.homeScreen.AppViewModel
import coil.compose.AsyncImage
import com.example.compose.UofTMobileTheme
import kotlinx.coroutines.launch


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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    appViewModel: AppViewModel, onNavigateToRssScreen: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val addSheetState = rememberModalBottomSheetState()
    val aboutSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var addBottomSheet by remember { mutableStateOf(false) }
    var aboutBottomSheet by remember { mutableStateOf(false) }
    var showRemoveIcon by remember { mutableStateOf(false) }
    val bookmarks = appViewModel.bookmarks.observeAsState().value
    val searchQuery = appViewModel.searchQuery.observeAsState().value
    val searchSections = appViewModel.filteredSections().observeAsState().value
    val jsonResponse = appViewModel.jsonResponse.value

    val context = LocalContext.current

    var appId by remember {
        mutableStateOf("")
    }

    val navItemColor = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        indicatorColor = Color.Transparent,
        unselectedIconColor = Color.White,
        unselectedTextColor = Color.White,
    )
    Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        CenterAlignedTopAppBar(
            colors = topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.surface,
            ),
            title = {
                AsyncImage(
                    model = R.drawable.uoftcrst_stacked_white_use_only_on_655,
                    contentDescription = "University of Toronto Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(48.dp)
                )
            },
        )
    }, bottomBar = {
        var selectedItem by remember { mutableIntStateOf(-1) }
        NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
            NavigationBarItem(icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
                label = { Text("Add") },
                selected = false,
                colors = navItemColor,
                onClick = {
                    selectedItem = 0
                    addBottomSheet = true
                    showRemoveIcon = false
                })
            NavigationBarItem(icon = { Icon(Icons.Filled.Edit, contentDescription = "Edit") },
                label = {
                    if (showRemoveIcon) {
                        Text("Done")
                    } else {
                        Text("Edit")
                    }
                },
                selected = false,
                colors = navItemColor,
                onClick = {
                    selectedItem = 1
                    showRemoveIcon = !showRemoveIcon
                })
        }
    }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(color = Color(0xFFD0D1C9))
        ) {

            LazyVerticalGrid(columns = GridCells.Adaptive(80.dp), Modifier.zIndex(1f),
                // content padding
                contentPadding = PaddingValues(
                    start = 8.dp, top = 12.dp, end = 8.dp, bottom = 12.dp
                ), content = {
                    items(items = bookmarks?.toList() ?: emptyList()) { item ->
                        val app = appViewModel.getAppById(item)
                        var tintColor by remember {
                            mutableStateOf(Color(0xFFD0D1C9))
                        }
                        if (app != null) {
                            Column(verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .dragAndDropTarget(shouldStartDragAndDrop = { event ->
                                        event
                                            .mimeTypes()
                                            .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                                    }, target = object : DragAndDropTarget {
                                        override fun onDrop(event: DragAndDropEvent): Boolean {
                                            val draggedData =
                                                event.toAndroidDragEvent().clipData.getItemAt(0).text
                                            appId = draggedData.toString()
                                            Log.d(
                                                "MainActivity", "onDrop: dropped $appId on $item"
                                            )
                                            appViewModel.swapBookmark(item, appId)
                                            return true
                                        }

                                        override fun onEntered(event: DragAndDropEvent) {
                                            super.onEntered(event)
                                            Log.d(
                                                "MainActivity", "onEntered: selected $item"
                                            )
                                            tintColor = Color(0xff6FC7EA)
                                        }

                                        override fun onEnded(event: DragAndDropEvent) {
                                            super.onEntered(event)
                                            Log.d(
                                                "MainActivity", "onEnded: selected $item"
                                            )
                                            tintColor = Color(0xFFD0D1C9)
                                        }

                                        override fun onExited(event: DragAndDropEvent) {
                                            super.onEntered(event)
                                            Log.d(
                                                "MainActivity", "onExited: selected $item"
                                            )
                                            tintColor = Color(0xFFD0D1C9)
                                        }

                                    })
                                    .dragAndDropSource {
                                        detectTapGestures(onLongPress = {
                                            startTransfer(
                                                DragAndDropTransferData(
                                                    ClipData.newPlainText(
                                                        "appId", item
                                                    )
                                                )
                                            )
                                        }, onTap = {
                                            if (app.id == "newseng") {
                                                onNavigateToRssScreen.invoke()
                                            } else {
                                                val url = app.url
                                                val intent = CustomTabsIntent
                                                    .Builder()
                                                    .build()
                                                intent.launchUrl(context, Uri.parse(url))
                                            }

                                        })
                                    }) {
                                Box(
                                    Modifier
                                        .padding(16.dp, 16.dp, 16.dp, 8.dp)
                                        .size(64.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(2.dp, tintColor, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = context.resources.getIdentifier(
                                            app.imageLocalName.lowercase(),
                                            "drawable",
                                            context.packageName
                                        ),
                                        contentDescription = "University of Toronto Logo",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.height(48.dp)
                                    )
                                    if (showRemoveIcon && !jsonResponse!!.mandatoryApps.contains(app.id)) {
                                        AsyncImage(model = R.drawable.minus,
                                            contentDescription = "Remove Button",
                                            modifier = Modifier.clickable {
                                                    Log.d(
                                                        "Remove Button",
                                                        "CenterAlignedTopAppBarExample: " + app.id
                                                    )
                                                    if ((appViewModel.bookmarks.value?.size
                                                            ?: 0) <= 0
                                                    ) {
                                                        showRemoveIcon = false
                                                    }
                                                    appViewModel.removeBookmark(app.id)
                                                })
                                    }
                                }
                                Text(
                                    text = app.name,
                                    textAlign = TextAlign.Center,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        shadow = Shadow(
                                            color = Color.Black,
                                            offset = Offset(2f, 2f),
                                            blurRadius = 4f
                                        )
                                    )
                                )
                            }
                        }

                    }
                })

            AsyncImage(
                model = R.drawable.background,
                contentDescription = "UofT Logo",
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(256.dp)
            )



            if (addBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        addBottomSheet = false
                    }, sheetState = aboutSheetState
                ) {
                    Box {
                        // Sheet content
                        Column(modifier = Modifier.padding(8.dp, 0.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(16.dp, 0.dp)
                            ) {
                                Button(onClick = {
                                    scope.launch { addSheetState.hide() }.invokeOnCompletion {
                                        if (!addSheetState.isVisible) {
                                            addBottomSheet = false
                                        }
                                    }
                                }) {
                                    Text("Done")
                                }
                                OutlinedTextField(
                                    value = searchQuery ?: "", onValueChange = {
                                        appViewModel.searchQuery.value = it
                                    }, modifier = Modifier
                                        .padding(
                                            8.dp, 0.dp
                                        )
                                        .weight(1f)
                                )
                                Button(onClick = {
                                    scope.launch { addSheetState.hide() }.invokeOnCompletion {
                                        addBottomSheet = false
                                        aboutBottomSheet = true
                                    }
                                }) {
                                    Text("About")
                                }
                            }
                            LazyVerticalGrid(GridCells.Fixed(4),
                                // content padding
                                contentPadding = PaddingValues(
                                    start = 8.dp, top = 12.dp, end = 8.dp, bottom = 12.dp
                                ), content = {
                                    searchSections!!.forEach { (key, value) ->
                                        run {
                                            item(span = { GridItemSpan(maxLineSpan) }) {
                                                Text(
                                                    text = key,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 16.sp,
                                                    lineHeight = 24.sp
                                                )
                                            }
                                            items(value.apps.toList()) { item ->
                                                Column(verticalArrangement = Arrangement.Center,
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.clickable {
                                                        Log.d(
                                                            "MainActivity",
                                                            "CenterAlignedTopAppBarExample: I am clicked in add" + jsonResponse?.apps!![item].id
                                                        )
                                                        appViewModel.addBookmark(jsonResponse.apps[item].id)
                                                    }) {
                                                    Box(
                                                        Modifier
                                                            .padding(16.dp, 16.dp, 16.dp, 8.dp)
                                                            .size(64.dp)
                                                            .background(
                                                                MaterialTheme.colorScheme.primary,
                                                                RoundedCornerShape(8.dp)
                                                            ), contentAlignment = Alignment.Center
                                                    ) {
                                                        AsyncImage(
                                                            model = context.resources.getIdentifier(
                                                                jsonResponse?.apps!![item].imageLocalName.lowercase(),
                                                                "drawable",
                                                                context.packageName
                                                            ),
                                                            contentDescription = "University of Toronto Logo",
                                                            contentScale = ContentScale.Fit,
                                                            modifier = Modifier.height(48.dp)
                                                        )
                                                        if (bookmarks?.contains(
                                                                jsonResponse.apps[item].id
                                                            ) == true
                                                        ) {
                                                            AsyncImage(
                                                                model = R.drawable.checkmark,
                                                                contentDescription = "Selected"
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = jsonResponse?.apps!![item].name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color.Black,
                                                        textAlign = TextAlign.Center,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                })
                        }
                        AsyncImage(
                            model = R.drawable.background,
                            contentDescription = "UofT Logo",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .height(256.dp)
                        )
                    }
                }
            }
            if (aboutBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        aboutBottomSheet = false
                    }, sheetState = aboutSheetState
                ) {
                    // Sheet content
                    Column(modifier = Modifier.padding(12.dp, 8.dp)) {
                        Button(onClick = {
                            scope.launch { aboutSheetState.hide() }.invokeOnCompletion {
                                if (!aboutSheetState.isVisible) {
                                    aboutBottomSheet = false
                                }
                            }
                        }) {
                            Text("Done")
                        }
                        Text(
                            text = "Feedback",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Have any comments or suggestions on the content or layout of U of T Mobile? We'd love to hear it!",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
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
                                .padding(top = 4.dp)
                        ) {
                            Text("Submit Feedback")
                        }
                        Text(
                            text = "Version",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(text = "Version 3.0, Build 1", textAlign = TextAlign.Center)
                        Text(
                            text = "Settings",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Button(onClick = {
                            appViewModel.resetBookmarks()
                        }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Text("Reset U of T Mobile")
                        }
                        Button(onClick = {
                            appViewModel.loadApps()
                        }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Text("Refresh Index")
                        }
                        Text("MADLab",
                            textDecoration = TextDecoration.Underline,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
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
        }


    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RssScreen(appViewModel: AppViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val rssFeed = appViewModel.getRssFeed().observeAsState().value
    if (rssFeed != null) {
        Log.d("MainActivity", "RssScreen: " + rssFeed.title)
        Scaffold(topBar = {
            TopAppBar(colors = topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.surface,
            ), title = {
                Text(rssFeed.title.toString())
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
//            Column(
//                modifier = Modifier.padding(innerPadding),
//                verticalArrangement = Arrangement.spacedBy(16.dp),
//            ) {
//                rssFeed.items.forEach { rssItem ->
//                    Text(
//                        text = rssDateFormatter(rssItem.pubDate ?: "")
//                    )
//                    Text(
//                        modifier = Modifier.padding(8.dp),
//                        text = rssItem.title!!,
//                    )
//                    Text(text = "by ${rssItem.author}")
//
//                    Text(text = "by ${HtmlCompat.fromHtml(rssItem.description!!, HtmlCompat.FROM_HTML_MODE_LEGACY)[0]}")
//                }
//            }
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(rssFeed.items.size) { item ->
                    Column(modifier = Modifier.clickable {
                        val url = rssFeed.items[item].link
                        val intent = CustomTabsIntent.Builder().build()
                        intent.launchUrl(context, Uri.parse(url))
                    }) {
                        Text(
                            text = rssDateFormatter(rssFeed.items[item].pubDate ?: ""),
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
            }
        }
    } else {
        Text(text = "Not Connected to Internet")
    }
}

fun rssDateFormatter(s: String): String {
    while (s.length >= 16) {
        if (s[5] == '0') {
            return "${s.substring(8, 11)} ${s.substring(6, 7)}, ${s.substring(12, 16)}"
        }
        return "${s.substring(8, 11)} ${s.substring(5, 7)}, ${s.substring(12, 16)}"
    }
    return ""
}

fun rssDescription(s: String) {

}


//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    UofTMobileTheme {
//        HomeScreen(AppViewModel(LocalContext.current.applicationContext))
//    }
//}