package ca.utoronto.megaapp.ui.screens.homeScreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.preference.PreferenceManager
import ca.utoronto.megaapp.data.entities.App
import ca.utoronto.megaapp.data.entities.Section
import ca.utoronto.megaapp.data.entities.UofTMobile
import ca.utoronto.megaapp.data.repository.UofTMobileRepository
import ca.utoronto.megaapp.ui.SectionsDTO

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val uofTMobileRepository: UofTMobileRepository = UofTMobileRepository(application)
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication())

    val jsonResponse: MutableLiveData<UofTMobile> = uofTMobileRepository.result

    var bookmarks = MutableLiveData<List<String>>()


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
                    savePreference()
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
            bookmarks.postValue(bookmarks.value?.plus(id))
            savePreference()
        } else {
            removeBookmark(id)
        }
    }

    private fun savePreference() {
        sharedPreferences.edit()
            .putString("bookmarks", bookmarks.value!!.joinToString(separator = ",")).apply()
    }

    fun removeBookmark(id: String) {
        bookmarks.postValue(bookmarks.value?.minus(id))
        savePreference()
    }

    fun swapBookmark(id1: String, id2: String) {
        Log.d("AppViewModel", "i1: $id1, i2: $id2")
        Log.d("AppViewModel", "${bookmarks.value?.toMutableList()}")
        val tempList = bookmarks.value!!.toMutableList()
        val i1 = tempList.indexOf(id1)
        val i2 = tempList.indexOf(id2)
        tempList.removeAt(i2)
        tempList.add(i1, id2)
//            bookmarks.postValue(tempList)
        bookmarks.value = tempList
        Log.d("AppViewModel", "${bookmarks.value?.toMutableList()}")
        savePreference()
//        if (i1 != -1 && i2 != -1) {
//
//        } else {
//            Log.d("AppViewModel else", "i1: $id1, i2: $id2")
//        }
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