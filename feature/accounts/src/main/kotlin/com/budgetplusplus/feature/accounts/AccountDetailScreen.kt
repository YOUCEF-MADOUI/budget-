package com.budgetplusplus.feature.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.budgetplusplus.core.designsystem.components.*
import com.budgetplusplus.core.designsystem.financial.*
import com.budgetplusplus.core.designsystem.icons.BudgetIcons
import com.budgetplusplus.core.model.*
import com.budgetplusplus.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.*

@HiltViewModel class AccountDetailViewModel @Inject constructor(accounts:AccountRepository,transactions:TransactionRepository):ViewModel(){val accounts=accounts.observeAccounts().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList());val transactions=transactions.observeTransactions().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())}
@Composable fun AccountDetailScreen(accountId:String,onBack:()->Unit,onEditTransaction:(String)->Unit,vm:AccountDetailViewModel=hiltViewModel()){val accounts by vm.accounts.collectAsStateWithLifecycle();val transactions by vm.transactions.collectAsStateWithLifecycle();val account=accounts.firstOrNull{it.id==accountId};val rows=transactions.filter{it.accountId==accountId||it.destinationAccountId==accountId};Scaffold(topBar={BudgetTopAppBar(account?.name?:stringResource(R.string.account_detail_title),onBackClick=onBack)}){p->if(account==null)BudgetLoadingIndicator(Modifier.padding(p).fillMaxSize())else LazyColumn(Modifier.padding(p).fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{BalanceCard(stringResource(R.string.account_detail_balance),account.currentBalanceMinor,account.currencyCode,Modifier.fillMaxWidth())};item{Text(stringResource(R.string.account_detail_operations),style=MaterialTheme.typography.titleLarge)};if(rows.isEmpty())item{BudgetEmptyState(stringResource(R.string.account_detail_empty),stringResource(R.string.account_detail_empty_message))}else items(rows,key={it.id}){t->TransactionRow(t.categoryCustomName?:stringResource(R.string.account_detail_transaction),t.description,DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(t.occurredAt)),t.accountName,t.amountMinor,t.currencyCode,when(t.type){TransactionType.EXPENSE->TransactionVisualType.Expense;TransactionType.INCOME->TransactionVisualType.Income;TransactionType.TRANSFER->TransactionVisualType.Transfer},BudgetIcons.Category,onClick={onEditTransaction(t.id)})}}}}
