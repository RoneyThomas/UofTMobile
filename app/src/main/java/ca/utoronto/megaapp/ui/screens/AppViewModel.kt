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
import ca.utoronto.megaapp.data.entities.UofTMobile
import ca.utoronto.megaapp.data.repository.EngRSSRepository
import ca.utoronto.megaapp.data.repository.UofTMobileRepository
import ca.utoronto.megaapp.ui.BookmarkDTO
import ca.utoronto.megaapp.ui.SectionsDTO
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.Locale

class AppViewModel(private val application: Application) : AndroidViewModel(application) {

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

    private var bookmarksDTOList = MutableLiveData<List<BookmarkDTO>>()
    var showRemoveIcon = MutableLiveData(false)

    var searchQuery = MutableLiveData("")
    private var refresh = MutableLiveData(false)
    private var updateList: MutableList<BookmarkDTO> = mutableListOf()

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

    fun setEditMode(isEditMode: Boolean) {
        showRemoveIcon.value = isEditMode
        Log.d("AppViewModel setEditMode 1", "setEditMode: $updateList")
        Log.d("AppViewModel setEditMode 1", "setEditMode: ${bookmarksDTOList.value}")
//        if (isEditMode) {
//            updateList = bookmarksDTOList.value!!.toMutableList()
//        }
        bookmarksDTOList.value = updateList.map { item ->
            if (jsonResponse.value?.mandatoryApps?.contains(item.id) == false) {
                item.copy(showRemoveIcon = isEditMode)
            } else {
                item
            }
        }.toList()
        if (!isEditMode) {
            Log.d("AppViewModel setEditMode", "setEditMode: $updateList")
            Log.d("AppViewModel setEditMode", "setEditMode: ${bookmarksDTOList.value}")
            savePreference()
        }
        Log.d("AppViewModel setEditMode 2", "setEditMode: $updateList")
        Log.d("AppViewModel setEditMode 2", "setEditMode: ${bookmarksDTOList.value}")
    }

    fun getBookMarks(): LiveData<List<BookmarkDTO>> {
        return bookmarksDTOList
    }

    // Creates DTO from jsonResponse
    private fun sections(): LiveData<Map<String, SectionsDTO>> = jsonResponse.switchMap {
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
                                it.imageURL.ifEmpty { it.imageLocalName.lowercase(Locale.getDefault()) },
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
        updateList = jsonResponse.value?.apps?.filter {
            jsonResponse.value?.mandatoryApps?.contains(it.id) ?: false
        }?.map {
            BookmarkDTO(
                it.id,
                it.name,
                it.url,
                it.imageURL.ifEmpty { it.imageLocalName.lowercase(Locale.getDefault()) },
                false
            )
        }?.toMutableList() ?: mutableListOf()
        bookmarksDTOList.value = updateList
        savePreference()
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
                updateList = bookmarksDTOList.value!!.toMutableList()
                val bookmarkDTO = jsonResponse.value?.apps?.filter { it.id == id }?.map {
                        BookmarkDTO(
                            it.id,
                            it.name,
                            it.url,
                            it.imageURL.ifEmpty { it.imageLocalName.lowercase(Locale.getDefault()) },
                            false
                        )
                    }?.first()
                if (bookmarkDTO != null) {
                    updateList.add(bookmarkDTO)
                    bookmarksDTOList.value = updateList.toList()
                    savePreference()
                }
            } else {
                removeBookmark(id)
            }
        }
    }

    private fun savePreference() {
        sharedPreferences.edit()
            .putString("bookmarks", bookmarksDTOList.value?.joinToString(separator = ",") { it.id })
            .apply()
    }

    @OptIn(ExperimentalCoilApi::class)
    fun resetBookmarks() {
        resetToMandatoryApps()
        client.cache?.evictAll()
        application.imageLoader.diskCache?.clear()
        application.imageLoader.memoryCache?.clear()
        showBookmarkInstructions.value = true
        sharedPreferences.edit().putBoolean("showBookmarkInstructions", true).apply()
    }

    fun removeBookmark(id: String) {
        updateList = bookmarksDTOList.value!!.toMutableList()
        updateList.removeIf { it.id == id }
        bookmarksDTOList.value = updateList.toList()
        savePreference()
    }

    fun swapBookmark(i1: Int, i2: Int) {
        Log.d("AppViewModel", "i1: $i1, i2: $i2")
        Log.d("AppViewModel swapBookmark", updateList.toString())
        updateList.add(i2, updateList.removeAt(i1))
        Log.d("AppViewModel swapBookmark", updateList.toString())
//        if (i1 < updateList.size && i2 < updateList.size) {
//
//        }
    }

    fun hideBookmarkInstructions() {
        showBookmarkInstructions.value = false
        sharedPreferences.edit().putBoolean("showBookmarkInstructions", false).apply()
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