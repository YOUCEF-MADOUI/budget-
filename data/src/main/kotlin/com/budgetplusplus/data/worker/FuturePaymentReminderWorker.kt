package com.budgetplusplus.data.worker

import android.Manifest
import android.app.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.work.*
import com.budgetplusplus.data.R
import com.budgetplusplus.domain.repository.FuturePaymentRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@EntryPoint @InstallIn(SingletonComponent::class) interface FuturePaymentWorkerEntryPoint{fun futurePaymentRepository():FuturePaymentRepository}
class FuturePaymentReminderWorker(context:Context,params:WorkerParameters):CoroutineWorker(context,params){override suspend fun doWork():Result=runCatching{if(Build.VERSION.SDK_INT>=33&&applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return Result.success();val repository=EntryPointAccessors.fromApplication(applicationContext,FuturePaymentWorkerEntryPoint::class.java).futurePaymentRepository();val reminders=repository.claimReminders(LocalDate.now().toString());if(reminders.isNotEmpty()){val manager=applicationContext.getSystemService(NotificationManager::class.java);val channel="budgetplusplus_due_reminders";manager.createNotificationChannel(NotificationChannel(channel,applicationContext.getString(R.string.due_notification_channel),NotificationManager.IMPORTANCE_DEFAULT));reminders.forEach{value->val notification=Notification.Builder(applicationContext,channel).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(applicationContext.getString(R.string.due_notification_title)).setContentText(applicationContext.getString(R.string.due_notification_message,value.name,value.dueDate)).setAutoCancel(true).build();manager.notify(value.dueId.hashCode(),notification)}};Result.success()}.getOrElse{Result.retry()}}
object FuturePaymentReminderScheduler{private const val UNIQUE="budgetplusplus-future-payment-reminders";fun enqueue(context:Context){val request=PeriodicWorkRequestBuilder<FuturePaymentReminderWorker>(24,TimeUnit.HOURS,2,TimeUnit.HOURS).build();WorkManager.getInstance(context).enqueueUniquePeriodicWork(UNIQUE,ExistingPeriodicWorkPolicy.UPDATE,request)}}
