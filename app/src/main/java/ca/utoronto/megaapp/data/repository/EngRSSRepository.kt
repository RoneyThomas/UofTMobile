package ca.utoronto.megaapp.data.repository

import androidx.annotation.WorkerThread
import com.prof18.rssparser.RssParserBuilder
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient

// Used the Eng News RSS page
class EngRSSRepository(client: OkHttpClient) {
    private val builder = RssParserBuilder(
        callFactory = client
    )
    private val rssParser = builder.build()

    @WorkerThread
    suspend fun rssChannel(): RssChannel? = try {
        coroutineScope {
            rssParser.getRssChannel("https://news.engineering.utoronto.ca/feed/")
        }
    } catch (e: Exception) {
        println("Error in fetching RSS Channel: $e")
        null
    }
}