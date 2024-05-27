package ca.utoronto.megaapp.ui

data class BookmarkDTO(
    val id: String,
    val name: String,
    val url: String,
    val imageLocalName: String,
    val imageURL: String,
) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is BookmarkDTO -> {
                this.id == other.id
            }

            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + imageLocalName.hashCode()
        result = 31 * result + imageURL.hashCode()
        return result
    }
}