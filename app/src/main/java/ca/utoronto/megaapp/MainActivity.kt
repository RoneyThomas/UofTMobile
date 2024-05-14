package ca.utoronto.megaapp

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.MutableLiveData
import ca.utoronto.megaapp.data.entities.UofTMobile
import ca.utoronto.megaapp.data.repository.UofTMobileRepository
import coil.compose.AsyncImage
import com.example.compose.UofTMobileTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UofTMobileTheme {
                CenterAlignedTopAppBarExample(UofTMobileRepository().getResult())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterAlignedTopAppBarExample(result: MutableLiveData<UofTMobile>) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val list = (1..8).map { it.toString() }
    val context = LocalContext.current
    val jsonResponse: State<UofTMobile?> = result.observeAsState(
        initial = Json.decodeFromString(
            context.assets.open("UofTMobile.json").bufferedReader()
                .use { it.readText() }) as UofTMobile
    )
    var mandaotryApps = jsonResponse.value?.mandatoryApps?.toSet()
//    var urlToDisplay = jsonResponse.value?.apps?.filter { it.id in mandaotryApps }


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
//                scrollBehavior = scrollBehavior,
            )
        }, bottomBar = {
            var selectedItem by remember { mutableIntStateOf(0) }
            NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
                NavigationBarItem(icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
                    label = { Text("Add") },
                    selected = false,
                    colors = navItemColor,
                    onClick = {
                        selectedItem = 0;
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
                    jsonResponse.value?.apps?.size?.let { it ->
                        items(it) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    Log.d(
                                        "MainActivity",
                                        "CenterAlignedTopAppBarExample: I am clicked"
                                    )
                                    val url = jsonResponse.value?.apps!![it].url
                                    val intent = CustomTabsIntent.Builder()
                                        .build()
                                    intent.launchUrl(context, Uri.parse(url))
                                }
                            ) {
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
                                            jsonResponse.value?.apps!![it].imageLocalName,
                                            "drawable",
                                            context.packageName
                                        ),
                                        contentDescription = "University of Toronto Logo",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.height(48.dp)
                                    )
                                }
                                Text(
                                    text = jsonResponse.value?.apps!![it].name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            )


            AsyncImage(
                model = R.drawable.background, contentDescription = "UofT Logo", modifier = Modifier
                    .align(Alignment.Center)
                    .height(256.dp)
            )


            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    }, sheetState = sheetState
                ) {
                    // Sheet content
                    Column(modifier = Modifier.padding(8.dp, 0.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp, 0.dp)) {
                            Button(onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                            }) {
                                Text("Done")
                            }
                            var text by remember { mutableStateOf(TextFieldValue("")) }
                            OutlinedTextField(
                                value = text,
                                onValueChange = { newText ->
                                    text = newText
                                },
                                modifier = Modifier
                                    .padding(
                                        8.dp, 0.dp
                                    )
                                    .weight(1f)
//                                    .height(36.dp)
                            )
                            Button(onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                            }) {
                                Text("About")
                            }
                        }
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
                                jsonResponse.value?.apps?.size?.let { it ->
                                    items(it) {
                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable {

                                            }
                                        ) {
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
                                                        jsonResponse.value?.apps!![it].imageLocalName,
                                                        "drawable",
                                                        context.packageName
                                                    ),
                                                    contentDescription = "University of Toronto Logo",
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.height(48.dp)
                                                )
                                            }
                                            Text(
                                                text = jsonResponse.value?.apps!![it].name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.Black,
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }


    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    UofTMobileTheme {
//        CenterAlignedTopAppBarExample()
//    }
//}