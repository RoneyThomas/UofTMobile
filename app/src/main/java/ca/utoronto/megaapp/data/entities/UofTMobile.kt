package ca.utoronto.megaapp.data.entities

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

// Generated using https://app.quicktype.io/
// Used for serializing json object from http request

@Immutable
@Serializable
data class UofTMobile(
    val disabledFeatures: List<String>,
    val supportedBuilds: List<Long>,
    val sections: List<Section>,
    val mandatoryApps: List<String>,
    val apps: List<App>
)

@Immutable
@Serializable
data class App(
    val name: String,
    val id: String,
    val sectionIndex: Long,
    val url: String,
    val opensInApp: Boolean,
    val imageLocalName: String,
    val imageURL: String,
    val url2: String? = null,
    val urlOld: String? = null
)

@Immutable
@Serializable
data class Section(
    val index: Long,
    val name: String
)
