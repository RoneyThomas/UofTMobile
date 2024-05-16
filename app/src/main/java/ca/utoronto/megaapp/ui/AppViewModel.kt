package ca.utoronto.megaapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import ca.utoronto.megaapp.data.entities.App
import ca.utoronto.megaapp.data.entities.Section
import ca.utoronto.megaapp.data.entities.UofTMobile
import ca.utoronto.megaapp.data.repository.UofTMobileRepository

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val uofTMobileRepository: UofTMobileRepository = UofTMobileRepository(application)
    val jsonResponse: MutableLiveData<UofTMobile> = uofTMobileRepository.result

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

            val sections: LiveData<List<SectionsDTO>> by lazy {
                MutableLiveData(sectionsDTOList)
            }
            return@switchMap sections
        }
    }
}