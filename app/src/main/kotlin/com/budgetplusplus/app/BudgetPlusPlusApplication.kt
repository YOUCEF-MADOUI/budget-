package com.budgetplusplus.app

import android.app.Application
import com.budgetplusplus.data.worker.RecurringWorkScheduler
import com.budgetplusplus.data.worker.FuturePaymentReminderScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BudgetPlusPlusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RecurringWorkScheduler.enqueueCatchUp(this)
        FuturePaymentReminderScheduler.enqueue(this)
    }
}
