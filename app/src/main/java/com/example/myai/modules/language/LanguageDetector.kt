package com.example.myai.modules.language

enum class DetectedLanguage {
    ENGLISH,
    HINGLISH,
    HINDI_DEVANAGARI
}

object LanguageDetector {
    private val HINGLISH_KEYWORDS = setOf(
        "kaise", "kaisa", "kaisi", "kya", "kyun", "kyu", "kab", "kahan", "kaha",
        "hai", "ho", "hain", "hoon", "hun", "tha", "thi", "the", "batao", "bataiye",
        "karo", "kijiye", "namaste", "shukriya", "dhanyawad", "bhai", "yaar",
        "acha", "achha", "theek", "thik", "mera", "meri", "mere", "aapka", "tumhara",
        "naam", "kaam", "madad", "samajh", "aaj", "kal", "parso", "subah", "shaam",
        "kuch", "sab", "yeh", "woh", "konsa", "kaun"
    )

    fun detect(text: String): DetectedLanguage {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return DetectedLanguage.ENGLISH

        // Check for Devanagari Unicode Block (U+0900 to U+097F)
        val devanagariCount = trimmed.count { it in '\u0900'..'\u097F' }
        if (devanagariCount > 0 && devanagariCount >= trimmed.filter { it.isLetter() }.length * 0.3) {
            return DetectedLanguage.HINDI_DEVANAGARI
        }

        // Check for Hinglish latin words
        val words = trimmed.lowercase().split(Regex("[\\s,?.!]+"))
        val hinglishMatchCount = words.count { it in HINGLISH_KEYWORDS }

        return if (hinglishMatchCount > 0) {
            DetectedLanguage.HINGLISH
        } else {
            DetectedLanguage.ENGLISH
        }
    }
}
