package com.example.service

import com.example.data.model.CustomRule

data class FilterMatch(
    val matched: Boolean,
    val pattern: String,
    val category: String,
    val reason: String
)

object AntiPmoFilterEngine {

    val DEFAULT_TERABOX_KEYWORDS = listOf(
        "terabox",
        "terabox.com",
        "teraboxapp.com",
        "1024tera",
        "1024 terabox",
        "nephobox",
        "4funbox",
        "mirrobox",
        "momerybox",
        "tibabox",
        "freeterabox",
        "terabox link",
        "link terabox",
        "terabox video",
        "terabox share",
        "terabox download",
        "terabox apk"
    )

    val DEFAULT_UC_KEYWORDS = listOf(
        "uc browser link",
        "uc browser",
        "ucweb",
        "ucweb link",
        "ucdown",
        "uc browser apk",
        "9apps",
        "wapka",
        "wapka link"
    )

    val KNOWN_BROWSER_PACKAGES = setOf(
        "com.android.chrome",
        "com.google.android.googlequicksearchbox",
        "com.UCMobile.intl",
        "com.uc.browser.en",
        "com.UCMobile",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.opera.mini.native",
        "com.opera.gx",
        "com.brave.browser",
        "com.microsoft.emmx",
        "com.sec.android.app.sbrowser",
        "com.kiwibrowser.browser",
        "com.duckduckgo.mobile.android",
        "com.cloudmosa.puffinFree",
        "com.quark.browser",
        "com.mi.globalbrowser",
        "com.android.browser",
        "com.vivobrowser",
        "com.heytap.browser"
    )

    val DEFAULT_ADULT_KEYWORDS = listOf(
        "bokep",
        "bokep indo",
        "bokep viral",
        "bokep jepang",
        "bokep barat",
        "bokep terbaru",
        "video bokep",
        "link bokep",
        "jav",
        "jav sub indo",
        "javhd",
        "javsub",
        "pornhub",
        "xvideos",
        "xnxx",
        "xhamster",
        "hentai",
        "nhentai",
        "doujin",
        "rule34",
        "spankbang",
        "youporn",
        "redtube",
        "brazzers",
        "onlyfans leak",
        "chaturbate",
        "nekopoi",
        "doodstream",
        "luluvid",
        "komik dewasa",
        "komik 18+",
        "manhwa 18+",
        "video 18+",
        "video dewasa",
        "film dewasa",
        "link 18+",
        "18+ link",
        "18+ dewasa",
        "asupan 18+",
        "asupan dewasa",
        "pemersatu bangsa",
        "ngocok",
        "coli",
        "sange",
        "vcs",
        "open bo",
        "porn",
        "porno",
        "xxx video",
        "erotic",
        "nsfw video",
        "camgirl",
        "beeg",
        "eporner",
        "tube8",
        "streamtape porn",
        "poophd",
        "droplink 18+"
    )

    private fun compact(text: String): String =
        text.lowercase().replace(Regex("[^\\p{L}\\p{N}+]"), "")

    private val ADULT_WORD_REGEXES = DEFAULT_ADULT_KEYWORDS.map { kw ->
        kw to Regex("""(?i)\b${Regex.escape(kw)}\b""")
    }

    private val TERABOX_WORD_REGEXES = DEFAULT_TERABOX_KEYWORDS.map { kw ->
        kw to Regex("""(?i)\b${Regex.escape(kw)}\b""")
    }

    private val UC_WORD_REGEXES = DEFAULT_UC_KEYWORDS.map { kw ->
        kw to Regex("""(?i)\b${Regex.escape(kw)}\b""")
    }

    val DEFAULT_REGEX_PATTERNS = listOf(
        FilterRegexRule(
            id = "regex_terabox_url",
            regex = Regex("""(?i)(https?://)?([a-z0-9-]+\.)*(terabox|teraboxapp|1024tera|nephobox|4funbox|mirrobox|momerybox)\.(com|app|me|link|fun|cc|org)/.*"""),
            category = "TERABOX",
            description = "Terabox Cloud Link Regex"
        ),
        FilterRegexRule(
            id = "regex_adult_domains",
            regex = Regex("""(?i)(https?://)?([a-z0-9-]+\.)*(pornhub|xvideos|xnxx|xhamster|redtube|youporn|brazzers|onlyfans|nekopoi|doodstream|luluvid|nhentai|spankbang|eporner|beeg|tube8|chaturbate|stripchat|livejasmin|javhd|javfinder|missav|jable|supjav)\.(com|net|org|tv|xyz|vip|me|to|is|site|cc|link|video|club|pro|guru|run|today)/?.*"""),
            category = "ADULT_18+",
            description = "18+ Adult Domains Regex"
        ),
        FilterRegexRule(
            id = "regex_id_adult_queries",
            regex = Regex("""(?i)\b(bokep(\s+indo|\s+viral|\s+jepang|\s+barat|\s+terbaru|\s+smp|\s+sma)?|jav(\s+sub|\s+hd|\s+indo)?|hentai|pemersatu\s+bangsa|video\s*(18\+|dewasa|bokep)|ngocok(\s+kontol|\s+terus)?|coli(\s+terus|\s+siang|\s+malam)?|sange(\s+berat)?|vcs(\s+gratis|\s+berbayar)?|open\s+bo|asupan\s*18\+|folder\s*bokep|link\s*bokep|link\s*terabox|terabox\s*link|uc\s*browser\s*link)\b"""),
            category = "ADULT_18+",
            description = "Indonesian Adult & PMO Keywords Regex"
        ),
        FilterRegexRule(
            id = "regex_nsfw_terms",
            regex = Regex("""(?i)\b(porn|porno|xxx|nsfw|hentai|ecchi|eroge|milf|gangbang|creampie|blowjob|anal|threesome|bdsm|bukkake|deepthroat|masturbat(e|ion)|masturbasi)\b"""),
            category = "ADULT_18+",
            description = "Global NSFW Keywords Regex"
        ),
        FilterRegexRule(
            id = "regex_separated_adult_terms",
            regex = Regex("""(?i)\b(?:b[\W_]*o[\W_]*k[\W_]*e[\W_]*p|p[\W_]*o[\W_]*r[\W_]*n|t[\W_]*e[\W_]*r[\W_]*a[\W_]*b[\W_]*o[\W_]*x)\b"""),
            category = "ADULT_18+",
            description = "Kata kunci dengan pemisah spasi atau tanda baca"
        )
    )

    data class FilterRegexRule(
        val id: String,
        val regex: Regex,
        val category: String,
        val description: String
    )

    fun evaluate(
        input: String,
        customRules: List<CustomRule> = emptyList(),
        filterAdult: Boolean = true,
        filterTerabox: Boolean = true,
        filterUc: Boolean = true,
        whitelistedWords: Set<String> = emptySet()
    ): FilterMatch {
        if (input.isBlank()) {
            return FilterMatch(matched = false, pattern = "", category = "", reason = "")
        }

        val normalized = input.trim().lowercase()
            .replace("\u200B", "")
            .replace("\u00A0", " ")
            .replace(Regex("\\s+"), " ")
        val compactInput = compact(normalized)

        for (w in whitelistedWords) {
            val cleanW = w.trim().lowercase()
            if (cleanW.isNotBlank() && normalized.contains(cleanW)) {
                return FilterMatch(
                    matched = false,
                    pattern = cleanW,
                    category = "WHITELISTED",
                    reason = "Dikecualikan (Whitelist: $cleanW)"
                )
            }
        }

        fun keywordHit(kw: String, regex: Regex): Boolean {
            val compactKw = compact(kw)
            val compactHit = kw.length >= 5 && compactKw.length >= 4 && compactInput.contains(compactKw)
            return regex.containsMatchIn(normalized) ||
                (kw.contains(" ") && normalized.contains(kw)) ||
                compactHit
        }

        if (filterTerabox) {
            for ((kw, regex) in TERABOX_WORD_REGEXES) {
                if (keywordHit(kw, regex)) {
                    return FilterMatch(
                        matched = true,
                        pattern = kw,
                        category = "TERABOX",
                        reason = "Terdeteksi link / kata kunci Terabox: '$kw'"
                    )
                }
            }
        }

        if (filterUc) {
            for ((kw, regex) in UC_WORD_REGEXES) {
                if (keywordHit(kw, regex)) {
                    return FilterMatch(
                        matched = true,
                        pattern = kw,
                        category = "UC_LINK",
                        reason = "Terdeteksi kata kunci tautan UC Browser: '$kw'"
                    )
                }
            }
        }

        if (filterAdult) {
            for ((kw, regex) in ADULT_WORD_REGEXES) {
                if (keywordHit(kw, regex)) {
                    return FilterMatch(
                        matched = true,
                        pattern = kw,
                        category = "ADULT_18+",
                        reason = "Terdeteksi kata kunci 18+ / PMO: '$kw'"
                    )
                }
            }
        }

        for (regexRule in DEFAULT_REGEX_PATTERNS) {
            if (regexRule.category == "TERABOX" && !filterTerabox) continue
            if (regexRule.category == "UC_LINK" && !filterUc) continue
            if (regexRule.category == "ADULT_18+" && !filterAdult) continue

            if (regexRule.regex.containsMatchIn(normalized)) {
                return FilterMatch(
                    matched = true,
                    pattern = regexRule.id,
                    category = regexRule.category,
                    reason = "Pola regex terdeteksi (${regexRule.description})"
                )
            }
        }

        for (custom in customRules) {
            if (!custom.isEnabled) continue

            if (custom.isRegex) {
                try {
                    val regex = Regex(custom.pattern, RegexOption.IGNORE_CASE)
                    if (regex.containsMatchIn(normalized)) {
                        return FilterMatch(
                            matched = true,
                            pattern = custom.pattern,
                            category = custom.category,
                            reason = "Aturan kustom regex cocok: '${custom.pattern}'"
                        )
                    }
                } catch (_: Exception) {
                    // Invalid custom regex is ignored.
                }
            } else {
                val kwRegex = Regex("""(?i)\b${Regex.escape(custom.pattern)}\b""")
                if (kwRegex.containsMatchIn(normalized) || normalized.contains(custom.pattern.lowercase())) {
                    return FilterMatch(
                        matched = true,
                        pattern = custom.pattern,
                        category = custom.category,
                        reason = "Kata kunci kustom cocok: '${custom.pattern}'"
                    )
                }
            }
        }

        return FilterMatch(matched = false, pattern = "", category = "", reason = "")
    }
}
