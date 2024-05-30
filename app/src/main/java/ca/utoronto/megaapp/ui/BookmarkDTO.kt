package ca.utoronto.megaapp.ui

data class BookmarkDTO(
    val id: String,
    val name: String,
    val url: String,
    val imageLocation: String,
    var showRemoveIcon: Boolean
)