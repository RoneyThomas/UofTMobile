package ca.utoronto.megaapp.data.repository

import androidx.annotation.WorkerThread
import com.prof18.rssparser.RssParserBuilder
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient

// Repository responsible for getting Eng News RSS feed
class RSSRepository(client: OkHttpClient) {
    private val builder = RssParserBuilder(
        callFactory = client
    )
    private val rssParser = builder.build()

    @WorkerThread
    suspend fun rssChannel(rssUrl: String): RssChannel? = try {
        coroutineScope {
            rssParser.getRssChannel(rssUrl)
        }
    } catch (e: Exception) {
        println("Error in fetching RSS Channel: $e")
        null
    }
}