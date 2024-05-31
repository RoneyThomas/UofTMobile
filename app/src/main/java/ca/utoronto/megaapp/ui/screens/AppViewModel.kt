package ca.utoronto.megaapp.ui.screens

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import ca.utoronto.megaapp.data.entities.UofTMobile
import ca.utoronto.megaapp.data.repository.EngRSSRepository
import ca.utoronto.megaapp.data.repository.UofTMobileRepository
import ca.utoronto.megaapp.ui.BookmarkDTO
import ca.utoronto.megaapp.ui.SectionsDTO
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "UofTAppDataStore")

class AppViewModel(private val application: Application) :
    AndroidViewModel(application) {

    // Creates OkHttpClient with http caching
    private val client: OkHttpClient = OkHttpClient.Builder().cache(
        Cache(
            directory = File(application.cacheDir, "http_cache"),
            maxSize = 5L * 1024L * 1024L // 5 MiB
        )
    ).build()

    private val uofTMobileRepository: UofTMobileRepository =
        UofTMobileRepository(application, client)
    private val dataStore = application.applicationContext.dataStore
    private val bookmarkDataStoreKey = stringPreferencesKey("bookmark")
    private val firstLaunchStoreKey = booleanPreferencesKey("firstLaunch")

    val showBookmarkInstructions = MutableLiveData(false)
    val editMode = MutableLiveData(false)

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
        val bookmarksFlow: Flow<Boolean> = dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey("firstLaunch")] ?: true
        }
        viewModelScope.launch {
            val firstTime = bookmarksFlow.collect {
                showBookmarkInstructions.value = it
            }
        }
        // get value from flow
    }

    fun refresh() {
        refresh.value = true
        loadApps()
        viewModelScope.launch {
            delay(400)
            refresh.value = false
        }
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
                val bookmarksFlow: Flow<String> = dataStore.data.map { preferences ->
                    preferences[stringPreferencesKey("bookmark")] ?: ""
                }
                viewModelScope.launch {
                    val bookmarks = bookmarksFlow.first()
                    Log.d("Flow", bookmarks)
                    if (bookmarks.isNotEmpty() && bookmarksDTOList.value != null) {
                        val bookmarkList = bookmarks.split(",").toList()
                        bookmarkList.forEach { id ->
                            val app = jsonResponse.value?.apps?.first { it.id == id }
                            if (app != null) {
                                updateList.add(
                                    BookmarkDTO(
                                        app.id,
                                        app.name,
                                        app.url,
                                        app.imageURL.ifEmpty { app.imageLocalName.lowercase(Locale.getDefault()) },
                                    )
                                )
                            }
                        }
                        bookmarksDTOList.postValue(updateList)
                    } else {
                        resetToMandatoryApps()
                    }
                }

            }
            return@switchMap MutableLiveData(sectionsDTOList)
        }
    }

    private fun resetToMandatoryApps() {
        updateList = jsonResponse.value?.apps?.filter {
            isMandatory(it.id) ?: false
        }?.map {
            BookmarkDTO(
                it.id,
                it.name,
                it.url,
                it.imageURL.ifEmpty { it.imageLocalName.lowercase(Locale.getDefault()) },
            )
        }?.toMutableList() ?: mutableListOf()
        bookmarksDTOList.value = updateList
        viewModelScope.launch {
            savePreference()
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
        if (!isMandatory(id)) {
            if (bookmarksDTOList.value?.filter { it.id == id }.isNullOrEmpty()) {
                updateList = bookmarksDTOList.value!!.toMutableList()
                val bookmarkDTO = jsonResponse.value?.apps?.filter { it.id == id }?.map {
                    BookmarkDTO(
                        it.id,
                        it.name,
                        it.url,
                        it.imageURL.ifEmpty { it.imageLocalName.lowercase(Locale.getDefault()) },
                    )
                }?.first()
                if (bookmarkDTO != null) {
                    updateList.add(bookmarkDTO)
                    bookmarksDTOList.value = updateList.toList()
                    viewModelScope.launch {
                        savePreference()
                    }
                }
            } else {
                removeBookmark(id)
            }
        }
    }

    private suspend fun savePreference() {
        dataStore.edit { preferences ->
            preferences[bookmarkDataStoreKey] =
                bookmarksDTOList.value?.joinToString(separator = ",") { it.id }.toString()
        }
        Log.d("AppViewModel swapBookmark", bookmarksDTOList.toString())
    }

    @OptIn(ExperimentalCoilApi::class)
    fun resetBookmarks() {
        resetToMandatoryApps()
        client.cache?.evictAll()
        application.imageLoader.diskCache?.clear()
        application.imageLoader.memoryCache?.clear()
        showBookmarkInstructions.value = true
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[firstLaunchStoreKey] = true
            }
        }
    }

    fun removeBookmark(id: String) {
        updateList = bookmarksDTOList.value!!.toMutableList()
        updateList.removeIf { it.id == id }
        bookmarksDTOList.value = updateList.toList()
        viewModelScope.launch {
            savePreference()
        }
    }

    fun setEditMode(isEdit: Boolean) {
        if (!isEdit) {
            viewModelScope.launch {
                savePreference()
            }
        }
        editMode.value = isEdit
    }

    fun swapBookmark(i1: Int, i2: Int) {
        Log.d("AppViewModel", "i1: $i1, i2: $i2")
        Log.d("AppViewModel swapBookmark", updateList.toString())
        updateList = bookmarksDTOList.value?.toMutableList() ?: mutableListOf()
        updateList.add(i2, updateList.removeAt(i1))
        Log.d("AppViewModel swapBookmark", updateList.toString())
        bookmarksDTOList.value = updateList
    }

    fun hideBookmarkInstructions() {
        showBookmarkInstructions.value = false
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[firstLaunchStoreKey] = false
            }
        }
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

    fun isMandatory(id: String): Boolean {
        return jsonResponse.value?.mandatoryApps?.contains(id) ?: false
    }
}