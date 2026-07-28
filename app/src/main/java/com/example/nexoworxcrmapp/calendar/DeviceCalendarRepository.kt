package com.example.nexoworxcrmapp.calendar

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import com.example.nexoworxcrmapp.speech.EventDraft
import java.util.TimeZone

/**
 * Writes CRM events to the device calendar via CalendarContract (Android platform API).
 */
class DeviceCalendarRepository(private val context: Context) {

    fun insertEvent(draft: EventDraft): Result<Long> = runCatching {
        val calendarId = findWritableCalendarId()
            ?: error("No writable calendar found on this device")
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, draft.subject)
            put(CalendarContract.Events.DTSTART, draft.startEpochMillis)
            put(
                CalendarContract.Events.DTEND,
                if (draft.endEpochMillis > draft.startEpochMillis) {
                    draft.endEpochMillis
                } else {
                    draft.startEpochMillis + 3_600_000
                },
            )
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.EVENT_LOCATION, draft.location)
            put(CalendarContract.Events.DESCRIPTION, "Created via Nexoworx CRM voice")
            put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
        }
        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            ?: error("Calendar insert failed")
        uri.lastPathSegment?.toLong() ?: error("Invalid calendar event id")
    }

    private fun findWritableCalendarId(): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY,
        )
        val uri = CalendarContract.Calendars.CONTENT_URI
        context.contentResolver.query(
            uri,
            projection,
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            null,
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndex(CalendarContract.Calendars._ID)
            val primaryIdx = cursor.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
            var fallback: Long? = null
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIdx)
                val primary = if (primaryIdx >= 0) cursor.getInt(primaryIdx) == 1 else false
                if (primary) return id
                if (fallback == null) fallback = id
            }
            return fallback
        }
        return null
    }
}
