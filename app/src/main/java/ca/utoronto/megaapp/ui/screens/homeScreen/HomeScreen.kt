package ca.utoronto.megaapp.ui.screens.homeScreen

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import ca.utoronto.megaapp.R
import ca.utoronto.megaapp.ui.screens.AppViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalGlideComposeApi::class
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
    var showRemoveIcon by remember { mutableStateOf(false) }
    val refresh = appViewModel.refresh.observeAsState().value
    val bookmarks = appViewModel.bookmarks.observeAsState().value
    val searchQuery = appViewModel.searchQuery.observeAsState().value
    val searchSections = appViewModel.filteredSections().observeAsState().value
    val showBookmarkInstructions = appViewModel.showBookmarkInstructions.observeAsState()
    val jsonResponse = appViewModel.jsonResponse.value

    val gridState = rememberLazyGridState()
//    val view = LocalView.current

    val dragDropState = rememberGridDragDropState(gridState) { fromIndex, toIndex ->
        appViewModel.swapBookmark(toIndex, fromIndex)
//        list = list.toMutableList().apply {
//            add(toIndex, removeAt(fromIndex))
//        }
    }

//    val dragDropState = rememberLazyGridState(gridState) { from, to ->
//        // Update the list
//        Log.d("HomeScreen", "HomeScreen: ${from.index}, ${to.index}")
//        appViewModel.swapBookmark(to.index, from.index)
////        if (Build.VERSION.SDK_INT >= 34) {
////            view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
////        }
//    }
    val context = LocalContext.current

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
                GlideImage(
                    model = R.drawable.uoftcrst_stacked_white_use_only_on_655,
                    contentDescription = "University of Toronto Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(48.dp)
                ) {
                    it.diskCacheStrategy(DiskCacheStrategy.ALL)
                }
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
        PullToRefreshBox(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(
                    color = Color(0xFFD0D1C9)
                )
                .paint(
                    painterResource(id = R.drawable.background), contentScale = ContentScale.Fit
                ),
            isRefreshing = refresh ?: false,
            onRefresh = {
                appViewModel.refresh()
            },
        ) {
            LazyVerticalGrid(columns = GridCells.Fixed(4),
                modifier = Modifier.dragContainer(dragDropState),
                state = gridState,
//                contentPadding = PaddingValues(16.dp),
//                verticalArrangement = Arrangement.spacedBy(16.dp),
//                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    start = 8.dp, top = 12.dp, end = 8.dp, bottom = 12.dp
                ),
                content = {
                    itemsIndexed(items = bookmarks?.toList() ?: emptyList(),
                        key = { _, item -> item }) { index, item ->
                        DraggableItem(
                            dragDropState, index
                        ) { isDragging ->
                            val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                            val app = appViewModel.getAppById(item)
                            if (app != null) {
                                Surface(
                                    shadowElevation = elevation, color = Color.Transparent
                                ) {
                                    Column(verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable {
                                            if (app.id == "newseng") {
                                                onNavigateToRssScreen.invoke()
                                            } else {
                                                val url = app.url
                                                val intent = CustomTabsIntent.Builder().build()
                                                intent.launchUrl(context, Uri.parse(url))
                                            }
                                        }) {
                                        Box(
                                            Modifier
                                                .padding(16.dp, 16.dp, 16.dp, 8.dp)
                                                .size(64.dp)
//                                                .draggableHandle(onDragStarted = {
//                                                    view.performHapticFeedback(
//                                                        HapticFeedbackConstants.DRAG_START
//                                                    )
//                                                }, onDragStopped = {
//                                                    view.performHapticFeedback(
//                                                        HapticFeedbackConstants.GESTURE_END
//                                                    )
//                                                })
                                                .background(
                                                    MaterialTheme.colorScheme.primary,
                                                    RoundedCornerShape(8.dp)
                                                ),
//                                            .border(2.dp, tintColor, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            GlideImage(
                                                model = context.resources.getIdentifier(
                                                    app.imageLocalName.lowercase(),
                                                    "drawable",
                                                    context.packageName
                                                ),
                                                contentDescription = "University of Toronto Logo",
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.height(48.dp),
                                            ) {
                                                it.diskCacheStrategy(DiskCacheStrategy.ALL)
                                            }
                                            if (showRemoveIcon && !jsonResponse!!.mandatoryApps.contains(
                                                    app.id
                                                )
                                            ) {
                                                GlideImage(model = R.drawable.minus,
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
                                                    blurRadius = 8f
                                                )
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
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
                                                        GlideImage(
                                                            model = context.resources.getIdentifier(
                                                                jsonResponse?.apps!![item].imageLocalName.lowercase(),
                                                                "drawable",
                                                                context.packageName
                                                            ),
                                                            contentDescription = "University of Toronto Logo",
                                                            contentScale = ContentScale.Fit,
                                                            modifier = Modifier.height(48.dp)
                                                        ) {
                                                            it.diskCacheStrategy(DiskCacheStrategy.ALL)
                                                        }
                                                        if (bookmarks?.contains(
                                                                jsonResponse.apps[item].id
                                                            ) == true
                                                        ) {
                                                            GlideImage(
                                                                model = R.drawable.checkmark,
                                                                contentDescription = "Selected"
                                                            ) {
                                                                it.diskCacheStrategy(
                                                                    DiskCacheStrategy.ALL
                                                                )
                                                            }
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
                        GlideImage(
                            model = R.drawable.background,
                            contentDescription = "UofT Logo",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .height(256.dp)
                        ) {
                            it.diskCacheStrategy(DiskCacheStrategy.ALL)
                        }
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
                            appViewModel.refresh()
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