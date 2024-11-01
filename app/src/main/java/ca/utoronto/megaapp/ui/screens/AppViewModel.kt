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
import ca.utoronto.megaapp.data.repository.RSSRepository
import ca.utoronto.megaapp.data.repository.UofTMobileRepository
import ca.utoronto.megaapp.ui.BookmarkDTO
import ca.utoronto.megaapp.ui.SectionsDTO
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.Locale

// For Jetpack DataStore
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
    val jsonResponse: MutableLiveData<UofTMobile> = uofTMobileRepository.result
    private var bookmarksDTOList = MutableLiveData<List<BookmarkDTO>>()
    var searchQuery = MutableLiveData("")
    private var updateList: MutableList<BookmarkDTO> = mutableListOf()
    private lateinit var rssFeed: LiveData<RssChannel>

    init {
        loadApps()
    }

    private fun loadApps() {
        uofTMobileRepository.loadApps()
        // DataStore is used for key-value persistence, here we are checking if the app is loaded for first time
        // If that is the case then we need to show instructions in home page
        val bookmarksFlow: Flow<Boolean> = dataStore.data.map { preferences ->
            preferences[firstLaunchStoreKey] ?: true
        }
        viewModelScope.launch {
            bookmarksFlow.collect {
                showBookmarkInstructions.value = it
            }
        }
    }

    fun refresh() {
        loadApps()
    }

    fun getBookMarks(): LiveData<List<BookmarkDTO>> {
        return bookmarksDTOList
    }

    // Creates Sections DTO from jsonResponse, used in the bottom sheet, where we need to show section along with its apps
    // The switchmap transform function automatically runs when jsonResponse is updated and is observed in the view
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
            // The App only stores app id for each app the user pins to home screen, when the app is opened we need to
            // get the keys from DataStore and match it with jsonResponse for full BookmarkDTO,
            // Which is then consumed by the HomeScreen
            if (bookmarksDTOList.value == null) {
                val bookmarksFlow: Flow<String> = dataStore.data.map { preferences ->
                    preferences[bookmarkDataStoreKey] ?: ""
                }
                viewModelScope.launch {
                    bookmarksFlow.collect { it ->
                        Log.d("Flow before it", it)
                        if (it.isNotEmpty()) {
                            Log.d("Flow inside", it)
                            if (updateList.isEmpty()) {
                                val bookmarkList = it.split(",").toList()
                                bookmarkList.forEach { id ->
                                    try {
                                        val app =
                                            jsonResponse.value?.apps?.firstOrNull { it.id == id }
                                        if (app != null) {
                                            updateList.add(
                                                BookmarkDTO(
                                                    app.id,
                                                    app.name,
                                                    app.url,
                                                    app.imageURL.ifEmpty {
                                                        app.imageLocalName.lowercase(
                                                            Locale.getDefault()
                                                        )
                                                    },
                                                )
                                            )
                                        }
                                    } catch (e: NoSuchElementException) {
                                        Log.e("Bookmark Removed", id)
                                    }
                                }
                                // Check all mandatory apps included
                                jsonResponse.value?.mandatoryApps?.forEach { app ->
                                    if (updateList.none { it.id == app }) {
                                        val newMandatoryApp =
                                            jsonResponse.value?.apps?.firstOrNull { it.id == app }
                                        if (newMandatoryApp != null){
                                            updateList.add(
                                                BookmarkDTO(
                                                    newMandatoryApp.id,
                                                    newMandatoryApp.name,
                                                    newMandatoryApp.url,
                                                    newMandatoryApp.imageURL.ifEmpty {
                                                        newMandatoryApp.imageLocalName.lowercase(
                                                            Locale.getDefault()
                                                        )
                                                    },
                                                )
                                            )
                                            addBookmark(app)
                                        }
                                    }
                                }
                                bookmarksDTOList.postValue(updateList)
                            }
                        } else {
                            // If there is no bookmarks found from DataStore, that means we need to show the mandatory apps
                            // Happens when the app is opened first time or when app is reset
                            resetToMandatoryApps()
                        }
                        this.cancel()
                    }
                }
            }
            return@switchMap MutableLiveData(sectionsDTOList)
        }
    }

    // Resets the pinned apps to mandatory apps and then saves it
    private fun resetToMandatoryApps() {
        updateList = jsonResponse.value?.apps?.filter {
            isMandatory(it.id)
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

    // Used for search in bottom sheet, will create apps that match the searchQuery
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
        // Only non-mandatory apps can be added
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
        Log.d("AppViewModel savePreference", bookmarksDTOList.toString())
    }

    @OptIn(ExperimentalCoilApi::class)
    fun resetBookmarks() {
        resetToMandatoryApps()
        // Reset OkHTTP Cache
        client.cache?.evictAll()
        // Reset Coil cache
        application.imageLoader.diskCache?.clear()
        application.imageLoader.memoryCache?.clear()
        showBookmarkInstructions.value = true
        // Reset preferences
        viewModelScope.launch {
            dataStore.edit {
                it.clear()
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

    // Used by the drag and drop, to swap bookmarks
    fun swapBookmark(i1: Int, i2: Int) {
        Log.d("AppViewModel", "i1: $i1, i2: $i2")
        Log.d("AppViewModel swapBookmark", updateList.toString())
        updateList = bookmarksDTOList.value?.toMutableList() ?: mutableListOf()
        updateList.add(i2, updateList.removeAt(i1))
        Log.d("AppViewModel swapBookmark", updateList.toString())
        bookmarksDTOList.value = updateList
        viewModelScope.launch {
            savePreference()
        }
    }

    // When user dismisses the bookmark instructions we need save it in DataStore so that we don't show it again
    fun hideBookmarkInstructions() {
        showBookmarkInstructions.value = false
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[firstLaunchStoreKey] = false
            }
        }
    }

    // Checks an app with given id is mandatory or not from the jsonResponse
    fun isMandatory(id: String): Boolean {
        return jsonResponse.value?.mandatoryApps?.contains(id) ?: false
    }

    // Used by the RssScreen
    fun getRssFeed(rssUrl: String): LiveData<RssChannel> {
        rssFeed = liveData {
            val data = RSSRepository(client).rssChannel(rssUrl)
            if (data != null) {
                emit(data)
            }
        }
        return rssFeed
    }
}