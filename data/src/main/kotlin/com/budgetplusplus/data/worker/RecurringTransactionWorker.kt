package com.budgetplusplus.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.budgetplusplus.domain.repository.RecurringRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit

@EntryPoint @InstallIn(SingletonComponent::class)
interface RecurringWorkerEntryPoint { fun recurringRepository():RecurringRepository }

class RecurringTransactionWorker(context:Context,parameters:WorkerParameters):CoroutineWorker(context,parameters){
 override suspend fun doWork():Result=runCatching{
  val repository=EntryPointAccessors.fromApplication(applicationContext,RecurringWorkerEntryPoint::class.java).recurringRepository()
  val next=repository.processDue();RecurringWorkScheduler.schedule(applicationContext,next);Result.success()
 }.getOrElse{Result.retry()}
}

object RecurringWorkScheduler{
 private const val UNIQUE="budgetplusplus-recurring-catchup"
 fun enqueueCatchUp(context:Context)=schedule(context,System.currentTimeMillis())
 fun schedule(context:Context,nextEpochMillis:Long?){
  val target=nextEpochMillis?:System.currentTimeMillis()+TimeUnit.DAYS.toMillis(1)
  val request=OneTimeWorkRequestBuilder<RecurringTransactionWorker>().setInitialDelay((target-System.currentTimeMillis()).coerceAtLeast(0),TimeUnit.MILLISECONDS).build()
  WorkManager.getInstance(context).enqueueUniqueWork(UNIQUE,ExistingWorkPolicy.REPLACE,request)
 }
}
