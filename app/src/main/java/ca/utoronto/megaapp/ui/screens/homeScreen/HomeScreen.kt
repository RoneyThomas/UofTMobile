package ca.utoronto.megaapp.ui.screens.homeScreen

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import ca.utoronto.megaapp.R
import ca.utoronto.megaapp.ui.screens.AppViewModel
import ca.utoronto.megaapp.ui.theme.extraLightBlue
import ca.utoronto.megaapp.ui.theme.green
import ca.utoronto.megaapp.ui.theme.lightBlue
import ca.utoronto.megaapp.ui.theme.onSecondaryLight
import ca.utoronto.megaapp.ui.theme.red
import ca.utoronto.megaapp.ui.theme.roundBookmarkBlue
import ca.utoronto.megaapp.ui.util.iconResourceMap
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
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
    var editMode by rememberSaveable { mutableStateOf(false) }
    val searchQuery = appViewModel.searchQuery.observeAsState().value
    val searchSections = appViewModel.filteredSections().observeAsState().value
    val showBookmarkInstructions = appViewModel.showBookmarkInstructions.observeAsState().value
    val jsonResponse = appViewModel.jsonResponse.value
    val bookmarksDTOList = appViewModel.getBookMarks().observeAsState().value

    val gridState = rememberLazyGridState()
    val dragDropState = rememberGridDragDropState(gridState) { fromIndex, toIndex ->
        appViewModel.swapBookmark(fromIndex, toIndex)
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            // For UiAutomator
            // https://medium.com/androiddevelopers/accessing-composables-from-uiautomator-cf316515edc2
            .semantics {
                testTagsAsResourceId = true
            },
        topBar = {
            // Navbar
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
                },
                actions = {
                    Crossfade(targetState = editMode, label = "editIconCrossFade") { mode ->
                        // note that it's required to use the value passed by Crossfade
                        // instead of your state value
                        if (mode) {
                            IconButton(onClick = {
                                editMode = false
                            }, modifier = Modifier.testTag("DoneButton")) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Done Editing"
                                )
                            }
                        } else {
                            IconButton(onClick = {
                                addBookmarkSheet = true
                                editMode = false
                            }, modifier = Modifier.testTag("AddButton")) {
                                Icon(
                                    imageVector = Icons.Default.Add, contentDescription = "Add"
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                    ) {
                        IconButton(
                            onClick = { overFlowMenuExpanded = !overFlowMenuExpanded },
                            modifier = Modifier.testTag("overFlowMenu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert, contentDescription = "More"
                            )
                        }
                        // Overflow menu
                        DropdownMenu(
                            expanded = overFlowMenuExpanded,
                            onDismissRequest = { overFlowMenuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                        ) {
                            DropdownMenuItem(text = { Text("Edit") },
                                modifier = Modifier.testTag("editMenu"),
                                onClick = {
                                    editMode = true
                                    Toast.makeText(
                                        context,
                                        "Drag and drop to rearrange bookmarks",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    overFlowMenuExpanded = false
                                })
                            DropdownMenuItem(text = { Text("Settings") },
                                modifier = Modifier.testTag("settingMenu"),
                                onClick = {
                                    onNavigateToSettingsScreen()
                                    overFlowMenuExpanded = false
                                })
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        // The bookmark grid
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .dragContainer(dragDropState)
                        .testTag("BookmarkList"),
                    state = gridState,
                    contentPadding = PaddingValues(
                        start = 8.dp, top = 12.dp, end = 8.dp, bottom = 12.dp
                    ),
                ) {
                    itemsIndexed(items = bookmarksDTOList?.toList() ?: emptyList(),
                        key = { _, item -> item.id }) { index, item ->
                        DraggableItem(
                            dragDropState = dragDropState,
                            index = index,
                        ) { isDragging ->
                            val elevation by animateDpAsState(
                                if (isDragging) 2.dp else 0.dp, label = "dragAndDropAnimation"
                            )
                            Surface(
                                shadowElevation = elevation,
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        // When Eng. is clicked we need to show Eng RSS feed
                                        if (item.id == "newseng") {
                                            onNavigateToRssScreen.invoke()
                                        } else {
                                            try {
                                                val url = item.url
                                                val intent = CustomTabsIntent.Builder().build()
                                                intent.launchUrl(context, Uri.parse(url))
                                            } catch (e: ActivityNotFoundException) {
                                                // If Quercus app is not installed then the intent cannot be handled, causing exception,
                                                // In that case open Google Play to show Quercus app, so user can install it
                                                // In future if the json contains links to other apps and if not installed on device
                                                // you need to add another switch statement to select which Uri to parse
                                                context.startActivity(
                                                    Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse("market://details?id=${"com.instructure.candroid"}")
                                                    )
                                                )
                                            }
                                        }
                                    }) {
                                    Box(
                                        Modifier
                                            .padding(8.dp, 16.dp, 8.dp, 16.dp)
                                            .size(52.dp)
                                            .background(
                                                roundBookmarkBlue, CircleShape
                                            ),
                                    ) {

                                        AsyncImage(
                                            model = iconResourceMap[item.imageLocation],
                                            contentDescription = "University of Toronto Logo",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .height(32.dp)
                                                .align(Alignment.Center),
                                        )

                                        if (editMode && !appViewModel.isMandatory(item.id)) {
                                            IconButton(modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(red)
                                                .align(
                                                    Alignment.TopEnd
                                                )
                                                .size(8.dp), onClick = {
                                                appViewModel.removeBookmark(item.id)
                                            }

                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Close,
                                                    tint = Color.White,
                                                    contentDescription = "Remove Bookmark",
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        fontWeight = FontWeight.Medium,
                                        text = item.name,
                                        textAlign = TextAlign.Center,
                                        color = Color.DarkGray,
                                        softWrap = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // Shows instructions the first time app is installed or reset
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
                            "To get started, simply click the + symbol to access a list of bookmarks from UofT.",
                            color = Color.DarkGray
                        )
                        Row {
                            Spacer(Modifier.weight(1.0f))
                            Button(
                                modifier = Modifier.testTag("Startup Instructions Dismiss Button"),
                                onClick = { appViewModel.hideBookmarkInstructions() },
                                colors = ButtonDefaults.buttonColors(containerColor = roundBookmarkBlue)
                            ) {
                                Text(text = "Dismiss")
                            }
                        }
                    }
                }
            }

            // Bottom sheet that comes up when you press add
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
                                        // Setting max value for textField to 15
                                        if (it.length <= 15) appViewModel.searchQuery.value = it
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            BorderStroke(
                                                width = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            ), shape = RoundedCornerShape(50)
                                        )
                                        .padding(8.dp, 0.dp)
                                        .testTag("SearchField"),
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
                            // LazyVerticalGrid bottom sheet
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
                                                        model = iconResourceMap[jsonResponse?.apps!![item].imageLocalName.lowercase()],
                                                        contentDescription = "University of Toronto Logo",
                                                        contentScale = ContentScale.Fit,
                                                        modifier = Modifier.height(32.dp),
                                                    )
                                                    if (bookmarksDTOList?.any { item1 -> item1.id == jsonResponse.apps[item].id } == true) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(18.dp)
                                                                .clip(CircleShape)
                                                                .background(green)
                                                                .align(
                                                                    Alignment.TopEnd
                                                                )
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Filled.Done,
                                                                tint = Color.White,
                                                                contentDescription = "Remove Bookmark",
                                                            )
                                                        }
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

// For LazyVerticalGrid drag and drop
@Composable
fun rememberGridDragDropState(
    gridState: LazyGridState, onMove: (Int, Int) -> Unit
): GridDragDropState {
    val scope = rememberCoroutineScope()
    val state = remember(gridState) {
        GridDragDropState(
            state = gridState, onMove = onMove, scope = scope
        )
    }
    LaunchedEffect(state) {
        while (true) {
            val diff = state.scrollChannel.receive()
            gridState.scrollBy(diff)
        }
    }
    return state
}


class GridDragDropState internal constructor(
    private val state: LazyGridState,
    private val scope: CoroutineScope,
    private val onMove: (Int, Int) -> Unit
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    internal val scrollChannel = Channel<Float>()

    private var draggingItemDraggedDelta by mutableStateOf(Offset.Zero)
    private var draggingItemInitialOffset by mutableStateOf(Offset.Zero)
    internal val draggingItemOffset: Offset
        get() = draggingItemLayoutInfo?.let { item ->
            draggingItemInitialOffset + draggingItemDraggedDelta - item.offset.toOffset()
        } ?: Offset.Zero

    private val draggingItemLayoutInfo: LazyGridItemInfo?
        get() = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == draggingItemIndex }

    internal var previousIndexOfDraggedItem by mutableStateOf<Int?>(null)
        private set
    internal var previousItemOffset = Animatable(Offset.Zero, Offset.VectorConverter)
        private set

    internal fun onDragStart(offset: Offset) {
        state.layoutInfo.visibleItemsInfo.firstOrNull { item ->
            offset.x.toInt() in item.offset.x..item.offsetEnd.x && offset.y.toInt() in item.offset.y..item.offsetEnd.y
        }?.also {
            draggingItemIndex = it.index
            draggingItemInitialOffset = it.offset.toOffset()
        }
    }

    internal fun onDragInterrupted() {
        if (draggingItemIndex != null) {
            previousIndexOfDraggedItem = draggingItemIndex
            val startOffset = draggingItemOffset
            scope.launch {
                previousItemOffset.snapTo(startOffset)
                previousItemOffset.animateTo(
                    Offset.Zero, spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = Offset.VisibilityThreshold
                    )
                )
                previousIndexOfDraggedItem = null
            }
        }
        draggingItemDraggedDelta = Offset.Zero
        draggingItemIndex = null
        draggingItemInitialOffset = Offset.Zero
    }

    internal fun onDrag(offset: Offset) {
        draggingItemDraggedDelta += offset

        val draggingItem = draggingItemLayoutInfo ?: return
        val startOffset = draggingItem.offset.toOffset() + draggingItemOffset
        val endOffset = startOffset + draggingItem.size.toSize()
        val middleOffset = startOffset + (endOffset - startOffset) / 2f

        val targetItem = state.layoutInfo.visibleItemsInfo.find { item ->
            middleOffset.x.toInt() in item.offset.x..item.offsetEnd.x && middleOffset.y.toInt() in item.offset.y..item.offsetEnd.y && draggingItem.index != item.index
        }
        if (targetItem != null) {
            if (draggingItem.index == state.firstVisibleItemIndex || targetItem.index == state.firstVisibleItemIndex) {
                state.requestScrollToItem(
                    state.firstVisibleItemIndex, state.firstVisibleItemScrollOffset
                )
            }
            onMove.invoke(draggingItem.index, targetItem.index)
            draggingItemIndex = targetItem.index
        } else {
            val overscroll = when {
                draggingItemDraggedDelta.y > 0 -> (endOffset.y - state.layoutInfo.viewportEndOffset).coerceAtLeast(
                    0f
                )

                draggingItemDraggedDelta.y < 0 -> (startOffset.y - state.layoutInfo.viewportStartOffset).coerceAtMost(
                    0f
                )

                else -> 0f
            }
            if (overscroll != 0f) {
                scrollChannel.trySend(overscroll)
            }
        }
    }

    private val LazyGridItemInfo.offsetEnd: IntOffset
        get() = this.offset + this.size
}

private operator fun IntOffset.plus(size: IntSize): IntOffset {
    return IntOffset(x + size.width, y + size.height)
}

private operator fun Offset.plus(size: Size): Offset {
    return Offset(x + size.width, y + size.height)
}

fun Modifier.dragContainer(dragDropState: GridDragDropState): Modifier {
    return pointerInput(dragDropState) {
        detectDragGesturesAfterLongPress(onDrag = { change, offset ->
            change.consume()
            dragDropState.onDrag(offset = offset)
        },
            onDragStart = { offset -> dragDropState.onDragStart(offset) },
            onDragEnd = { dragDropState.onDragInterrupted() },
            onDragCancel = { dragDropState.onDragInterrupted() })
    }
}

@Composable
fun LazyGridItemScope.DraggableItem(
    dragDropState: GridDragDropState,
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable (isDragging: Boolean) -> Unit
) {
    val dragging = index == dragDropState.draggingItemIndex
    val draggingModifier = if (dragging) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationX = dragDropState.draggingItemOffset.x
                translationY = dragDropState.draggingItemOffset.y
            }
    } else if (index == dragDropState.previousIndexOfDraggedItem) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationX = dragDropState.previousItemOffset.value.x
                translationY = dragDropState.previousItemOffset.value.y
            }
    } else {
        Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
    }
    Box(modifier = modifier.then(draggingModifier), propagateMinConstraints = true) {
        content(dragging)
    }
}