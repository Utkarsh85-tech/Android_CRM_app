package com.example.nexoworxcrmapp.speech

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class VoiceCommandParserTest {

    private val monday = LocalDateTime.of(2026, 5, 25, 10, 0)
    private val parser = VoiceCommandParser { monday }

    @Test
    fun parseCreateLead_extractsCoreFields() {
        val result = parser.parse(
            "Create a lead — Rajesh from Tata Motors, phone 9876543210",
        ) as VoiceParseResult.CreateLead

        assertEquals("Rajesh", result.draft.firstName)
        assertEquals("Tata Motors", result.draft.company)
        assertTrue(result.draft.phone.contains("98765"))
        assertTrue(result.draft.missingRequired().isEmpty())
    }

    @Test
    fun parseScheduleEvent_extractsSubjectAndTime() {
        val result = parser.parse(
            "Schedule event — Product demo tomorrow at 3 PM",
        ) as VoiceParseResult.CreateEvent

        assertTrue(result.draft.subject.contains("Product demo", ignoreCase = true))
        assertTrue(result.draft.startEpochMillis > 0)
        assertTrue(result.draft.missingRequired().isEmpty())
    }

    @Test
    fun parseUnknown_returnsReason() {
        val result = parser.parse("Hello there")
        assertTrue(result is VoiceParseResult.Unknown)
    }
}
