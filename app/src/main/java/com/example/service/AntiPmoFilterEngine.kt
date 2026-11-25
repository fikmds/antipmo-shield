package com.example.service

import com.example.data.model.CustomRule

data class FilterMatch(
    val matched: Boolean,
    val pattern: String,
    val category: String, // "TERABOX", "UC_LINK", "ADULT_18+", "CUSTOM_REGEX", "KEYWORD"
    val reason: String
)

object AntiPmoFilterEngine {

    val DEFAULT_TERABOX_KEYWORDS = listOf(
        "terabox",
        "terabox.com",
        "teraboxapp.com",
        "1024tera",
        "nephobox",
        "4funbox",
        "mirrobox",
        "momerybox",
        "tibabox",
        "freeterabox",
        "terabox link",
        "link terabox",
        "terabox video",
        "terabox share"
    )

    val DEFAULT_UC_KEYWORDS = listOf(
        "uc browser link",
        "uc browser",
        "ucweb link",
        "ucdown",
        "9apps",
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

    // Full word matched adult keywords (must match on whole word boundary \b to prevent false positives while typing)
    val DEFAULT_ADULT_KEYWORDS = listOf(
        "bokep",
        "bokep indo",
        "bokep viral",
        "bokep jepang",
        "bokep barat",
        "video bokep",
        "link bokep",
        "jav",
        "jav sub indo",
        "javhd",
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
        "link 18+",
        "18+ link",
        "pemersatu bangsa",
        "ngocok",
        "coli",
        "sange",
        "vcs",
        "open bo",
        "porn",
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

    // Precompiled word boundary regex map for accurate token matching
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
            regex = Regex("""(?i)\b(porn|xxx|nsfw|hentai|ecchi|eroge|milf|gangbang|creampie|blowjob|anal|threesome|bdsm|bukkake|deepthroat|masturbat(e|ion)|masturbasi)\b"""),
            category = "ADULT_18+",
            description = "Global NSFW Keywords Regex"
        )
    )

    data class FilterRegexRule(
        val id: String,
        val regex: Regex,
        val category: String,
        val description: String
    )

    /**
     * Inspects text, URL, and window content against default rules, whitelist, and custom user rules.
     */
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

        // 0. Check Whitelist first: if input matches any whitelisted benign phrase, skip!
        for (w in whitelistedWords) {
            val cleanW = w.trim().lowercase()
            if (cleanW.isNotBlank() && normalized.contains(cleanW)) {
                // If it's explicitly whitelisted (e.g. "javascript" or "version control"), do not block
                return FilterMatch(matched = false, pattern = cleanW, category = "WHITELISTED", reason = "Dikecualikan (Whitelist: $cleanW)")
            }
        }

        // 1. Check Terabox Keywords & Regex (if enabled)
        if (filterTerabox) {
            for ((kw, regex) in TERABOX_WORD_REGEXES) {
                if (regex.containsMatchIn(input) || (kw.contains(" ") && normalized.contains(kw))) {
                    return FilterMatch(
                        matched = true,
                        pattern = kw,
                        category = "TERABOX",
                        reason = "Terdeteksi link / kata kunci Terabox: '$kw'"
                    )
                }
            }
        }

        // 2. Check UC Browser keywords (if enabled)
        if (filterUc) {
            for ((kw, regex) in UC_WORD_REGEXES) {
                if (regex.containsMatchIn(input) || (kw.contains(" ") && normalized.contains(kw))) {
                    return FilterMatch(
                        matched = true,
                        pattern = kw,
                        category = "UC_LINK",
                        reason = "Terdeteksi kata kunci tautan UC Browser: '$kw'"
                    )
                }
            }
        }

        // 3. Check 18+ Adult Keywords (if enabled - No Porn Filter)
        if (filterAdult) {
            for ((kw, regex) in ADULT_WORD_REGEXES) {
                if (regex.containsMatchIn(input) || (kw.contains(" ") && normalized.contains(kw))) {
                    return FilterMatch(
                        matched = true,
                        pattern = kw,
                        category = "ADULT_18+",
                        reason = "Terdeteksi kata kunci 18+ / PMO: '$kw'"
                    )
                }
            }
        }

        // 4. Check Default Regex Rules
        for (regexRule in DEFAULT_REGEX_PATTERNS) {
            if (regexRule.category == "TERABOX" && !filterTerabox) continue
            if (regexRule.category == "UC_LINK" && !filterUc) continue
            if (regexRule.category == "ADULT_18+" && !filterAdult) continue

            if (regexRule.regex.containsMatchIn(input)) {
                return FilterMatch(
                    matched = true,
                    pattern = regexRule.id,
                    category = regexRule.category,
                    reason = "Pola regex terdeteksi (${regexRule.description})"
                )
            }
        }

        // 5. Check Custom User Rules (From DB)
        for (custom in customRules) {
            if (!custom.isEnabled) continue

            if (custom.isRegex) {
                try {
                    val regex = Regex(custom.pattern, RegexOption.IGNORE_CASE)
                    if (regex.containsMatchIn(input)) {
                        return FilterMatch(
                            matched = true,
                            pattern = custom.pattern,
                            category = custom.category,
                            reason = "Aturan kustom regex cocok: '${custom.pattern}'"
                        )
                    }
                } catch (_: Exception) {
                    // Invalid regex skipped gracefully
                }
            } else {
                val kwRegex = Regex("""(?i)\b${Regex.escape(custom.pattern)}\b""")
                if (kwRegex.containsMatchIn(input) || normalized.contains(custom.pattern.lowercase())) {
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

