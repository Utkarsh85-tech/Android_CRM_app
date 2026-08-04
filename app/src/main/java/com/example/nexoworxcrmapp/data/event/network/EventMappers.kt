// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/data/event/network/EventMappers.kt

package com.example.nexoworxcrmapp.data.event.network

import com.example.nexoworxcrmapp.data.Event

fun SalesforceEventDto.toDomain(): Event {
    return Event(
        id = id.orEmpty(),
        subject = subject.orEmpty(),
        startDateTime = startDateTime.orEmpty(),
        endDateTime = endDateTime.orEmpty(),
        location = location.orEmpty(),
        whatId = whatId.orEmpty(),
        description = description.orEmpty(),
        whoId = whoId.orEmpty(),
    )
}