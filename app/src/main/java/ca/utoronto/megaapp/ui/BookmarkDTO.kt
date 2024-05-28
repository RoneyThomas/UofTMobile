package ca.utoronto.megaapp.ui

data class BookmarkDTO(
    val id: String,
    val name: String,
    val url: String,
    val imageLocalName: String,
    val imageURL: String,
    var showRemoveIcon: Boolean
)