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




object CrmRepository {
    private val leadRepository = LeadRepository(NetworkModule.leadApi)

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




    private val _leads = MutableStateFlow<List<Lead>>(emptyList())
    val leads: StateFlow<List<Lead>> = _leads.asStateFlow()

    private val _calendarItems = MutableStateFlow(SampleData.calendarEvents)
    val calendarItems: StateFlow<List<CalendarDayItem>> = _calendarItems.asStateFlow()

    private var nextEventId = 100L

    suspend fun refreshLeads(): ApiResult<List<Lead>> {
        return when (val result = leadRepository.readAllLeads()) {
            is ApiResult.Success -> {
                _leads.value = result.data
                result
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun readLeadDetail(id: String): ApiResult<Lead> = leadRepository.readOneLead(id)

    suspend fun createLead(lead: Lead): ApiResult<Lead> {
        return when (val result = leadRepository.createLead(lead.toCreateRequest())) {
            is ApiResult.Success -> {
                _leads.update { it + result.data }
                result
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun updateLead(id: String, lead: Lead): ApiResult<Lead> {
        return when (val result = leadRepository.updateLead(id, lead)) {
            is ApiResult.Success -> {
                _leads.update { existing ->
                    existing.map { if (it.id == id) result.data else it }
                }
                result
            }
            is ApiResult.Error -> result
        }
    }


    suspend fun deleteLead(id: String): ApiResult<Unit> {
        return when (val result = leadRepository.deleteLead(id)) {
            is ApiResult.Success -> {
                // Remove from local cache so the list updates immediately
                _leads.update { it.filter { lead -> lead.id != id } }
                result
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun convertLead(id: String): ApiResult<Unit> {
        return when (val result = leadRepository.convertLead(id)) {
            is ApiResult.Success -> {
                // Remove converted lead from local cache (it no longer exists as a Lead)
                _leads.update { it.filter { lead -> lead.id != id } }
                result
            }
            is ApiResult.Error -> result
        }
    }
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
        return createLead(lead)
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

    private val taskRepository = TaskRepository(NetworkModule.taskApi)

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


    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    // Fetch all tasks (for Tasks tab)
    suspend fun refreshTasks(): ApiResult<List<Task>> {
        // Preload leads/accounts/opportunities so category filtering works
        if (_leads.value.isEmpty()) refreshLeads()
        if (_accounts.value.isEmpty()) refreshAccounts()
        if (_opportunities.value.isEmpty()) refreshOpportunities()
        return when (val result = taskRepository.readAllTasks()) {
            is ApiResult.Success -> {
                _tasks.value = result.data
                syncTasksToCalendar()
                result
            }
            is ApiResult.Error -> result
        }
    }

    // ADD this function after refreshTasks():
    fun syncTasksToCalendar() {
        val taskItems = _tasks.value
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
    suspend fun refreshTasksForParent(parentId: String, isLead: Boolean = false): ApiResult<List<Task>> {
        return taskRepository.readTasksForParent(parentId, isLead)
    }

    suspend fun createTask(
        subject: String,
        status: String = "Not Started",
        priority: String = "Normal",
        dueDate: String? = null,
        whoId: String? = null,    // ← for Lead tasks
        whatId: String? = null,   // ← for Account/Opportunity tasks
        description: String? = null,
    ): ApiResult<Task> {
        val request = SalesforceTaskCreateRequest(
            subject = subject,
            status = status,
            priority = priority,
            activityDate = dueDate,
            whoId = whoId,
            whatId = whatId,
            description = description,
        )

        return when (val result = taskRepository.createTask(request)) {
            is ApiResult.Success -> {
                _tasks.update { it + result.data }
                result
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun updateTask(
        id: String,
        subject: String,
        status: String,
        priority: String,
        dueDate: String? = null,
        whoId: String? = null,
        whatId: String? = null,
        description: String? = null,
    ): ApiResult<Task> {
        val request = SalesforceTaskCreateRequest(
            subject = subject,
            status = status,
            priority = priority,
            activityDate = dueDate,
            whoId = whoId,
            whatId = whatId,
            description = description,
        )

        return when (val result = taskRepository.updateTask(id, request)) {
            is ApiResult.Success -> {
                _tasks.update { list -> list.map { if (it.id == id) result.data else it } }
                result
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun deleteTask(id: String): ApiResult<Unit> {
        return when (val result = taskRepository.deleteTask(id)) {
            is ApiResult.Success -> {
                _tasks.update { it.filter { task -> task.id != id } }
                result
            }
            is ApiResult.Error -> result
        }
    }



}
