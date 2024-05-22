package ca.utoronto.megaapp.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import ca.utoronto.megaapp.data.entities.UofTMobile
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.net.ConnectException
import java.net.UnknownHostException


class UofTMobileRepository(val context: Context, private val client: OkHttpClient) {

    private val tag: String = "SitesRepository"

    var result: MutableLiveData<UofTMobile> = MutableLiveData<UofTMobile>()

    private val request: Request = Request.Builder()
        .url("https://uoft-mobile.s3.ca-central-1.amazonaws.com/UofTMobile.JSON").build()

    fun loadApps() {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                when (e) {
                    is UnknownHostException -> Log.d(tag, "onFailure: Unknown host!")
                    is ConnectException -> Log.d(tag, "onFailure: No internet!")
                    else -> e.printStackTrace()
                }
                // Loads local JSON when network request fails
                result.postValue(
                    Json.decodeFromString(context.assets.open("UofTMobile.json").bufferedReader()
                        .use { it.readText() }) as UofTMobile
                )
            }

            // Successful HTTP request
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val jsonResponse: UofTMobile =
                        Json.decodeFromString<UofTMobile>(response.body!!.string())
                    result.postValue(jsonResponse)
                }
            }
        })
    }
}