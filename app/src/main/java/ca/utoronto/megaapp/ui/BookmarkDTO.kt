package ca.utoronto.megaapp.ui

// DTO used in showing bookmarks in the home page
data class BookmarkDTO(
    val id: String,
    val name: String,
    val url: String,
    val imageLocation: String,
)