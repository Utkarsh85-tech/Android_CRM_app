package com.example.nexoworxcrmapp.data.account

import com.example.nexoworxcrmapp.data.Account
import com.example.nexoworxcrmapp.data.account.network.AccountApiService
import com.example.nexoworxcrmapp.data.account.network.toDomain
import com.example.nexoworxcrmapp.data.account.network.toCreateRequest
import com.example.nexoworxcrmapp.data.account.network.toPatchRequest
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.safeApiCall
import com.example.nexoworxcrmapp.network.safeApiCallEmpty

class AccountRepository(
    private val api: AccountApiService,
) {

    suspend fun readAllAccounts(): ApiResult<List<Account>> {
        return when (val result = safeApiCall { api.queryAccounts() }) {
            is ApiResult.Success -> ApiResult.Success(result.data.records.map { it.toDomain() })
            is ApiResult.Error -> result
        }
    }

    suspend fun readOneAccount(id: String): ApiResult<Account> {
        return when (val result = safeApiCall { api.getAccount(id) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    suspend fun createAccount(account: Account): ApiResult<Account> {
        return when (val result = safeApiCall { api.createAccount(account.toCreateRequest()) }) {
            is ApiResult.Success -> {
                if (!result.data.success) {
                    ApiResult.Error(message = "Create account failed")
                } else {
                    readOneAccount(result.data.id)
                }
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun updateAccount(id: String, account: Account): ApiResult<Account> {
        return when (val result = safeApiCallEmpty { api.updateAccount(id, account.toPatchRequest()) }) {
            is ApiResult.Success -> readOneAccount(id)
            is ApiResult.Error -> result
        }
    }

    /** Permanently delete an Account from Salesforce */
    suspend fun deleteAccount(id: String): ApiResult<Unit> {
        return safeApiCallEmpty { api.deleteAccount(id) }
    }
}
