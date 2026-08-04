package com.example.nexoworxcrmapp.data

import java.time.LocalDate

data class Lead(
    val id: String,
    val firstName: String,
    val lastName: String,
    val company: String,
    val status: String,
    val phone: String,
    val email: String = "",
    val source: String = "",
    val rating: String = "",
    val industry: String = "",
    val title: String = "",
    val description: String = "",
) {
    val fullName: String get() = "${firstName.trim()} ${lastName.trim()}".trim()
}


data class Account(
    val id: String,
    val name: String,
    val phone: String,
    val industry: String,
    val type: String,
    val billingCity: String,
    val billingCountry: String = "",
    val website: String = "",
    val description: String = "",
) {
    val initials: String
        get() = name.trim().split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifEmpty { "?" }
}

data class Contact(
    val id: String,
    val firstName: String,
    val lastName: String,
    val title: String = "",
    val phone: String = "",
    val email: String = "",
    val accountId: String = "",
    val accountName: String = "",
    val department: String = "",
    val description: String = "",
) {
    val fullName: String get() = "${firstName.trim()} ${lastName.trim()}".trim()

    val initials: String
        get() = fullName.trim().split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifEmpty { "?" }
}

data class LeadTask(
    val id: String,
    val subject: String,
    val dueDate: String,
    val priority: String,
    val completed: Boolean = false,
)

data class Task(
    val id: String,
    val subject: String,
    val status: String = "Not Started",   // Not Started, In Progress, Completed, Deferred
    val priority: String = "Normal",       // High, Normal, Low
    val dueDate: String = "",              // YYYY-MM-DD format from Salesforce
    val whatId: String = "",               // linked Lead/Account/Opportunity ID
    val description: String = "",
    val whoId: String = "",
) {
    val isCompleted: Boolean get() = status.equals("Completed", ignoreCase = true)

}

// Picklist values for dropdowns
val TASK_STATUSES = listOf("Not Started", "In Progress", "Completed", "Waiting on someone else", "Deferred")
val TASK_PRIORITIES = listOf("High", "Normal", "Low")


data class LeadEvent(
    val id: String,
    val subject: String,
    val timeRange: String,
    val location: String,
)


data class Opportunity(
    val id: String,
    val name: String,
    val stageName: String,
    val closeDate: String,
    val amount: Double? = null,
    val accountId: String = "",
    val accountName: String = "",
    val description: String = "",
    val probability: Double? = null,

    val type: String = "",
    val leadSource: String = "",
    val expectedRevenue: Double? = null,
    val deliveryInstallationStatus: String = "",
) {
    val isClosed: Boolean get() = stageName.startsWith("Closed", ignoreCase = true)
    val isWon: Boolean get() = stageName.equals("Closed Won", ignoreCase = true)
}

data class OpportunityLineItem(
    val id: String,
    val pricebookEntryId: String,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val totalPrice: Double,
)

data class PricebookEntry(
    val id: String,
    val name: String,
    val unitPrice: Double,
)


// A real Salesforce Event (a scheduled meeting/call) — distinct from Task,
// which has no fixed start/end time. Mirrors Task's shape: same WhoId/WhatId
// relationship fields, same REST pattern, just calendar-specific fields
// instead of Status/Priority/ActivityDate.
data class Event(
    val id: String,
    val subject: String,
    val startDateTime: String = "", // ISO 8601, e.g. 2026-08-02T15:00:00.000+0000
    val endDateTime: String = "",
    val location: String = "",
    val whatId: String = "",        // linked Account/Opportunity
    val whoId: String = "",         // linked Lead/Contact
    val description: String = "",
)

data class CalendarDayItem(
    val id: Long = 0,
    val date: Int,
    val month: Int = 5,
    val year: Int = 2026,
    val type: String,
    val subject: String,
    val time: String,
    val startEpochMillis: Long = 0L,
    val endEpochMillis: Long = 0L,
    val location: String = "",
    val deviceEventId: Long? = null,
    // Set only for items that came from a real Salesforce Event fetch.
    // null means "locally added, not (yet) confirmed against the server" —
    // e.g. a voice-created meeting. refreshEvents() only ever replaces items
    // where this is non-null, so it can never delete a local-only entry.
    val salesforceEventId: String? = null,
    val whoId: String = "",
    val whatId: String = "",
)

object SampleData {
    val calendarEvents: List<CalendarDayItem> = run {
        val today = LocalDate.now()
        val m = today.monthValue
        val y = today.year
        listOf(
            CalendarDayItem(1, date = 14, month = m, year = y, type = "event", subject = "Team Sync", time = "10:00 AM"),
            CalendarDayItem(2, date = 15, month = m, year = y, type = "event", subject = "Demo — Ashok Leyland", time = "02:00 PM"),
            CalendarDayItem(3, date = 15, month = m, year = y, type = "task", subject = "Prepare demo org", time = "Due"),
            CalendarDayItem(4, date = 16, month = m, year = y, type = "event", subject = "Intro Call — Rajesh", time = "11:00 AM"),
            CalendarDayItem(5, date = 16, month = m, year = y, type = "task", subject = "Send CPQ brochure", time = "Due"),
            CalendarDayItem(6, date = 18, month = m, year = y, type = "task", subject = "Discovery call prep", time = "Due"),
            CalendarDayItem(7, date = 20, month = m, year = y, type = "task", subject = "Schedule product demo", time = "Due"),
            CalendarDayItem(8, date = 22, month = m, year = y, type = "event", subject = "Product Demo — CPQ", time = "03:00 PM"),
        )
    }

    val voiceSamples = listOf(
        "Create a lead — Rajesh from Tata Motors, phone 9876543210",
        "Add a task — Follow up with Ashok client, due Friday, high priority",
        "Schedule event — Product demo tomorrow at 3 PM",
    )

    private val rajeshTasks = listOf(
        LeadTask("1", "Send CPQ brochure", "16 May 2026", "High"),
        LeadTask("2", "Schedule product demo", "20 May 2026", "Normal"),
        LeadTask("3", "Follow up on pricing", "10 May 2026", "High", completed = true),
    )

    private val rajeshEvents = listOf(
        LeadEvent(
            id = "1",
            subject = "Intro Call — Rajesh Sharma",
            timeRange = "16 May 2026, 11:00 AM — 12:00 PM",
            location = "Google Meet",
        ),
        LeadEvent(
            id = "2",
            subject = "Product Demo — CPQ",
            timeRange = "22 May 2026, 03:00 PM — 04:30 PM",
            location = "Tata Motors Office, Mumbai",
        ),
    )

    fun mockTasksForLead(lead: Lead): List<LeadTask> {
        if (lead.firstName.equals("Sunita", ignoreCase = true)) return emptyList()
        return rajeshTasks
    }

    fun mockEventsForLead(lead: Lead): List<LeadEvent> {
        if (lead.firstName.equals("Sunita", ignoreCase = true)) return emptyList()
        return rajeshEvents
    }
}
