// Step 3 of 15
// NEW FILE: app/src/main/java/com/example/nexoworxcrmapp/data/contact/network/ContactMappers.kt

package com.example.nexoworxcrmapp.data.contact.network

import com.example.nexoworxcrmapp.data.Contact

fun SalesforceContactDto.toDomain(): Contact {
    return Contact(
        id = id.orEmpty(),
        firstName = firstName.orEmpty(),
        lastName = lastName.orEmpty(),
        title = title.orEmpty(),
        phone = phone.orEmpty(),
        email = email.orEmpty(),
        accountId = accountId.orEmpty(),
        accountName = account?.name.orEmpty(),
        department = department.orEmpty(),
        description = description.orEmpty(),
    )
}
