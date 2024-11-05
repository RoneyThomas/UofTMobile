package ca.utoronto.megaapp.ui.util

// Formatter used to show date in May 27, 2024 format
// And faster than using Date utils
fun rssDateFormatter(s: String): String {
    while (s.length >= 16) {
        if (s[5] == '0') {
            return "${s.substring(8, 11)} ${s.substring(6, 7)}, ${s.substring(12, 16)}"
        }
        return if (s.contains("+")){
            "${s.substring(8, 11)} ${s.substring(5, 7)}, ${s.substring(12, 16)}"
        } else {
            "${s.substring(7, 10)} ${s.substring(5, 7).trim()}, ${s.substring(11, 16)}"
        }
    }
    return ""
}