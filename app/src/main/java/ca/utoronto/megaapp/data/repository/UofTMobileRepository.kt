package ca.utoronto.megaapp.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.MutableLiveData
import ca.utoronto.megaapp.R
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

// Repository responsible for getting json data from s3
class UofTMobileRepository(val context: Context, private val client: OkHttpClient) {

    private val tag: String = "SitesRepository"

    // Could change it to StateFlow, but that is for another day
    var result: MutableLiveData<UofTMobile> = MutableLiveData<UofTMobile>()

    private val request: Request = Request.Builder()
        .url(context.resources.getString(R.string.jsonURL)).build()

    fun loadApps() {
        if (isOnline(context)) {
            loadLocalJson(result)
        }
        client.newCall(request).enqueue(object : Callback {
            // On fail, loads json from assets folder
            override fun onFailure(call: Call, e: IOException) {
                when (e) {
                    is UnknownHostException -> Log.d(tag, "onFailure: Unknown host!")
                    is ConnectException -> Log.d(tag, "onFailure: No internet!")
                    else -> e.printStackTrace()
                }
                loadLocalJson(result)
            }

            // Successful HTTP request
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    try {
                        val jsonResponse: UofTMobile =
                            Json.decodeFromString<UofTMobile>(response.body!!.string())
                        result.postValue(jsonResponse)
                    } catch (e: Exception) {
                        loadLocalJson(result)
                    }
                }
            }
        })
    }

    private fun loadLocalJson(result: MutableLiveData<UofTMobile>) {
        result.postValue(
            Json.decodeFromString(context.assets.open("UofTMobile.json").bufferedReader()
                .use { it.readText() }) as UofTMobile
        )
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }
}