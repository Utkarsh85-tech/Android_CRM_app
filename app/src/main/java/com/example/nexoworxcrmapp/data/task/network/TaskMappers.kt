// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/data/task/network/TaskMappers.kt

package com.example.nexoworxcrmapp.data.task.network

import com.example.nexoworxcrmapp.data.Task

fun SalesforceTaskDto.toDomain(): Task {
    return Task(
        id = id.orEmpty(),
        subject = subject.orEmpty(),
        status = status.orEmpty(),
        priority = priority.orEmpty(),
        dueDate = activityDate.orEmpty(),
        whatId = whatId.orEmpty(),
        description = description.orEmpty(),
        whoId = whoId.orEmpty(),
    )
}
