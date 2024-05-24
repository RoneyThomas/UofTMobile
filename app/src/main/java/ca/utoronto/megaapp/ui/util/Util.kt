package ca.utoronto.megaapp.ui.util

fun rssDateFormatter(s: String): String {
    while (s.length >= 16) {
        if (s[5] == '0') {
            return "${s.substring(8, 11)} ${s.substring(6, 7)}, ${s.substring(12, 16)}"
        }
        return "${s.substring(8, 11)} ${s.substring(5, 7)}, ${s.substring(12, 16)}"
    }
    return ""
}