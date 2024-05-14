package ca.utoronto.megaapp.data.repository

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


class UofTMobileRepository() {

    private val tag: String = "SitesRepository"
    private val client = OkHttpClient()
    private var result: MutableLiveData<UofTMobile> = MutableLiveData<UofTMobile>()

    //    private val client = OkHttpClient.Builder().cache(
//        Cache(
//            directory = File(application.cacheDir, "http_cache"),
//            // $0.05 worth of phone storage in 2020
//            maxSize = 50L * 1024L * 1024L // 50 MiB
//        )
//    )
    init {
        val request = Request.Builder()
//            .url("https://uoft-mobile.s3.ca-central-1.amazonaws.com/UofTMobile.JSON")
            .url("http://10.0.1.2:8000/UofTMobile.JSON")
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                when (e) {
                    is UnknownHostException -> Log.d(tag, "onFailure: Unknown host!")
                    is ConnectException -> Log.d(tag, "onFailure: No internet!")
                    else -> e.printStackTrace()
                    //TODO read from asset JSON files
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

//                    for ((name, value) in response.headers) {
//                        println("$name: $value")
//                    }
                    val jsonResponse: UofTMobile =
                        Json.decodeFromString<UofTMobile>(response.body!!.string())
                    result.postValue(jsonResponse)
                }
            }
        })
    }


    public fun getResult(): MutableLiveData<UofTMobile> {
        return result
    }
}