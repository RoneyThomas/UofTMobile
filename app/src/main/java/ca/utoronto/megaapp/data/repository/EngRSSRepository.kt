package ca.utoronto.megaapp.data.repository

import androidx.annotation.WorkerThread
import com.prof18.rssparser.RssParserBuilder
import com.prof18.rssparser.model.RssChannel
import okhttp3.OkHttpClient

class EngRSSRepository(client: OkHttpClient) {
    private val builder = RssParserBuilder(
        callFactory = OkHttpClient()
    )
    private val rssParser = builder.build()

    @WorkerThread
    suspend fun rssChannel(): RssChannel =
        rssParser.getRssChannel("https://news.engineering.utoronto.ca/feed/")
}