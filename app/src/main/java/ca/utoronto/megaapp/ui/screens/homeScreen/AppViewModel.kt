package ca.utoronto.megaapp.ui.screens.homeScreen

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.preference.PreferenceManager
import ca.utoronto.megaapp.data.entities.App
import ca.utoronto.megaapp.data.entities.Section
import ca.utoronto.megaapp.data.entities.UofTMobile
import ca.utoronto.megaapp.data.repository.EngRSSRepository
import ca.utoronto.megaapp.data.repository.UofTMobileRepository
import ca.utoronto.megaapp.ui.SectionsDTO
import com.prof18.rssparser.model.RssChannel
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

class AppViewModel(application: Application) : AndroidViewModel(application) {

    // Creates OkHttpClient with http caching
    val client: OkHttpClient = OkHttpClient.Builder().cache(
        Cache(
            directory = File(application.cacheDir, "http_cache"),
            maxSize = 5L * 1024L * 1024L // 5 MiB
        )
    ).build()

    private val uofTMobileRepository: UofTMobileRepository =
        UofTMobileRepository(application, client)
    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(getApplication())

    val jsonResponse: MutableLiveData<UofTMobile> = uofTMobileRepository.result

    var bookmarks = MutableLiveData<List<String>>()

    private lateinit var rssFeed: LiveData<RssChannel>


    // Creates DTO from jsonResponse
    fun sections(): LiveData<List<SectionsDTO>> = jsonResponse.switchMap { response ->
        run {
            val sectionsDTOList: MutableList<SectionsDTO> = mutableListOf()
            response.sections.forEach { section: Section ->
                sectionsDTOList.add(
                    SectionsDTO(
                        section.index,
                        section.name,
                        mutableListOf(),
                        mutableListOf()
                    )
                )
            }

            response.apps.forEachIndexed { index, app: App ->
                run {
                    sectionsDTOList[app.sectionIndex.toInt()].apps.add(index)
                }
            }

            // Loads bookmark
            if (bookmarks.value == null) {
                val sharedPref = sharedPreferences.getString("bookmarks", "")
                if (!sharedPref.isNullOrEmpty()) {
                    bookmarks.postValue(sharedPref.split(",").toList())
                } else {
                    bookmarks.postValue(response.mandatoryApps)
                    savePreference(response.mandatoryApps)
                }
            }

            val sections: LiveData<List<SectionsDTO>> by lazy {
                MutableLiveData(sectionsDTOList)
            }
            return@switchMap sections
        }
    }

    fun addBookmark(id: String) {
        if (bookmarks.value?.contains(id) == false) {
            val updateList = bookmarks.value!!.toMutableList()
            updateList.add(id)
            bookmarks.value = updateList
            savePreference(updateList)
        } else {
            removeBookmark(id)
        }
    }

    private fun savePreference(updateList: List<String>?) {
        if (updateList != null) {
            sharedPreferences.edit()
                .putString("bookmarks", updateList.joinToString(separator = ",")).apply()
        }
    }

    fun removeBookmark(id: String) {
        val updateList = bookmarks.value!!.toMutableList()
        updateList.remove(id)
        bookmarks.value = updateList
        savePreference(updateList)
    }

    fun swapBookmark(id1: String, id2: String) {
        Log.d("AppViewModel", "i1: $id1, i2: $id2")
        Log.d("AppViewModel", "${bookmarks.value?.toMutableList()}")
        val updateList = bookmarks.value!!.toMutableList()
        Log.d("AppViewModel", updateList.toString())
        val i1 = updateList.indexOf(id1)
        val i2 = updateList.indexOf(id2)
        updateList.removeAt(i2)
        updateList.add(i1, id2)
        bookmarks.value = updateList
        Log.d("AppViewModel", "${bookmarks.value?.toMutableList()}")
        savePreference(updateList)
    }

    fun getAppById(id: String): App? {
        return jsonResponse.value?.apps?.single { app: App -> app.id == id }
    }

    private fun getAppIndex(id: String): Int {
        jsonResponse.value?.apps?.forEachIndexed { index, app ->
            if (app.id == id) return index
        }
        return -1
    }

    fun getRssFeed(): LiveData<RssChannel> {
        rssFeed = liveData {
            val data = EngRSSRepository(client).rssChannel()
            emit(data)
        }
        return rssFeed
    }

    private fun <T> MutableList<T>.swap(id1: Int, id2: Int): MutableList<T> = apply {
        val t = this[id1]
        this[id1] = this[id2]
        this[id2] = t
    }

//    fun searchApps(query: String) {
//        val searchSectionsDTO: MutableList<SectionsDTO> = mutableListOf()
//        jsonResponse.value?.apps?.forEach {
//            if (it.name.contains(query, ignoreCase = true)){
//                searchSectionsDTO.add()
//            }
//        }
//    }
}