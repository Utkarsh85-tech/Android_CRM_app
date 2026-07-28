package com.example.nexoworxcrmapp.speech

data class LeadDraft(
    val firstName: String = "",
    val lastName: String = "",
    val company: String = "",
    val phone: String = "",
    val email: String = "",
    val status: String = "New",
    val source: String = "",
    val rating: String = "",
    val description: String = "",
) {
    fun displayFields(): List<Pair<String, String>> = buildList {
        if (firstName.isNotBlank()) add("First name" to firstName)
        if (lastName.isNotBlank()) add("Last name" to lastName)
        if (company.isNotBlank()) add("Company" to company)
        if (phone.isNotBlank()) add("Phone" to phone)
        if (email.isNotBlank()) add("Email" to email)
        add("Status" to status)
        if (source.isNotBlank()) add("Source" to source)
        if (rating.isNotBlank()) add("Rating" to rating)
        if (description.isNotBlank()) add("Notes" to description)
    }

    fun missingRequired(): List<String> = buildList {
        if (company.isBlank()) add("Company")
        if (lastName.isBlank() && firstName.isBlank()) add("Name")
        if (phone.isBlank()) add("Phone")
    }
}

data class EventDraft(
    val subject: String = "",
    val startEpochMillis: Long = 0L,
    val endEpochMillis: Long = 0L,
    val location: String = "",
    val allDay: Boolean = false,
) {
    fun displayFields(): List<Pair<String, String>> = buildList {
        if (subject.isNotBlank()) add("Subject" to subject)
        if (startEpochMillis > 0) add("Start" to VoiceDateFormatter.formatDateTime(startEpochMillis))
        if (endEpochMillis > 0) add("End" to VoiceDateFormatter.formatDateTime(endEpochMillis))
        if (location.isNotBlank()) add("Location" to location)
    }

    fun missingRequired(): List<String> = buildList {
        if (subject.isBlank()) add("Subject")
        if (startEpochMillis <= 0L) add("Start date/time")
    }
}

sealed class VoiceParseResult {
    data class CreateLead(val draft: LeadDraft) : VoiceParseResult()
    data class CreateEvent(val draft: EventDraft) : VoiceParseResult()
    data class Unknown(val reason: String) : VoiceParseResult()

    val intentLabel: String
        get() = when (this) {
            is CreateLead -> "Create Lead"
            is CreateEvent -> "Create Event"
            is Unknown -> "Unknown"
        }
}

object VoiceDateFormatter {
    fun formatDateTime(epochMillis: Long): String {
        val instant = java.time.Instant.ofEpochMilli(epochMillis)
        val zoned = instant.atZone(java.time.ZoneId.systemDefault())
        return java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a")
            .format(zoned)
    }

    fun dayOfMonth(epochMillis: Long): Int {
        val instant = java.time.Instant.ofEpochMilli(epochMillis)
        return instant.atZone(java.time.ZoneId.systemDefault()).dayOfMonth
    }
}
