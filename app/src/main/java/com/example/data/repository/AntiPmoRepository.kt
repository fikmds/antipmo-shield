package com.example.data.repository

import com.example.data.db.BlockedLogDao
import com.example.data.db.CustomRuleDao
import com.example.data.db.RecoveryDao
import com.example.data.model.BlockedLog
import com.example.data.model.CustomRule
import com.example.data.model.RecoveryProfile
import kotlinx.coroutines.flow.Flow

class AntiPmoRepository(
    private val blockedLogDao: BlockedLogDao,
    private val customRuleDao: CustomRuleDao,
    private val recoveryDao: RecoveryDao
) {
    val allLogs: Flow<List<BlockedLog>> = blockedLogDao.getAllLogs()
    val blockedCount: Flow<Int> = blockedLogDao.getBlockedCount()
    val recentLogs: Flow<List<BlockedLog>> = blockedLogDao.getRecentLogs()

    val allRules: Flow<List<CustomRule>> = customRuleDao.getAllRules()
    val activeRules: Flow<List<CustomRule>> = customRuleDao.getActiveRules()
    val recoveryProfile: Flow<RecoveryProfile?> = recoveryDao.getProfile()

    suspend fun recordBlockedAttempt(
        source: String,
        matchedPattern: String,
        category: String,
        targetUrlOrText: String,
        packageName: String = ""
    ) {
        val log = BlockedLog(
            triggerSource = source,
            matchedPattern = matchedPattern,
            category = category,
            targetUrlOrText = targetUrlOrText,
            packageName = packageName
        )
        blockedLogDao.insertLog(log)

        // Increment urges resisted
        val currentProfile = recoveryDao.getProfileOnce() ?: RecoveryProfile()
        recoveryDao.saveProfile(
            currentProfile.copy(
                totalUrgesResisted = currentProfile.totalUrgesResisted + 1
            )
        )
    }

    suspend fun insertRule(rule: CustomRule) {
        customRuleDao.insertRule(rule)
    }

    suspend fun updateRule(rule: CustomRule) {
        customRuleDao.updateRule(rule)
    }

    suspend fun deleteRule(rule: CustomRule) {
        customRuleDao.deleteRule(rule)
    }

    suspend fun insertCustomRule(pattern: String, isRegex: Boolean, category: String, description: String = "") {
        val rule = CustomRule(
            pattern = pattern.trim(),
            isRegex = isRegex,
            category = category,
            description = description.trim(),
            isEnabled = true
        )
        customRuleDao.insertRule(rule)
    }

    suspend fun toggleRule(rule: CustomRule, isEnabled: Boolean) {
        customRuleDao.updateRule(rule.copy(isEnabled = isEnabled))
    }

    suspend fun deleteRule(ruleId: Long) {
        customRuleDao.deleteRuleById(ruleId)
    }

    suspend fun clearLogs() {
        blockedLogDao.clearAllLogs()
    }

    suspend fun clearAllLogs() {
        blockedLogDao.clearAllLogs()
    }

    suspend fun resetStreak(reflection: String = "") {
        val current = recoveryDao.getProfileOnce() ?: RecoveryProfile()
        recoveryDao.saveProfile(
            current.copy(
                streakStartTimestamp = System.currentTimeMillis(),
                lastRelapseTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateTargetDays(days: Int) {
        val current = recoveryDao.getProfileOnce() ?: RecoveryProfile()
        recoveryDao.saveProfile(current.copy(targetDays = days))
    }

    suspend fun getActiveRulesSnapshot(): List<CustomRule> {
        return customRuleDao.getActiveRulesList()
    }

    suspend fun seedInitialRulesIfNeeded() {
        if (customRuleDao.getRuleCount() == 0) {
            val initial = listOf(
                CustomRule(pattern = "terabox", isRegex = false, category = "TERABOX"),
                CustomRule(pattern = "teraboxapp", isRegex = false, category = "TERABOX"),
                CustomRule(pattern = "1024tera", isRegex = false, category = "TERABOX"),
                CustomRule(pattern = "nephobox", isRegex = false, category = "TERABOX"),
                CustomRule(pattern = "uc browser link", isRegex = false, category = "UC_LINK"),
                CustomRule(pattern = "bokep", isRegex = false, category = "ADULT_18+"),
                CustomRule(pattern = "pornhub", isRegex = false, category = "ADULT_18+"),
                CustomRule(pattern = "xvideos", isRegex = false, category = "ADULT_18+"),
                CustomRule(pattern = "xnxx", isRegex = false, category = "ADULT_18+"),
                CustomRule(pattern = "hentai", isRegex = false, category = "ADULT_18+"),
                CustomRule(pattern = "jav sub indo", isRegex = false, category = "ADULT_18+"),
                CustomRule(pattern = "pemersatu bangsa", isRegex = false, category = "ADULT_18+"),
                CustomRule(pattern = "onlyfans leak", isRegex = false, category = "ADULT_18+"),
                CustomRule(
                    pattern = """(?i)\b(https?://)?([a-z0-9-]+\.)*(terabox|teraboxapp|1024tera|nephobox|4funbox)\.(com|app|me|link)/.*""",
                    isRegex = true,
                    category = "TERABOX"
                ),
                CustomRule(
                    pattern = """(?i)\b(pornhub|xvideos|xnxx|xhamster|redtube|youporn|brazzers|onlyfans|nekopoi|doodstream|luluvid|nhentai|spankbang|eporner)\.(com|net|org|tv|xyz|vip|me|to|is|site)\b""",
                    isRegex = true,
                    category = "ADULT_18+"
                ),
                CustomRule(
                    pattern = """(?i)\b(bokep|porn|xxx|hentai|javhd|doodstream|nekopoi|komikdewasa|manhwa18|video18\+|ngocok|sange|vcs)\b""",
                    isRegex = true,
                    category = "ADULT_18+"
                )
            )
            customRuleDao.insertRules(initial)
        }

        if (recoveryDao.getProfileOnce() == null) {
            recoveryDao.saveProfile(RecoveryProfile())
        }
    }
}
