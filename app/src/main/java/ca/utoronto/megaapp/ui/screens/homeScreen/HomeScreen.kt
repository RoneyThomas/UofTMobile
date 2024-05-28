package ca.utoronto.megaapp.ui.screens.homeScreen

import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.ListPopupWindow.MATCH_PARENT
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.utoronto.megaapp.R
import ca.utoronto.megaapp.ui.composables.SettingsPage
import ca.utoronto.megaapp.ui.screens.AppViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
)
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
    var expanded by remember { mutableStateOf(false) }
    var showRemoveIcon = appViewModel.showRemoveIcon.observeAsState().value
    val bookmarksDTOList = appViewModel.getBookMarks().observeAsState().value
    val refresh = appViewModel.refresh.observeAsState().value
    val searchQuery = appViewModel.searchQuery.observeAsState().value
    val searchSections = appViewModel.filteredSections().observeAsState().value
    val showBookmarkInstructions = appViewModel.showBookmarkInstructions.observeAsState()
    val jsonResponse = appViewModel.jsonResponse.value
    val context = LocalContext.current

    val navItemColor = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        indicatorColor = Color.Transparent,
        unselectedIconColor = Color.White,
        unselectedTextColor = Color.White,
    )
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
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
                actions = {
                    Box(
                        modifier = Modifier
                            .wrapContentSize(Alignment.TopEnd)
                    ) {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                tint = MaterialTheme.colorScheme.surface,
                                contentDescription = "More"
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = { Toast.makeText(context, "Load", Toast.LENGTH_SHORT).show() }
                            )
                            DropdownMenuItem(
                                text = { Text("Setting") },
                                onClick = { Toast.makeText(context, "Save", Toast.LENGTH_SHORT).show() }
                            )
                        }
                    }
                    // RowScope here, so these icons will be placed horizontally
//                    IconButton(onClick = { /* doSomething() */ }) {
//                        Icon(
//                            imageVector = Icons.Default.MoreVert,
//                            tint = MaterialTheme.colorScheme.surface,
//                            contentDescription = "Localized description"
//                        )
//                    }
                },
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick =
                {
                    addBottomSheet = true
                    showRemoveIcon = false
                    appViewModel.setEditMode(false)
                },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        },
//        bottomBar = {
//        var selectedItem by remember { mutableIntStateOf(-1) }
//        NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
//            NavigationBarItem(icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
//                label = { Text("Add") },
//                selected = false,
//                colors = navItemColor,
//                onClick = {
//                    selectedItem = 0
//                    addBottomSheet = true
//                    showRemoveIcon = false
//                    appViewModel.setEditMode(false)
////                    appViewModel.showRemoveIcon(showRemoveIcon)
//                })
//            NavigationBarItem(icon = { Icon(Icons.Filled.Edit, contentDescription = "Edit") },
//                label = {
//                    if (showRemoveIcon == true) {
//                        Text("Done")
//                    } else {
//                        Text("Edit")
//                    }
//                },
//                selected = false,
//                colors = navItemColor,
//                onClick = {
//                    selectedItem = 1
//                    appViewModel.setEditMode(!showRemoveIcon!!)
//                })
//        }
//    }
    ) { innerPadding ->
        PullToRefreshBox(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
//                .background(
//                    color = Color(0xFFD0D1C9)
//                )
//                .paint(
//                    painterResource(id = R.drawable.background), contentScale = ContentScale.Fit
//                ),
            isRefreshing = refresh ?: false,
            onRefresh = {
                appViewModel.refresh()
            },
        ) {

            AndroidView(factory = {
                RecyclerView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    layoutManager = GridLayoutManager(context, 4)
                    adapter = AppAdapter(
                        onNavigateToRssScreen, appViewModel::removeBookmark, appViewModel
                    ).also {
                        it.submitList(
                            bookmarksDTOList
                        )
                    }
                    this.setPadding(16, 12, 16, 0)
                }
            }, update = {
                if (showRemoveIcon == true) {
                    itemTouchHelper.attachToRecyclerView(it)
                } else {
                    itemTouchHelper.attachToRecyclerView(null)
                }
                (it.adapter as AppAdapter).submitList(bookmarksDTOList)
            })

            if (showBookmarkInstructions.value == true) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp)
                        .clip(shape = RoundedCornerShape(8.dp))
                        .background(Color(0XCC1E3765))
                ) {
                    Column(modifier = Modifier.padding(16.dp, 8.dp)) {
                        Text(
                            "To get started, simply click the + symbol in the bottom left corner to access to list of bookmarks from UofT.",
                            color = Color.White
                        )
                        Row {
                            Spacer(Modifier.weight(1.0f))
                            Button(onClick = { appViewModel.hideBookmarkInstructions() }) {
                                Text(text = "Dismiss")
                            }
                        }

                    }
                }
            }

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
                                TextField(
                                    value = searchQuery ?: "", onValueChange = {
                                        appViewModel.searchQuery.value = it
                                    }, modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            BorderStroke(
                                                width = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            ),
                                            shape = RoundedCornerShape(50)
                                        )
                                        .padding(8.dp, 0.dp),
                                    placeholder = { Text("Search") },
                                    colors = TextFieldDefaults.colors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedContainerColor = Color.Transparent),
                                    trailingIcon = { Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search"
                                    )}
                                )
                            }
                            LazyVerticalGrid(GridCells.Fixed(4),
                                // content padding
                                contentPadding = PaddingValues(
                                    start = 8.dp, top = 12.dp, end = 8.dp, bottom = 12.dp
                                ), content = {
                                    searchSections?.forEach { (key, value) ->
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
                                                        if (bookmarksDTOList?.any { item1 -> item1.id == jsonResponse.apps[item].id } == true) {
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
                Log.d("Deleted", "Deleted")

//                SettingsPage(
//                    scope = scope,
//                    context = context,
//                    appViewModel = appViewModel
//                ).SettingsPageMain()
            }
        }
    }
}

@Composable
fun DropDownMenu() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd)
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Load") },
                onClick = { Toast.makeText(context, "Load", Toast.LENGTH_SHORT).show() }
            )
            DropdownMenuItem(
                text = { Text("Save") },
                onClick = { Toast.makeText(context, "Save", Toast.LENGTH_SHORT).show() }
            )
        }
    }
}