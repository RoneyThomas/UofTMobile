package ca.utoronto.megaapp.ui

// DTO used for Sections in the bottom sheet
data class SectionsDTO(
    val index: Long,
    val name: String,
    val apps: MutableList<Int>,
    val checked: MutableList<Int>
)