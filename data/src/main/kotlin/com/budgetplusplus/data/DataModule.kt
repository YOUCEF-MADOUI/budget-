package com.budgetplusplus.data

import com.budgetplusplus.data.repository.LocalAccountRepository
import com.budgetplusplus.data.repository.LocalAnalyticsRepository
import com.budgetplusplus.data.repository.LocalBudgetRepository
import com.budgetplusplus.data.repository.LocalBackupRepository
import com.budgetplusplus.data.repository.LocalCategoryRepository
import com.budgetplusplus.data.repository.LocalDashboardRepository
import com.budgetplusplus.data.repository.LocalFuturePaymentRepository
import com.budgetplusplus.data.repository.LocalFavoriteRepository
import com.budgetplusplus.data.repository.LocalMediaRepository
import com.budgetplusplus.data.repository.LocalTransactionRepository
import com.budgetplusplus.data.repository.LocalRecurringRepository
import com.budgetplusplus.data.repository.LocalSearchRepository
import com.budgetplusplus.data.repository.LocalSecurityRepository
import com.budgetplusplus.domain.repository.AccountRepository
import com.budgetplusplus.domain.repository.AnalyticsRepository
import com.budgetplusplus.domain.repository.BudgetRepository
import com.budgetplusplus.domain.repository.BackupRepository
import com.budgetplusplus.domain.repository.CategoryRepository
import com.budgetplusplus.domain.repository.DashboardRepository
import com.budgetplusplus.domain.repository.FuturePaymentRepository
import com.budgetplusplus.domain.repository.FavoriteRepository
import com.budgetplusplus.domain.repository.MediaRepository
import com.budgetplusplus.domain.repository.TransactionRepository
import com.budgetplusplus.domain.repository.RecurringRepository
import com.budgetplusplus.domain.repository.SearchRepository
import com.budgetplusplus.domain.repository.SecurityRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds @Singleton abstract fun analytics(implementation: LocalAnalyticsRepository): AnalyticsRepository
    @Binds @Singleton abstract fun accounts(implementation: LocalAccountRepository): AccountRepository
    @Binds @Singleton abstract fun backup(implementation: LocalBackupRepository): BackupRepository
    @Binds @Singleton abstract fun budgets(implementation: LocalBudgetRepository): BudgetRepository
    @Binds @Singleton abstract fun categories(implementation: LocalCategoryRepository): CategoryRepository
    @Binds @Singleton abstract fun media(implementation: LocalMediaRepository): MediaRepository
    @Binds @Singleton abstract fun favorites(implementation: LocalFavoriteRepository): FavoriteRepository
    @Binds @Singleton abstract fun futurePayments(implementation: LocalFuturePaymentRepository): FuturePaymentRepository
    @Binds @Singleton abstract fun dashboard(implementation: LocalDashboardRepository): DashboardRepository
    @Binds @Singleton abstract fun recurring(implementation: LocalRecurringRepository): RecurringRepository
    @Binds @Singleton abstract fun security(implementation: LocalSecurityRepository): SecurityRepository
    @Binds @Singleton abstract fun search(implementation: LocalSearchRepository): SearchRepository
    @Binds @Singleton abstract fun transactions(implementation: LocalTransactionRepository): TransactionRepository
}
