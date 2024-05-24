package ca.utoronto.megaapp.ui.screens

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import ca.utoronto.megaapp.data.entities.App
import ca.utoronto.megaapp.data.entities.UofTMobile
import ca.utoronto.megaapp.data.repository.EngRSSRepository
import ca.utoronto.megaapp.data.repository.UofTMobileRepository
import ca.utoronto.megaapp.ui.SectionsDTO
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

class AppViewModel(application: Application) : AndroidViewModel(application) {

    // Creates OkHttpClient with http caching
    private val client: OkHttpClient = OkHttpClient.Builder().cache(
        Cache(
            directory = File(application.cacheDir, "http_cache"),
            maxSize = 5L * 1024L * 1024L // 5 MiB
        )
    ).build()

    private val uofTMobileRepository: UofTMobileRepository =
        UofTMobileRepository(application, client)
    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(getApplication())

    val showBookmarkInstructions = MutableLiveData(false)

    val jsonResponse: MutableLiveData<UofTMobile> = uofTMobileRepository.result

    var bookmarks = MutableLiveData<List<String>>()

    var searchQuery = MutableLiveData("")
    var refresh = MutableLiveData(false)

    private lateinit var rssFeed: LiveData<RssChannel>

    init {
        loadApps()
    }

    private fun loadApps() {
        uofTMobileRepository.loadApps()
        showBookmarkInstructions.value =
            sharedPreferences.getBoolean("showBookmarkInstructions", true)
    }

    fun refresh() {
        refresh.value = true
        loadApps()
        viewModelScope.launch {
            delay(400)
            refresh.value = false
        }
    }

    // Creates DTO from jsonResponse
    private fun sections(): LiveData<Map<String, SectionsDTO>> =
        jsonResponse.switchMap { response ->
            run {
                val sectionsDTOList: MutableMap<String, SectionsDTO> =
                    emptyMap<String, SectionsDTO>().toMutableMap()
                jsonResponse.value?.apps?.forEachIndexed { index, app ->
                    run {
                        if (!sectionsDTOList.contains(jsonResponse.value!!.sections[app.sectionIndex.toInt()].name)) {
                            sectionsDTOList[jsonResponse.value!!.sections[app.sectionIndex.toInt()].name] =
                                SectionsDTO(
                                    jsonResponse.value!!.sections[app.sectionIndex.toInt()].index,
                                    jsonResponse.value!!.sections[app.sectionIndex.toInt()].name,
                                    mutableListOf(index),
                                    mutableListOf()
                                )
                        } else {
                            sectionsDTOList[jsonResponse.value!!.sections[app.sectionIndex.toInt()].name]?.apps?.add(
                                index
                            )
                        }
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
                return@switchMap MutableLiveData(sectionsDTOList)
            }
        }

    fun filteredSections(): LiveData<Map<String, SectionsDTO>> = searchQuery.switchMap { query ->
        val sectionsDTOList: MutableMap<String, SectionsDTO> =
            emptyMap<String, SectionsDTO>().toMutableMap()
        if (query.isNotBlank()) {
            jsonResponse.value?.apps?.forEachIndexed { index, app ->
                run {
                    if (app.name.contains(query, ignoreCase = true)) {
                        if (!sectionsDTOList.contains(jsonResponse.value!!.sections[app.sectionIndex.toInt()].name)) {
                            sectionsDTOList[jsonResponse.value!!.sections[app.sectionIndex.toInt()].name] =
                                SectionsDTO(
                                    jsonResponse.value!!.sections[app.sectionIndex.toInt()].index,
                                    jsonResponse.value!!.sections[app.sectionIndex.toInt()].name,
                                    mutableListOf(index),
                                    mutableListOf()
                                )
                        } else {
                            sectionsDTOList[jsonResponse.value!!.sections[app.sectionIndex.toInt()].name]?.apps?.add(
                                index
                            )
                        }
                    }
                }
            }
            return@switchMap MutableLiveData(sectionsDTOList)
        }
        return@switchMap sections()
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

    fun resetBookmarks() {
        client.cache?.evictAll()
        bookmarks.value = jsonResponse.value?.mandatoryApps
        savePreference(jsonResponse.value?.mandatoryApps)
        showBookmarkInstructions.value = true
        sharedPreferences.edit()
            .putBoolean("showBookmarkInstructions", true).apply()
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

    fun hideBookmarkInstructions() {
        showBookmarkInstructions.value = false
        sharedPreferences.edit()
            .putBoolean("showBookmarkInstructions", false).apply()
    }

    fun getRssFeed(): LiveData<RssChannel> {
        rssFeed = liveData {
            val data = EngRSSRepository(client).rssChannel()
            if (data != null) {
                emit(data)
            }
        }
        return rssFeed
    }
}