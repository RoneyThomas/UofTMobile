package ca.utoronto.megaapp.ui.util

import ca.utoronto.megaapp.R

// Since app icons are described in json,
// to use them in compose screen you need to use reflection to get resource id.
// Using this map avoids it, in future when more app icons are added, make sure to add them here
val iconResourceMap = mapOf(
    "blogs" to R.drawable.blogs,
    "course_finder" to R.drawable.course_finder,
    "dates" to R.drawable.dates,
    "email" to R.drawable.email,
    "events" to R.drawable.events,
    "facebook" to R.drawable.facebook,
    "instagram" to R.drawable.instagram,
    "library" to R.drawable.library,
    "linkedin" to R.drawable.linkedin,
    "magazine" to R.drawable.magazine,
    "maps" to R.drawable.maps,
    "news" to R.drawable.news,
    "portal" to R.drawable.portal,
    "research" to R.drawable.research,
    "rosi" to R.drawable.rosi,
    "social" to R.drawable.social,
    "systemstatus" to R.drawable.systemstatus,
    "tours" to R.drawable.tours,
    "transit" to R.drawable.transit,
    "twitter" to R.drawable.twitter,
    "ucheck" to R.drawable.ucheck,
    "ulife" to R.drawable.ulife,
    "utsc" to R.drawable.utsc,
    "youtube" to R.drawable.youtube,
)