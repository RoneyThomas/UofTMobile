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
import ca.utoronto.megaapp.ui.BookmarkDTO
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

    var bookmarksDTOList = MutableLiveData<List<BookmarkDTO>>()

    var searchQuery = MutableLiveData("")
    var refresh = MutableLiveData(false)
    var updateList: MutableList<BookmarkDTO> = mutableListOf()

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
        jsonResponse.switchMap {
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
                if (bookmarksDTOList.value == null) {
                    val sharedPref = sharedPreferences.getString("bookmarks", "")
                    if (!sharedPref.isNullOrEmpty()) {
                        val bookmarkList = sharedPref.split(",").toList()
                        updateList =
                            jsonResponse.value?.apps?.filter { bookmarkList.contains(it.id) }?.map {
                                BookmarkDTO(
                                    it.id,
                                    it.name,
                                    it.url,
                                    it.imageLocalName,
                                    it.imageURL,
                                    false
                                )
                            }?.toMutableList() ?: mutableListOf()
                        bookmarksDTOList.postValue(updateList)
                    } else {
                        resetToMandatoryApps()
                        savePreference()
                    }
                }
                return@switchMap MutableLiveData(sectionsDTOList)
            }
        }

    private fun resetToMandatoryApps() {
        updateList =
            jsonResponse.value?.apps?.filter {
                jsonResponse.value?.mandatoryApps?.contains(it.id)
                    ?: false
            }?.map {
                BookmarkDTO(it.id, it.name, it.url, it.imageLocalName, it.imageURL, false)
            }?.toMutableList() ?: mutableListOf()
        bookmarksDTOList.postValue(updateList)
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
        if (jsonResponse.value?.mandatoryApps?.contains(id) != true) {
            if (bookmarksDTOList.value?.filter { it.id == id }.isNullOrEmpty()) {
                val updateList = bookmarksDTOList.value!!.toMutableList()
                val bookmarkDTO = jsonResponse.value?.apps
                    ?.filter { it.id == id }
                    ?.map {
                        BookmarkDTO(
                            it.id,
                            it.name,
                            it.url,
                            it.imageLocalName,
                            it.imageURL,
                            false
                        )
                    }
                    ?.first()
                if (bookmarkDTO != null) {
                    updateList.add(bookmarkDTO)
                    bookmarksDTOList.value = updateList
                    savePreference()
                }
            } else {
                removeBookmark(id)
            }
        }
    }

    private fun savePreference() {
        sharedPreferences.edit()
            .putString(
                "bookmarks",
                bookmarksDTOList.value?.map { it.id }?.joinToString(separator = ",")
            )
            .apply()
    }

    fun resetBookmarks() {
        client.cache?.evictAll()
        resetToMandatoryApps()
        savePreference()
        showBookmarkInstructions.value = true
        sharedPreferences.edit()
            .putBoolean("showBookmarkInstructions", true).apply()
    }

    fun removeBookmark(id: String) {
        val updateList = bookmarksDTOList.value!!.toMutableList()
        updateList.removeIf { it.id == id }
        bookmarksDTOList.value = updateList
        savePreference()
    }

    fun showRemoveIcon(showRemoveIcon: Boolean) {
        val updateList = bookmarksDTOList.value!!.toMutableList()
        updateList.forEach { item -> item.showRemoveIcon = showRemoveIcon }
        bookmarksDTOList.value = updateList
    }

    fun swapBookmark(i1: Int, i2: Int) {
        Log.d("AppViewModel", "i1: $i1, i2: $i2")
//        Log.d("AppViewModel", "${bookmarksDTOList.value?.toMutableList()}")
        Log.d("AppViewModel", updateList.toString())
        updateList.add(i2, updateList.removeAt(i1))
//        Log.d("AppViewModel", "${bookmarksDTOList.value?.toMutableList()}")
    }

    fun saveBookmark() {
        bookmarksDTOList.value = updateList
        savePreference()
    }


    fun getAppById(id: String): App? {
        return jsonResponse.value?.apps?.single { app: App -> app.id == id }
    }

    fun bookmarksDTOListContains(id: String): Boolean {
        return bookmarksDTOList.value?.any { item -> item.id == id } == true
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