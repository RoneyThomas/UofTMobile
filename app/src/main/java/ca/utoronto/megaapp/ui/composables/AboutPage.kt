package ca.utoronto.megaapp.ui.composables

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.utoronto.megaapp.ui.screens.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AboutPage @OptIn(ExperimentalMaterial3Api::class) constructor
    (var changeBottomSheet: (Boolean) -> Unit, var aboutSheetState: SheetState,
     var scope: CoroutineScope, var context: Context, var appViewModel: AppViewModel){

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AboutPageMain() {
        ModalBottomSheet(
            onDismissRequest = {
                changeBottomSheet(false)
            }, sheetState = aboutSheetState
        ) {
            // Sheet content
            Column(modifier = Modifier.padding(12.dp, 8.dp)) {
                AboutPageButton(onClickEffect = {
                    scope.launch { aboutSheetState.hide() }.invokeOnCompletion {
                        if (!aboutSheetState.isVisible) {
                            changeBottomSheet(false)
                        }
                    }
                }, text = "Done", alignment = Alignment.Start)

                AboutPageSection(mainText = "Feedback",
                    subText = "Have any comments or suggestions on the content or layout of U of T Mobile? " +
                            "We'd love to hear it!")

                AboutPageButton(onClickEffect = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:") // Only email apps handle this.
                        putExtra(Intent.EXTRA_EMAIL, "mad.lab@utoronto.ca")
                        putExtra(
                            Intent.EXTRA_SUBJECT, "UofT Mobile Feedback (v3.0, 4)"
                        )
                    }
                    context.startActivity(intent)
                }, text = "Submit Feedback")

                AboutPageSection(mainText = "Version", subText = "Version 3.0, Build 1")

                AboutPageSection(mainText = "Settings", subText = "")

                AboutPageButton(onClickEffect = { appViewModel.resetBookmarks() },
                    text = "Reset U of T Mobile")

                AboutPageButton(onClickEffect = { appViewModel.refresh() },
                    text = "Refresh Index")

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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AboutPageButton(onClickEffect: () -> Unit, text: String,
                        alignment: Alignment.Horizontal = Alignment.CenterHorizontally){
        Column(modifier = Modifier.fillMaxWidth()){
            OutlinedButton(onClick = onClickEffect,
                modifier = Modifier.align(alignment).padding(vertical = 8.dp)) {
                Text(text)
            }
        }
    }

    @Composable
    fun AboutPageSection(mainText: String, subText: String){
        Text(
            text = mainText,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)
        )
        if (subText.isNotEmpty())
            Text(text = subText, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth())
    }
}
