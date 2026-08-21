package com.budgetplusplus.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetplusplus.core.model.Account
import com.budgetplusplus.core.model.AccountType
import com.budgetplusplus.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AccountsViewModel @Inject constructor(private val repository: AccountRepository) : ViewModel() {
    val accounts: StateFlow<List<Account>> = repository.observeAccounts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun add(name: String, type: AccountType, amountMinor: Long) = viewModelScope.launch { repository.create(name, type, amountMinor) }
    fun archive(account: Account) = viewModelScope.launch { repository.setArchived(account.id, !account.isArchived) }
}
