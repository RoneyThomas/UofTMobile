package ca.utoronto.megaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.compose.UofTMobileTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UofTMobileTheme {
                CenterAlignedTopAppBarExample()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterAlignedTopAppBarExample() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val list = (1..8).map { it.toString() }

    val navItemColor = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        indicatorColor = Color.Transparent,
        unselectedIconColor = Color.White,
        unselectedTextColor = Color.White,
    )
    Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
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
                scrollBehavior = scrollBehavior,
            )
        }, bottomBar = {
            var selectedItem by remember { mutableIntStateOf(0) }
            NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
                NavigationBarItem(icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
                    label = { Text("Add") },
                    selected = false,
                    colors = navItemColor,
                    onClick = {
//                        selectedItem = 0;
                        showBottomSheet = true
                    })
                NavigationBarItem(icon = { Icon(Icons.Filled.Edit, contentDescription = "Edit") },
                    label = { Text("Edit") },
                    selected = false,
                    colors = navItemColor,
                    onClick = { selectedItem = 1 })
            }
        }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxHeight()
                .background(color = Color(0xFFD0D1CB))
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(80.dp),
                Modifier.zIndex(1f),
                // content padding
                contentPadding = PaddingValues(
                    start = 8.dp,
                    top = 12.dp,
                    end = 8.dp,
                    bottom = 12.dp
                ),
                content = {
                    items(list.size) { index ->
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier
                                    .padding(16.dp, 16.dp, 16.dp, 8.dp)
                                    .size(64.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(4.dp)
                                    ), contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = R.drawable.portal,
                                    contentDescription = "University of Toronto Logo",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.height(48.dp)
                                )
                            }
                            Text(
                                text = list[index],
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.Black,
//                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            )

            AsyncImage(model = R.drawable.background, contentDescription = "UofT Logo", modifier = Modifier.align(Alignment.Center).height(256.dp))


            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    }, sheetState = sheetState
                ) {
                    // Sheet content
                    Button(onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }) {
                        Text("Hide bottom sheet")
                    }
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = """
                    This is an example of a scaffold. It uses the Scaffold composable's parameters to create a screen with a simple top app bar, bottom app bar, and floating action button.

                    It also contains some basic inner content, such as this text.

                    You have pressed the floating action button 6 times.
                """.trimIndent(),
                    )
                }
            }
        }


    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UofTMobileTheme {
        CenterAlignedTopAppBarExample()
    }
}