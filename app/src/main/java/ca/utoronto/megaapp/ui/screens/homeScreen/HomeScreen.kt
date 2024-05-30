package ca.utoronto.megaapp.ui.screens.homeScreen

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.utoronto.megaapp.R
import ca.utoronto.megaapp.ui.screens.AppViewModel
import ca.utoronto.megaapp.ui.theme.extraLightBlue
import ca.utoronto.megaapp.ui.theme.lightBlue
import ca.utoronto.megaapp.ui.theme.onSecondaryLight
import ca.utoronto.megaapp.ui.theme.roundBookmarkBlue
import coil.compose.AsyncImage

@OptIn(
    ExperimentalMaterial3Api::class,
)
@Composable
fun HomeScreen(
    appViewModel: AppViewModel,
    onNavigateToRssScreen: () -> Unit,
    onNavigateToSettingsScreen: () -> Unit
) {
    // Sets the navigationBarColor, remove this in future when switching to dynamic theming
    (LocalView.current.context as Activity).window.navigationBarColor = lightBlue.toArgb()

    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var addBookmarkSheet by remember { mutableStateOf(false) }
    val addBookmarkSheetState = rememberModalBottomSheetState()
    var overFlowMenuExpanded by remember { mutableStateOf(false) }
    var showRemoveIcon = appViewModel.showRemoveIcon.observeAsState().value

    val searchQuery = appViewModel.searchQuery.observeAsState().value
    val searchSections = appViewModel.filteredSections().observeAsState().value
    val showBookmarkInstructions = appViewModel.showBookmarkInstructions.observeAsState().value
    val jsonResponse = appViewModel.jsonResponse.value
    val bookmarksDTOList = appViewModel.getBookMarks().observeAsState().value

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.surface,
                    actionIconContentColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    AsyncImage(
                        model = R.drawable.uoft,
                        contentDescription = "University of Toronto Logo",
                        modifier = Modifier.height(48.dp)
                    )
                },
                actions = {
                    if (showRemoveIcon == true) {
                        IconButton(onClick = {
                            appViewModel.setEditMode(false)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Done, contentDescription = "More"
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            addBookmarkSheet = true
                            showRemoveIcon = false
                            appViewModel.setEditMode(false)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Add, contentDescription = "More"
                            )
                        }
                    }
                    Box(
                        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                    ) {
                        IconButton(onClick = { overFlowMenuExpanded = !overFlowMenuExpanded }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert, contentDescription = "More"
                            )
                        }

                        DropdownMenu(
                            expanded = overFlowMenuExpanded,
                            onDismissRequest = { overFlowMenuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            DropdownMenuItem(text = { Text("Edit") }, onClick = {
                                appViewModel.setEditMode(!showRemoveIcon!!)
                                Toast.makeText(context, "Edit", Toast.LENGTH_SHORT).show()
                                overFlowMenuExpanded = false
                            })
                            DropdownMenuItem(text = { Text("Setting") }, onClick = {
                                onNavigateToSettingsScreen()
                                overFlowMenuExpanded = false
                            })
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
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
            Card(
                modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White, //Card background color
                )
            ) {
                AndroidView(factory = {
                    RecyclerView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                        layoutManager = GridLayoutManager(context, 4)
                        adapter = AppAdapter(
                            onNavigateToRssScreen, appViewModel::removeBookmark, appViewModel
                        ).also {
                            it.submitList(
                                bookmarksDTOList
                            )
                        }
                        this.setPadding(6, 0, 6, 24)
                    }
                }, update = {
                    if (showRemoveIcon == true) {
                        itemTouchHelper.attachToRecyclerView(it)
                    } else {
                        itemTouchHelper.attachToRecyclerView(null)
                    }
                    (it.adapter as AppAdapter).submitList(bookmarksDTOList)
                })
            }
            if (showBookmarkInstructions == true) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = onSecondaryLight,
                    ), modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp, 8.dp)) {
                        Text(
                            "To get started, simply click the + symbol in the bottom left corner to access to list of bookmarks from UofT.",
                            color = Color.DarkGray
                        )
                        Row {
                            Spacer(Modifier.weight(1.0f))
                            Button(
                                onClick = { appViewModel.hideBookmarkInstructions() },
                                colors = ButtonDefaults.buttonColors(containerColor = roundBookmarkBlue)
                            ) {
                                Text(text = "Dismiss")
                            }
                        }
                    }
                }
            }

            if (addBookmarkSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        addBookmarkSheet = false
                    }, sheetState = addBookmarkSheetState
                ) {
                    Box {
                        // Sheet content
                        Column(modifier = Modifier.padding(8.dp, 0.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(16.dp, 0.dp)
                            ) {
                                TextField(value = searchQuery ?: "",
                                    onValueChange = {
                                        appViewModel.searchQuery.value = it
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            BorderStroke(
                                                width = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            ), shape = RoundedCornerShape(50)
                                        )
                                        .padding(8.dp, 0.dp),
                                    placeholder = { Text("Search") },
                                    colors = TextFieldDefaults.colors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedContainerColor = Color.Transparent
                                    ),
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search"
                                        )
                                    })
                            }
                            LazyVerticalGrid(GridCells.Fixed(4), contentPadding = PaddingValues(
                                start = 8.dp, top = 12.dp, end = 8.dp, bottom = 12.dp
                            ), content = {
                                searchSections?.forEach { (key, value) ->
                                    run {
                                        item(span = { GridItemSpan(maxLineSpan) }, key = key) {

                                            Text(
                                                text = key,
                                                modifier = Modifier.padding(vertical = 12.dp),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        }
                                        items(value.apps.toList(), key = { it }) { item ->
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
                                                        .padding(8.dp, 8.dp, 8.dp, 24.dp)
                                                        .size(52.dp)
                                                        .background(
                                                            Color(0xFF2F4675), CircleShape
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
                                                        modifier = Modifier.height(32.dp),
                                                    )
                                                    if (bookmarksDTOList?.any { item1 -> item1.id == jsonResponse.apps[item].id } == true) {
                                                        AsyncImage(
                                                            model = R.drawable.checkmark,
                                                            contentDescription = "Selected"
                                                        )
                                                    }
                                                }
                                                Text(
                                                    fontWeight = FontWeight.Medium,
                                                    text = jsonResponse?.apps!![item].name,
                                                    textAlign = TextAlign.Center,
                                                    color = Color.DarkGray,
                                                    softWrap = true
                                                )
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}