package com.budgetplusplus.data

import com.budgetplusplus.data.repository.LocalAccountRepository
import com.budgetplusplus.data.repository.LocalCategoryRepository
import com.budgetplusplus.data.repository.LocalDashboardRepository
import com.budgetplusplus.data.repository.LocalTransactionRepository
import com.budgetplusplus.domain.repository.AccountRepository
import com.budgetplusplus.domain.repository.CategoryRepository
import com.budgetplusplus.domain.repository.DashboardRepository
import com.budgetplusplus.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds @Singleton abstract fun accounts(implementation: LocalAccountRepository): AccountRepository
    @Binds @Singleton abstract fun categories(implementation: LocalCategoryRepository): CategoryRepository
    @Binds @Singleton abstract fun dashboard(implementation: LocalDashboardRepository): DashboardRepository
    @Binds @Singleton abstract fun transactions(implementation: LocalTransactionRepository): TransactionRepository
}
