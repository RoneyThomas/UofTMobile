package ca.utoronto.megaapp.ui.screens.homeScreen

import android.app.Application
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

    fun getAppById(id: String): App? {
        return jsonResponse.value?.apps?.single { app: App -> app.id == id }
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