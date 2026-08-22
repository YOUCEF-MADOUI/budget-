package com.budgetplusplus.data.repository

import com.budgetplusplus.core.model.Account
import com.budgetplusplus.core.model.AccountType
import com.budgetplusplus.core.model.BalancePoint
import com.budgetplusplus.core.model.CategoryTotal
import com.budgetplusplus.core.model.DashboardData
import com.budgetplusplus.core.model.DailyCashFlow
import com.budgetplusplus.core.model.FinanceTransaction
import com.budgetplusplus.core.model.TransactionType
import com.budgetplusplus.database.dao.AccountDao
import com.budgetplusplus.database.dao.DashboardDao
import com.budgetplusplus.domain.dashboard.BalanceEvolutionCalculator
import com.budgetplusplus.domain.repository.DashboardRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class LocalDashboardRepository @Inject constructor(
    private val accounts: AccountDao,
    private val dashboard: DashboardDao,
) : DashboardRepository {
    override fun observeDashboard(fromInclusive: Long, toExclusive: Long): Flow<DashboardData> {
        val core = combine(
            accounts.observeAll(),
            dashboard.observePeriodTotals(fromInclusive, toExclusive),
            dashboard.observeOpeningBalance(fromInclusive),
            dashboard.observeDailyDeltas(fromInclusive, toExclusive),
            dashboard.observeCategoryTotals(fromInclusive, toExclusive),
        ) { accountRows, totals, opening, deltas, categories ->
            val accountModels = accountRows.map { row -> Account(row.id, row.name, AccountType.valueOf(row.type), row.currencyCode, row.initialBalanceMinor, row.currentBalanceMinor, row.isArchived) }
            DashboardData(
                totalBalanceMinor = accountModels.filterNot(Account::isArchived).sumOf(Account::currentBalanceMinor),
                incomeMinor = totals.incomeMinor,
                expenseMinor = totals.expenseMinor,
                accounts = accountModels.filterNot(Account::isArchived),
                evolution = BalanceEvolutionCalculator.calculate(fromInclusive, opening, deltas.map { delta -> DailyCashFlow(LocalDate.parse(delta.localDate).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), delta.incomeMinor, delta.expenseMinor) }),
                categoryTotals = categories.map { CategoryTotal(it.categoryId, it.nameKey, it.customName, it.amountMinor) },
                currencyCode = accountModels.firstOrNull()?.currencyCode ?: "DZD",
            )
        }
        return combine(core, dashboard.observeRecent(fromInclusive, toExclusive, limit = 5)) { data, recent ->
            data.copy(recentTransactions = recent.map { row ->
                FinanceTransaction(
                    id = row.id,
                    type = TransactionType.valueOf(row.type),
                    amountMinor = row.amountMinor,
                    currencyCode = row.currencyCode,
                    accountId = row.accountId,
                    accountName = row.accountName,
                    destinationAccountId = row.destinationAccountId,
                    destinationAccountName = row.destinationAccountName,
                    categoryId = row.categoryId,
                    categoryNameKey = row.categoryNameKey,
                    categoryCustomName = row.categoryCustomName,
                    occurredAt = row.occurredAt,
                    description = row.description,
                    subcategoryId = row.subcategoryId,
                    subcategoryName = row.subcategoryName,
                )
            })
        }
    }

}
