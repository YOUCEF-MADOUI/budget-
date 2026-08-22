package com.budgetplusplus.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetplusplus.core.model.Account
import com.budgetplusplus.core.model.AccountType
import com.budgetplusplus.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AccountsViewModel @Inject constructor(private val repository: AccountRepository) : ViewModel() {
    val accounts: StateFlow<List<Account>> = repository.observeAccounts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _hasError = MutableStateFlow(false)
    val hasError = _hasError.asStateFlow()

    fun add(name: String, type: AccountType, amountMinor: Long, onSuccess: () -> Unit) = viewModelScope.launch {
        runCatching { repository.create(name, type, amountMinor) }
            .onSuccess { _hasError.value = false; onSuccess() }
            .onFailure { _hasError.value = true }
    }

    fun archive(account: Account) = viewModelScope.launch {
        runCatching { repository.setArchived(account.id, !account.isArchived) }
            .onSuccess { _hasError.value = false }
            .onFailure { _hasError.value = true }
    }

    fun clearError() { _hasError.value = false }
}
