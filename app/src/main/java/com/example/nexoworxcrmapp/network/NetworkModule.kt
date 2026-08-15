package com.example.nexoworxcrmapp.network


import com.example.nexoworxcrmapp.BuildConfig
import com.example.nexoworxcrmapp.data.lead.network.LeadApiService
import com.example.nexoworxcrmapp.network.auth.AuthApiService
import com.example.nexoworxcrmapp.network.auth.AuthInterceptor
import com.example.nexoworxcrmapp.network.auth.SalesforceAuthManager
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.nexoworxcrmapp.data.account.network.AccountApiService
import com.example.nexoworxcrmapp.data.opportunity.network.OpportunityApiService
import com.example.nexoworxcrmapp.data.contact.network.ContactApiService
import com.example.nexoworxcrmapp.data.task.network.TaskApiService
import com.example.nexoworxcrmapp.data.email.network.EmailApiService
import com.example.nexoworxcrmapp.data.opportunity.ProductRepository
import com.example.nexoworxcrmapp.data.attachment.AttachmentApiService
import com.example.nexoworxcrmapp.data.attachment.AttachmentRepository
import com.example.nexoworxcrmapp.data.quote.CpqApiService
import com.example.nexoworxcrmapp.data.quote.CpqRepository
import com.example.nexoworxcrmapp.data.account.ContractApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
object NetworkModule {
    private val gson = GsonBuilder().create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val authRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(NetworkConfig.instanceUrl)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build(),
            )
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authManager: SalesforceAuthManager by lazy {
        SalesforceAuthManager(authApi)
    }

    private val authApi: AuthApiService by lazy {
        authRetrofit.create(AuthApiService::class.java)
    }

    private val authenticatedClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val apiRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(NetworkConfig.instanceUrl)
            .client(authenticatedClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val leadApi: LeadApiService by lazy {
        apiRetrofit.create(LeadApiService::class.java)
    }

    val opportunityApi: OpportunityApiService by lazy {
        apiRetrofit.create(OpportunityApiService::class.java)
    }

    val accountApi: AccountApiService by lazy {
        apiRetrofit.create(AccountApiService::class.java)
    }

    val contactApi: ContactApiService by lazy {
        apiRetrofit.create(ContactApiService::class.java)
    }

    val taskApi: TaskApiService by lazy {
        apiRetrofit.create(TaskApiService::class.java)
    }
    val eventApi: com.example.nexoworxcrmapp.data.event.network.EventApiService by lazy {
        apiRetrofit.create(com.example.nexoworxcrmapp.data.event.network.EventApiService::class.java)
    }
    val emailApi: EmailApiService by lazy {
        apiRetrofit.create(EmailApiService::class.java)
    }

    val productRepository: ProductRepository by lazy {
        com.example.nexoworxcrmapp.data.opportunity.ProductRepository(opportunityApi)
    }
    val attachmentApi: AttachmentApiService by lazy {
        apiRetrofit.create(AttachmentApiService::class.java)
    }


    val instanceUrl: String get() = NetworkConfig.instanceUrl
    val cpqApi: CpqApiService by lazy {
        apiRetrofit.create(CpqApiService::class.java)
    }

    val cpqRepository: CpqRepository by lazy {
        CpqRepository(cpqApi)
    }
    val contractApi: ContractApiService by lazy {
        apiRetrofit.create(ContractApiService::class.java)
    }

    lateinit var appContext: android.content.Context

    val database by lazy { com.example.nexoworxcrmapp.data.local.AppDatabase.getInstance(appContext) }

    val leadRepository: com.example.nexoworxcrmapp.data.lead.LeadRepository by lazy {
        com.example.nexoworxcrmapp.data.lead.LeadRepository(leadApi, database.leadDao(), database.pendingOperationDao())
    }

    val taskRepository: com.example.nexoworxcrmapp.data.task.TaskRepository by lazy {
        com.example.nexoworxcrmapp.data.task.TaskRepository(taskApi, database.taskDao(), database.pendingOperationDao())
    }

    val attachmentRepository: com.example.nexoworxcrmapp.data.attachment.AttachmentRepository by lazy {
        com.example.nexoworxcrmapp.data.attachment.AttachmentRepository(
            attachmentApi, database.attachmentDao(), database.pendingOperationDao(), appContext,
        )
    }

    val syncManager: com.example.nexoworxcrmapp.data.sync.SyncManager by lazy {
        com.example.nexoworxcrmapp.data.sync.SyncManager(leadRepository, taskRepository, attachmentRepository, database.pendingOperationDao())
    }

    val connectivityObserver: com.example.nexoworxcrmapp.data.sync.ConnectivityObserver by lazy {
        com.example.nexoworxcrmapp.data.sync.ConnectivityObserver(appContext)
    }

    /** Fire-and-forget: if we're online, kick off a sync right away instead of
     *  waiting for the periodic worker. Safe to call as often as you like —
     *  SyncManager's mutex means overlapping calls just queue up harmlessly. */
    fun triggerSyncIfOnline() {
        if (connectivityObserver.isCurrentlyOnline()) {
            CoroutineScope(Dispatchers.IO).launch { syncManager.sync() }
        }
    }
}
