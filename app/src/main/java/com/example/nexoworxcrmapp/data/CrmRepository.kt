package com.example.nexoworxcrmapp.data

import com.example.nexoworxcrmapp.data.lead.LeadRepository
import com.example.nexoworxcrmapp.data.lead.network.toCreateRequest
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.NetworkModule
import com.example.nexoworxcrmapp.speech.EventDraft
import com.example.nexoworxcrmapp.speech.LeadDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.ZoneId
import com.example.nexoworxcrmapp.data.account.AccountRepository
import com.example.nexoworxcrmapp.data.opportunity.OpportunityRepository
import com.example.nexoworxcrmapp.data.opportunity.network.SalesforceOpportunityCreateRequest
import com.example.nexoworxcrmapp.data.contact.ContactRepository
import com.example.nexoworxcrmapp.data.contact.network.SalesforceContactCreateRequest
import com.example.nexoworxcrmapp.data.task.TaskRepository
 import com.example.nexoworxcrmapp.data.task.network.SalesforceTaskCreateRequest
import com.example.nexoworxcrmapp.data.email.EmailRepository
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatterBuilder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object CrmRepository {
    // Salesforce always sends offsets without a colon (e.g. "+0000"), which
    // java.time.OffsetDateTime.parse()'s default ISO_OFFSET_DATE_TIME formatter
    // rejects. This formatter matches Salesforce's exact wire format.
    private val salesforceDateTimeFormatter = DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        .appendOffset("+HHMM", "+0000")
        .toFormatter()

    private val opportunityRepository = OpportunityRepository(NetworkModule.opportunityApi)

    private val _opportunities = MutableStateFlow<List<Opportunity>>(emptyList())
    val opportunities: StateFlow<List<Opportunity>> = _opportunities.asStateFlow()

    suspend fun refreshOpportunities(): ApiResult<List<Opportunity>> {
        return when (val result = opportunityRepository.readAllOpportunities()) {
            is ApiResult.Success -> {
                _opportunities.value = result.data
                result
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun readOpportunityDetail(id: String): ApiResult<Opportunity> =
        opportunityRepository.readOneOpportunity(id)

    suspend fun createOpportunity(
        name: String,
        stageName: String,
        closeDate: String,
        amount: Double? = null,
        accountId: String? = null,
        description: String? = null,
        type: String? = null,
        leadSource: String? = null,
        deliveryInstallationStatus: String? = null,
    ): ApiResult<Opportunity> {
        val request = SalesforceOpportunityCreateRequest(
            name = name,
            stageName = stageName,
            closeDate = closeDate,
            amount = amount,
            accountId = accountId,
            description = description,
        )
        return when (val result = opportunityRepository.createOpportunity(request)) {
            is ApiResult.Success -> {
                _opportunities.update { it + result.data }
                result
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun updateOpportunity(
        id: String,
        name: String,
        stageName: String,
        closeDate: String,
        amount: Double? = null,
        accountId: String? = null,
        description: String? = null,
        type: String? = null,
        leadSource: String? = null,
        deliveryInstallationStatus: String? = null,
    ): ApiResult<Opportunity> {
        val request = SalesforceOpportunityCreateRequest(
            name = name,
            stageName = stageName,
            closeDate = closeDate,
            amount = amount,
            accountId = accountId,
            description = description,
            type = type,
            leadSource = leadSource,
            deliveryInstallationStatus = deliveryInstallationStatus,
        )
        return when (val result = opportunityRepository.updateOpportunity(id, request)) {
            is ApiResult.Success -> {
                _opportunities.update { list -> list.map { if (it.id == id) result.data else it } }
                result
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun deleteOpportunity(id: String): ApiResult<Unit> {
        return when (val result = opportunityRepository.deleteOpportunity(id)) {
            is ApiResult.Success -> {
                _opportunities.update { list -> list.filter { it.id != id } }
                result
            }
            is ApiResult.Error -> result
        }
    }

    private val accountRepository = AccountRepository(NetworkModule.accountApi)

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    suspend fun refreshAccounts(): ApiResult<List<Account>> {
        return when (val result = accountRepository.readAllAccounts()) {
            is ApiResult.Success -> {
                _accounts.value = result.data
                result
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun readAccountDetail(id: String): ApiResult<Account> =
        accountRepository.readOneAccount(id)

    suspend fun createAccount(account: Account): ApiResult<Account> {
        return when (val result = accountRepository.createAccount(account)) {
            is ApiResult.Success -> {
                _accounts.update { it + result.data }
                result
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun updateAccount(id: String, account: Account): ApiResult<Account> {
        return when (val result = accountRepository.updateAccount(id, account)) {
            is ApiResult.Success -> {
                _accounts.update { existing ->
                    existing.map { if (it.id == id) result.data else it }
                }
                result
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun deleteAccount(id: String): ApiResult<Unit> {
        return when (val result = accountRepository.deleteAccount(id)) {
            is ApiResult.Success -> {
                // Remove from local cache so the list updates immediately
                _accounts.update { it.filter { account -> account.id != id } }
                result
            }
            is ApiResult.Error -> result
        }
    }




    val leads: StateFlow<List<Lead>> =
        NetworkModule.leadRepository.observeLeads()
            .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, emptyList())

    private val _calendarItems = MutableStateFlow(SampleData.calendarEvents)
    val calendarItems: StateFlow<List<CalendarDayItem>> = _calendarItems.asStateFlow()

    private var nextEventId = 100L

    suspend fun refreshLeads(): ApiResult<Unit> {
        NetworkModule.syncManager.sync()
        return ApiResult.Success(Unit)
    }

    suspend fun readLeadDetail(id: String): Lead? =
        leads.value.find { it.id == id }

    suspend fun createLead(lead: Lead): Lead = NetworkModule.leadRepository.createLead(lead)

    suspend fun updateLead(id: String, lead: Lead) = NetworkModule.leadRepository.updateLead(id, lead)

    suspend fun deleteLead(id: String) = NetworkModule.leadRepository.deleteLead(id)

    suspend fun convertLead(id: String): ApiResult<Unit> = NetworkModule.leadRepository.convertLead(id)
    suspend fun createLeadFromVoice(draft: LeadDraft): ApiResult<Lead> {
        val lead = Lead(
            id = "",
            firstName = draft.firstName,
            lastName = draft.lastName,
            company = draft.company,
            status = draft.status,
            phone = draft.phone,
            email = draft.email,
            source = draft.source.ifBlank { "Voice" },
            rating = draft.rating,
            description = draft.description,
        )
        return ApiResult.Success(createLead(lead))
    }

    fun addEventFromVoice(draft: EventDraft, deviceEventId: Long? = null): CalendarDayItem {
        val zone = ZoneId.systemDefault()
        val start = Instant.ofEpochMilli(draft.startEpochMillis).atZone(zone)
        val timeLabel = java.time.format.DateTimeFormatter.ofPattern("h:mm a").format(start)
        val item = CalendarDayItem(
            id = nextEventId++,
            date = start.dayOfMonth,
            month = start.monthValue,
            year = start.year,
            type = "event",
            subject = draft.subject,
            time = timeLabel,
            startEpochMillis = draft.startEpochMillis,
            endEpochMillis = draft.endEpochMillis,
            location = draft.location,
            deviceEventId = deviceEventId,
        )
        _calendarItems.update { items -> items + item }
        return item
    }
    private val contactRepository = ContactRepository(NetworkModule.contactApi)

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    suspend fun refreshContacts(): ApiResult<List<Contact>> {
        return when (val result = contactRepository.readAllContacts()) {
            is ApiResult.Success -> {
                _contacts.value = result.data
                result
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun readContactDetail(id: String): ApiResult<Contact> =
        contactRepository.readOneContact(id)

    suspend fun createContact(
        firstName: String? = null,
        lastName: String,
        title: String? = null,
        phone: String? = null,
        email: String? = null,
        accountId: String? = null,
        department: String? = null,
        description: String? = null,
    ): ApiResult<Contact> {
        val request = SalesforceContactCreateRequest(
            firstName = firstName,
            lastName = lastName,
            title = title,
            phone = phone,
            email = email,
            accountId = accountId,
            department = department,
            description = description,
        )
        return when (val result = contactRepository.createContact(request)) {
            is ApiResult.Success -> {
                _contacts.update { it + result.data }
                result
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun updateContact(
        id: String,
        firstName: String? = null,
        lastName: String,
        title: String? = null,
        phone: String? = null,
        email: String? = null,
        accountId: String? = null,
        department: String? = null,
        description: String? = null,
    ): ApiResult<Contact> {
        val request = SalesforceContactCreateRequest(
            firstName = firstName,
            lastName = lastName,
            title = title,
            phone = phone,
            email = email,
            accountId = accountId,
            department = department,
            description = description,
        )
        return when (val result = contactRepository.updateContact(id, request)) {
            is ApiResult.Success -> {
                _contacts.update { list -> list.map { if (it.id == id) result.data else it } }
                result
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun deleteContact(id: String): ApiResult<Unit> {
        return when (val result = contactRepository.deleteContact(id)) {
            is ApiResult.Success -> {
                _contacts.update { it.filter { contact -> contact.id != id } }
                result
            }
            is ApiResult.Error -> result
        }
    }



    private val eventRepository = com.example.nexoworxcrmapp.data.event.EventRepository(NetworkModule.eventApi)

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    // Fetch all real Salesforce meetings (for Calendar screen + Home's Meetings stat)
    suspend fun refreshEvents(): ApiResult<List<Event>> {
        return when (val result = eventRepository.readAllEvents()) {
            is ApiResult.Success -> {
                _events.value = result.data
                syncEventsToCalendar()
                result
            }
            is ApiResult.Error -> result
        }
    }

    // Turns real Events into calendar-grid entries. Only ever replaces items
    // that were previously populated FROM a Salesforce fetch (salesforceEventId
    // != null) — anything locally-added (e.g. a future voice-created meeting,
    // which has salesforceEventId == null) is left untouched, so this can
    // never silently delete it.
    private fun syncEventsToCalendar() {
        val zone = java.time.ZoneId.systemDefault()
        val eventItems = _events.value.mapNotNull { event ->
            try {
                if (event.startDateTime.isBlank()) return@mapNotNull null
                val start = OffsetDateTime.parse(event.startDateTime, salesforceDateTimeFormatter).atZoneSameInstant(zone)
                val end = if (event.endDateTime.isNotBlank()) {
                    OffsetDateTime.parse(event.endDateTime, salesforceDateTimeFormatter).atZoneSameInstant(zone)
                } else {
                    start
                }
                CalendarDayItem(
                    id = event.id.hashCode().toLong(),
                    date = start.dayOfMonth,
                    month = start.monthValue,
                    year = start.year,
                    type = "event",
                    subject = event.subject,
                    time = java.time.format.DateTimeFormatter.ofPattern("h:mm a").format(start),
                    startEpochMillis = start.toInstant().toEpochMilli(),
                    endEpochMillis = end.toInstant().toEpochMilli(),
                    location = event.location,
                    salesforceEventId = event.id,
                    whoId = event.whoId,
                    whatId = event.whatId,
                )
            } catch (e: Exception) {
                null
            }
        }
        _calendarItems.update { existing ->
            existing.filter { it.salesforceEventId == null } + eventItems
        }
    }

    // Create a real Salesforce meeting. startEpochMillis/endEpochMillis are
    // plain device-local timestamps from the date/time pickers; converted to
    // the ISO format Salesforce expects.
    suspend fun createEvent(
        subject: String,
        startEpochMillis: Long,
        endEpochMillis: Long,
        location: String = "",
        whatId: String? = null,
        whoId: String? = null,
        description: String? = null,
    ): ApiResult<Event> {
        val zone = java.time.ZoneId.systemDefault()
        val isoFormatter = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val startIso = java.time.Instant.ofEpochMilli(startEpochMillis).atZone(zone).format(isoFormatter)
        val endIso = java.time.Instant.ofEpochMilli(endEpochMillis).atZone(zone).format(isoFormatter)
        val request = com.example.nexoworxcrmapp.data.event.network.SalesforceEventCreateRequest(
            subject = subject,
            startDateTime = startIso,
            endDateTime = endIso,
            location = location.ifBlank { null },
            whatId = whatId,
            whoId = whoId,
            description = description,
        )
        return when (val result = eventRepository.createEvent(request)) {
            is ApiResult.Success -> {
                // Re-fetch everything instead of manually merging the one new
                // item in — this guarantees the new event goes through the
                // exact same parsing path as every other event you can
                // already see, instead of a separate shortcut that could
                // silently disagree with it.
                refreshEvents()
                result
            }
            is ApiResult.Error -> result
        }
    }

    private val emailRepository = EmailRepository(NetworkModule.emailApi)
    suspend fun sendEmailToLead(
        toAddress: String,
        subject: String,
        body: String,
        senderAddress: String,
        leadId: String,
    ): ApiResult<Unit> {
        return emailRepository.sendEmail(
            toAddress = toAddress,
            subject = subject,
            body = body,
            senderAddress = senderAddress,
            leadId = leadId,
        )
    }


    val tasks: StateFlow<List<Task>> =
        NetworkModule.taskRepository.observeTasks()
            .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, emptyList())
            .also { flow ->
                CoroutineScope(Dispatchers.IO).launch { flow.collect { syncTasksToCalendar() } }
            }

    suspend fun refreshTasks(): ApiResult<Unit> {
        if (leads.value.isEmpty()) refreshLeads()
        if (_accounts.value.isEmpty()) refreshAccounts()
        if (_opportunities.value.isEmpty()) refreshOpportunities()
        NetworkModule.syncManager.sync()
        return ApiResult.Success(Unit)
    }

    // ADD this function after refreshTasks():
    fun syncTasksToCalendar() {
        val taskItems = tasks.value
            .filter { it.dueDate.isNotBlank() }
            .mapNotNull { task ->
                try {
                    val parts = task.dueDate.split("-")
                    if (parts.size != 3) return@mapNotNull null
                    val year = parts[0].toInt()
                    val month = parts[1].toInt()
                    val date = parts[2].toInt()
                    CalendarDayItem(
                        id = task.id.hashCode().toLong(),
                        date = date,
                        month = month,
                        year = year,
                        type = "task",
                        subject = task.subject,
                        time = "Due",
                    )
                } catch (e: Exception) { null }
            }
        // Merge with existing calendar items (keep events, replace task items)
        _calendarItems.update { existing ->
            existing.filter { it.type != "task" } + taskItems
        }
    }

    // Fetch tasks for a specific Lead/Account/Opportunity
    suspend fun refreshTasksForParent(parentId: String, isLead: Boolean = false): ApiResult<List<Task>> =
        ApiResult.Success(tasks.value.filter { (isLead && it.whoId == parentId) || (!isLead && it.whatId == parentId) })

    suspend fun createTask(
        subject: String, status: String = "Not Started", priority: String = "Normal",
        dueDate: String? = null, whoId: String? = null, whatId: String? = null, description: String? = null,
    ): Task = NetworkModule.taskRepository.createTask(subject, status, priority, dueDate, whoId, whatId, description)

    suspend fun updateTask(
        id: String, subject: String, status: String, priority: String,
        dueDate: String? = null, whoId: String? = null, whatId: String? = null, description: String? = null,
    ) = NetworkModule.taskRepository.updateTask(id, subject, status, priority, dueDate, whoId, whatId, description)

    suspend fun deleteTask(id: String) = NetworkModule.taskRepository.deleteTask(id)



}
