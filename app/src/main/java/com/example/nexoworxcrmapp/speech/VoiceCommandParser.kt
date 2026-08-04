package com.example.nexoworxcrmapp.speech

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * Rule-based NLU for CRM voice commands (no cloud API).
 * Works with transcripts from Vosk or typed sample phrases.
 */
class VoiceCommandParser(
    private val clock: () -> LocalDateTime = { LocalDateTime.now() },
) {
    fun parse(transcript: String): VoiceParseResult {
        val text = transcript.trim()
        if (text.isBlank()) return VoiceParseResult.Unknown("Empty speech")

        val normalized = text.lowercase(Locale.US)
        return when {
            isLeadIntent(normalized) -> parseLead(text, normalized)
            isAccountIntent(normalized) -> parseAccount(text, normalized)
            isOpportunityIntent(normalized) -> parseOpportunity(text, normalized)
            isTaskIntent(normalized) -> parseTask(text, normalized)
            isEventIntent(normalized) -> parseEvent(text, normalized)
            else -> VoiceParseResult.Unknown(
                "Say \"create a lead\", \"add an account\", \"create a deal\", \"add a task\", or \"schedule an event\"",
            )
        }
    }

    private fun isAccountIntent(text: String): Boolean =
        listOf("create an account", "create account", "new account", "add an account", "add account")
            .any { text.contains(it) }

    private fun isOpportunityIntent(text: String): Boolean =
        listOf(
            "create an opportunity", "create opportunity", "new opportunity", "add an opportunity", "add opportunity",
            "create a deal", "create deal", "new deal", "add a deal", "add deal", "log an opportunity", "log a deal",
        ).any { text.contains(it) }

    private fun isTaskIntent(text: String): Boolean =
        listOf("create a task", "create task", "new task", "add a task", "add task", "remind me to")
            .any { text.contains(it) }

    private fun isLeadIntent(text: String): Boolean =
        listOf("create a lead", "create lead", "new lead", "add a lead", "add lead").any { text.contains(it) }

    private fun isEventIntent(text: String): Boolean =
        listOf(
            "schedule event", "schedule an event", "create event", "create an event",
            "book meeting", "book a meeting", "add event", "add an event",
        ).any { text.contains(it) }

    private fun parseLead(original: String, normalized: String): VoiceParseResult {
        val body = extractBody(original, normalized, leadPrefixes)
        val phone = extractPhone(body) ?: ""
        val email = extractEmail(body) ?: ""
        val company = extractCompany(body) ?: ""
        val (first, last) = extractPersonName(body)
        val source = extractSource(body)
        val rating = extractRating(body)

        val draft = LeadDraft(
            firstName = first,
            lastName = last,
            company = company,
            phone = phone,
            email = email,
            source = source,
            rating = rating,
            description = body,
        )
        return if (draft.missingRequired().isNotEmpty()) {
            VoiceParseResult.CreateLead(draft)
        } else {
            VoiceParseResult.CreateLead(draft)
        }
    }

    private fun parseAccount(original: String, normalized: String): VoiceParseResult {
        val body = extractBody(original, normalized, accountPrefixes)
        val phone = extractPhone(body) ?: ""
        val website = extractWebsite(body)
        val industry = extractIndustry(body)
        val name = extractAccountName(body)

        val draft = AccountDraft(
            name = name,
            phone = phone,
            industry = industry,
            website = website,
            description = body,
        )
        return VoiceParseResult.CreateAccount(draft)
    }

    private fun parseOpportunity(original: String, normalized: String): VoiceParseResult {
        val body = extractBody(original, normalized, opportunityPrefixes)
        val amount = extractAmount(body) ?: ""
        val name = extractOpportunityName(body)
        val closeDate = extractDateOnly(normalized)

        val draft = OpportunityDraft(
            name = name,
            amount = amount,
            closeDate = closeDate,
            description = body,
        )
        return VoiceParseResult.CreateOpportunity(draft)
    }

    private fun parseTask(original: String, normalized: String): VoiceParseResult {
        val body = extractBody(original, normalized, taskPrefixes)
        val priority = extractPriority(normalized)
        val dueDate = extractDateOnly(normalized)
        val subject = extractTaskSubject(body)

        val draft = TaskDraft(
            subject = subject,
            priority = priority,
            dueDate = dueDate,
            description = body,
        )
        return VoiceParseResult.CreateTask(draft)
    }

    private fun extractWebsite(text: String): String =
        Regex("""\b((?:www\.)?[a-z0-9-]+\.(?:com|in|org|net|io|co))\b""", RegexOption.IGNORE_CASE)
            .find(text)?.value.orEmpty()

    private fun extractIndustry(text: String): String {
        val industries = listOf(
            "Technology", "Manufacturing", "Healthcare", "Finance", "Retail",
            "Education", "Construction", "Agriculture", "Hospitality", "Transportation",
        )
        return industries.firstOrNull { text.contains(it, ignoreCase = true) } ?: ""
    }

    private fun extractAccountName(text: String): String {
        var name = text
        name = name.split(Regex("""\s+(?:phone|number|website|industry)\s+""", RegexOption.IGNORE_CASE)).firstOrNull() ?: name
        name = name.split(",").firstOrNull()?.trim().orEmpty()
        if (name.isBlank()) name = text.take(60).trim()
        return name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
    }

    private fun extractAmount(text: String): String? {
        val match = Regex(
            """(?:worth|of|for|amount)?\s*(?:₹|rs\.?|inr)?\s*([\d,]{3,}(?:\.\d+)?)\s*(k|thousand|lakh|lakhs|l|crore|cr)?""",
            RegexOption.IGNORE_CASE,
        ).find(text) ?: return null
        val raw = match.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null
        val multiplier = when (match.groupValues[2].lowercase()) {
            "k", "thousand" -> 1_000.0
            "lakh", "lakhs", "l" -> 100_000.0
            "crore", "cr" -> 10_000_000.0
            else -> 1.0
        }
        val value = raw * multiplier
        return if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
    }

    private fun extractOpportunityName(text: String): String {
        var name = text
        name = name.split(Regex("""\s+worth\s+""", RegexOption.IGNORE_CASE)).firstOrNull() ?: name
        name = name.split(Regex("""\s+for\s+₹|\s+for\s+rs""", RegexOption.IGNORE_CASE)).firstOrNull() ?: name
        name = name.split(",").firstOrNull()?.trim().orEmpty()
        if (name.length < 3) name = text.take(60).trim()
        return name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
    }

    private fun extractPriority(text: String): String = when {
        text.contains("urgent") || text.contains("high priority") -> "High"
        text.contains("low priority") -> "Low"
        else -> "Normal"
    }

    private fun extractTaskSubject(body: String): String {
        var subject = body
        listOf(
            Regex("""tomorrow.*""", RegexOption.IGNORE_CASE),
            Regex("""next\s+\w+.*""", RegexOption.IGNORE_CASE),
            Regex("""due\s+.*""", RegexOption.IGNORE_CASE),
            Regex("""by\s+\w+.*""", RegexOption.IGNORE_CASE),
        ).forEach { subject = subject.replace(it, "").trim() }
        subject = subject.split(",").firstOrNull()?.trim().orEmpty()
        if (subject.length < 3) subject = body.take(60).trim()
        return subject.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
    }

    // Date only (no time) — used for Task due dates and Opportunity close dates.
    // Returns "YYYY-MM-DD", matching what Salesforce's Date fields expect.
    private fun extractDateOnly(normalized: String): String {
        val now = clock()
        var date = now.toLocalDate()
        when {
            normalized.contains("tomorrow") -> date = date.plusDays(1)
            normalized.contains("today") -> Unit
            normalized.contains("next week") -> date = date.plusWeeks(1)
            else -> {
                val day = DayOfWeek.entries.firstOrNull { normalized.contains(it.name.lowercase()) }
                if (day != null) date = date.with(TemporalAdjusters.next(day))
            }
        }
        return date.toString()
    }

    private fun parseEvent(original: String, normalized: String): VoiceParseResult {
        val body = extractBody(original, normalized, eventPrefixes)
        val (start, end) = extractDateTime(body, normalized)
        val location = extractLocation(body)
        val subject = extractEventSubject(body)

        val draft = EventDraft(
            subject = subject,
            startEpochMillis = start?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: 0L,
            endEpochMillis = end?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: 0L,
            location = location,
        )
        return VoiceParseResult.CreateEvent(draft)
    }

    private fun extractBody(original: String, normalized: String, prefixes: List<String>): String {
        var body = original
        for (prefix in prefixes) {
            val idx = normalized.indexOf(prefix)
            if (idx >= 0) {
                body = original.substring(idx + prefix.length).trim()
                body = body.removePrefix("—").removePrefix("-").removePrefix(":").trim()
                break
            }
        }
        return body
    }

    private fun extractPhone(text: String): String? {
        val match = Regex("""(?:phone|mobile|call)?\s*[:#]?\s*([\d\s\-]{10,})""", RegexOption.IGNORE_CASE)
            .find(text) ?: Regex("""\b(\d[\d\s\-]{8,}\d)\b""").find(text)
        return match?.groupValues?.get(1)?.replace(Regex("[^0-9]"), "")?.takeIf { it.length >= 10 }
            ?.let { formatPhone(it) }
    }

    private fun formatPhone(digits: String): String {
        val d = digits.takeLast(10)
        return if (d.length == 10) {
            "${d.substring(0, 5)} ${d.substring(5)}"
        } else {
            digits
        }
    }

    private fun extractEmail(text: String): String? =
        Regex("""[\w.+-]+@[\w.-]+\.\w+""").find(text)?.value

    private fun extractCompany(text: String): String? {
        val patterns = listOf(
            Regex("""from\s+(.+?)(?:,|\s+phone|\s+email|\s+status|$)""", RegexOption.IGNORE_CASE),
            Regex("""at\s+(.+?)(?:,|\s+phone|\s+on\s+|\s+tomorrow|$)""", RegexOption.IGNORE_CASE),
            Regex("""company\s+(.+?)(?:,|$)""", RegexOption.IGNORE_CASE),
        )
        for (pattern in patterns) {
            val match = pattern.find(text)?.groupValues?.get(1)?.trim()
            if (!match.isNullOrBlank() && match.length > 2) return match.trimEnd('.', ' ')
        }
        return null
    }

    private fun extractPersonName(text: String): Pair<String, String> {
        val nameMatch = Regex(
            """^(?:for\s+)?([A-Z][a-z]+)(?:\s+([A-Z][a-z]+))?(?:\s+from|\s+at\s|,|$)""",
        ).find(text.trim())
        if (nameMatch != null) {
            val first = nameMatch.groupValues[1]
            val last = nameMatch.groupValues.getOrNull(2).orEmpty()
            if (last.isNotBlank()) return first to last
            return first to ""
        }
        val simple = Regex("""\b([A-Z][a-z]+)\b""").findAll(text).map { it.value }.toList()
        return when {
            simple.size >= 2 -> simple[0] to simple[1]
            simple.size == 1 -> simple[0] to ""
            else -> "" to ""
        }
    }

    private fun extractSource(text: String): String {
        val sources = listOf("Cold Call", "Web", "Partner Referral", "Trade Show", "Advertisement", "Referral")
        return sources.firstOrNull { text.contains(it, ignoreCase = true) } ?: ""
    }

    private fun extractRating(text: String): String {
        return when {
            text.contains("hot", ignoreCase = true) -> "Hot"
            text.contains("warm", ignoreCase = true) -> "Warm"
            text.contains("cold", ignoreCase = true) -> "Cold"
            else -> ""
        }
    }

    private fun extractLocation(text: String): String {
        val patterns = listOf(
            Regex("""(?:at|in|location)\s+(.+?)(?:,|$)""", RegexOption.IGNORE_CASE),
            Regex("""(?:on\s+)(zoom|google meet|teams)""", RegexOption.IGNORE_CASE),
        )
        for (pattern in patterns) {
            val value = pattern.find(text)?.groupValues?.get(1)?.trim()
            if (!value.isNullOrBlank()) return value.trimEnd('.', ' ')
        }
        return ""
    }

    private fun extractEventSubject(body: String): String {
        var subject = body
        listOf(
            Regex("""tomorrow.*""", RegexOption.IGNORE_CASE),
            Regex("""next\s+\w+.*""", RegexOption.IGNORE_CASE),
            Regex("""at\s+\d.*""", RegexOption.IGNORE_CASE),
            Regex("""due\s+.*""", RegexOption.IGNORE_CASE),
            Regex("""phone\s+.*""", RegexOption.IGNORE_CASE),
        ).forEach { subject = subject.replace(it, "").trim() }
        subject = subject.removeSuffix(" tomorrow").removeSuffix(" today").trim()
        subject = subject.split(Regex("""\s+at\s+""", RegexOption.IGNORE_CASE)).firstOrNull() ?: subject
        subject = subject.split(",").firstOrNull()?.trim().orEmpty()
        if (subject.length < 3) {
            subject = body.take(60).trim()
        }
        return subject.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
    }

    private fun extractDateTime(body: String, normalized: String): Pair<LocalDateTime?, LocalDateTime?> {
        val now = clock()
        var date = now.toLocalDate()
        when {
            normalized.contains("tomorrow") -> date = date.plusDays(1)
            normalized.contains("today") -> Unit
            normalized.contains("next week") -> date = date.plusWeeks(1)
            else -> {
                val day = DayOfWeek.entries.firstOrNull { normalized.contains(it.name.lowercase()) }
                if (day != null) {
                    date = date.with(TemporalAdjusters.next(day))
                }
            }
        }
        val time = extractTime(normalized) ?: LocalTime.of(9, 0)
        val start = LocalDateTime.of(date, time)
        val end = start.plusHours(1)
        return start to end
    }

    private fun extractTime(text: String): LocalTime? {
        Regex("""(\d{1,2})\s*:\s*(\d{2})\s*(am|pm)?""", RegexOption.IGNORE_CASE).find(text)?.let { m ->
            var hour = m.groupValues[1].toInt()
            val minute = m.groupValues[2].toInt()
            val ampm = m.groupValues[3].lowercase()
            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0
            return LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        }
        Regex("""(\d{1,2})\s*(am|pm)""", RegexOption.IGNORE_CASE).find(text)?.let { m ->
            var hour = m.groupValues[1].toInt()
            val ampm = m.groupValues[2].lowercase()
            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0
            return LocalTime.of(hour.coerceIn(0, 23), 0)
        }
        return null
    }

    companion object {
        private val leadPrefixes = listOf(
            "create a lead", "create lead", "new lead", "add a lead", "add lead",
        )
        private val accountPrefixes = listOf(
            "create an account", "create account", "new account", "add an account", "add account",
        )
        private val opportunityPrefixes = listOf(
            "create an opportunity", "create opportunity", "new opportunity", "add an opportunity", "add opportunity",
            "create a deal", "create deal", "new deal", "add a deal", "add deal", "log an opportunity", "log a deal",
        )
        private val taskPrefixes = listOf(
            "create a task", "create task", "new task", "add a task", "add task", "remind me to",
        )
        private val eventPrefixes = listOf(
            "schedule an event", "schedule event", "create an event", "create event",
            "book a meeting", "book meeting", "add an event", "add event",
        )
    }
}
