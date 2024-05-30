package ca.utoronto.megaapp.ui.screens.homeScreen

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.utoronto.megaapp.ui.BookmarkDTO
import ca.utoronto.megaapp.ui.screens.AppViewModel
import ca.utoronto.megaapp.ui.theme.roundBookmarkBlue
import coil.compose.AsyncImage
import kotlin.reflect.KFunction1

class AppAdapter(
    onNavigateToRssScreen: () -> Unit, removeApp: KFunction1<String, Unit>,
    private val appViewModel: AppViewModel
) :
    ListAdapter<BookmarkDTO, AppAdapter.AppViewHolder>(AppDiffCallback) {
    private val navigate = onNavigateToRssScreen
    private val removeApp = removeApp

    /* ViewHolder for Flower, takes in the inflated view and the onClick behavior. */
    class AppViewHolder(private val composeView: ComposeView) :
        RecyclerView.ViewHolder(composeView) {
        private var currentApp: BookmarkDTO? = null

        fun bind(
            app: BookmarkDTO,
            onNavigateToRssScreen: () -> Unit,
            removeApp: KFunction1<String, Unit>
        ) {
            currentApp = app
            composeView.setContent {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            if (app.id == "newseng") {
                                onNavigateToRssScreen.invoke()
                            } else {
                                val url = app.url
                                val intent = CustomTabsIntent.Builder().build()
                                intent.launchUrl(composeView.context, Uri.parse(url))
                            }
                        }) {
                    Box(
                        Modifier
                            .padding(8.dp, 16.dp, 8.dp, 16.dp)
                            .size(52.dp)
                            .background(
                                roundBookmarkBlue,
                                CircleShape
//                                RoundedCornerShape(30.dp)
                            ),
//                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = composeView.context.resources.getIdentifier(
                                app.imageLocalName.lowercase(),
                                "drawable",
                                composeView.context.packageName
                            ),
                            contentDescription = "University of Toronto Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(32.dp)
                                .align(Alignment.Center),
                        )

                        if (app.showRemoveIcon) {
                            IconButton(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .align(
                                        Alignment.TopEnd
                                    )
                                    .size(8.dp), onClick = {
                                    removeApp(app.id)
                                }

                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    tint = Color.White,
                                    contentDescription = "Remove Bookmark",
                                )
                            }

//                                AsyncImage(model = R.drawable.minus,
//                                    contentDescription = "Remove Button",
//                                    modifier = Modifier
//                                        .clickable {
//                                            Log.d(
//                                                "Remove Button",
//                                                "CenterAlignedTopAppBarExample: " + app.id
//                                            )
//                                            removeApp(app.id)
////                                    if ((appViewModel.bookmarks.value?.size
////                                            ?: 0) <= 0
////                                    ) {
////                                        showRemoveIcon = false
////                                    }
////                                    appViewModel.removeBookmark(app.id)
//                                        })
                        }
//                        if (showRemoveIcon && !jsonResponse!!.mandatoryApps.contains(
//                                app.id
//                            )
//                        ) {
//                            AsyncImage(model = R.drawable.minus,
//                                contentDescription = "Remove Button",
//                                modifier = Modifier.clickable {
//                                    Log.d(
//                                        "Remove Button",
//                                        "CenterAlignedTopAppBarExample: " + app.id
//                                    )
//                                    if ((appViewModel.bookmarks.value?.size
//                                            ?: 0) <= 0
//                                    ) {
//                                        showRemoveIcon = false
//                                    }
//                                    appViewModel.removeBookmark(app.id)
//                                })
//                        }
                    }
                    Text(
                        fontWeight = FontWeight.Medium,
//                        fontFamily = Bembo,
//                        fontWeight = FontWeight.Bold,
                        text = app.name,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
//                        style = ,
//                        style = TextStyle(lineBreak = LineBreak.Heading),
                        softWrap = true
//                            .copy(
//                            shadow = Shadow(
//                                color = Color.White,
//                                offset = Offset(2f, 2f),
//                                blurRadius = 8f
//                            )
//                        )
                    )
                }
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): AppViewHolder {
        return AppViewHolder(ComposeView(parent.context))
    }

    /* Gets current flower and uses it to bind view. */
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = getItem(position)
        holder.bind(app, navigate, removeApp)
    }

    fun moveItem(from: Int, to: Int) {
        Log.d("AppAdapter", "moveItem: $from to $to")
        appViewModel.swapBookmark(from, to)
    }
}

object AppDiffCallback : DiffUtil.ItemCallback<BookmarkDTO>() {
    override fun areItemsTheSame(oldItem: BookmarkDTO, newItem: BookmarkDTO): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BookmarkDTO, newItem: BookmarkDTO): Boolean {
        return oldItem == newItem
    }
}