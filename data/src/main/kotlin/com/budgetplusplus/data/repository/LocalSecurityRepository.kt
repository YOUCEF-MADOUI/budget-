package com.budgetplusplus.data.repository

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.WorkManager
import com.budgetplusplus.core.model.*
import com.budgetplusplus.data.security.KeystorePinHasher
import com.budgetplusplus.database.BudgetPlusDatabase
import com.budgetplusplus.domain.repository.SecurityRepository
import com.budgetplusplus.domain.security.PinRateLimiter
import com.budgetplusplus.domain.security.AppSecurityPolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

private val Context.securityDataStore by preferencesDataStore("security_preferences")
class LocalSecurityRepository @Inject constructor(@ApplicationContext private val context:Context,private val database:BudgetPlusDatabase):SecurityRepository{
 private val hasher=KeystorePinHasher()
 private object Keys{val hash=stringPreferencesKey("pin_hash");val salt=stringPreferencesKey("pin_salt");val biometric=booleanPreferencesKey("biometric");val delay=longPreferencesKey("lock_delay");val protect=booleanPreferencesKey("protect_screen");val attempts=intPreferencesKey("failed_attempts");val blocked=longPreferencesKey("blocked_until")}
 override fun observeSettings():Flow<AppSecuritySettings>=context.securityDataStore.data.map{p->AppSecuritySettings(p[Keys.hash]!=null,p[Keys.biometric]?:false,p[Keys.delay]?:0,p[Keys.protect]?:true,p[Keys.attempts]?:0,p[Keys.blocked]?:0)}
 override suspend fun setPin(pin:CharArray){if(!AppSecurityPolicy.validPin(pin)){pin.fill('\u0000');throw IllegalArgumentException()};val salt=ByteArray(32).also{SecureRandom().nextBytes(it)};val hash=hasher.hash(pin,salt);context.securityDataStore.edit{it[Keys.salt]=Base64.encodeToString(salt,Base64.NO_WRAP);it[Keys.hash]=Base64.encodeToString(hash,Base64.NO_WRAP);it[Keys.attempts]=0;it[Keys.blocked]=0};salt.fill(0);hash.fill(0)}
 override suspend fun verifyPin(pin:CharArray,nowEpochMillis:Long):PinVerificationResult{val p=context.securityDataStore.data.first();val blocked=p[Keys.blocked]?:0;if(nowEpochMillis<blocked){pin.fill('\u0000');return PinVerificationResult.Blocked(blocked)};val saltText=p[Keys.salt]?:run{pin.fill('\u0000');return PinVerificationResult.Invalid(0,0)};val expectedText=p[Keys.hash]?:run{pin.fill('\u0000');return PinVerificationResult.Invalid(0,0)};val salt=Base64.decode(saltText,Base64.NO_WRAP);val expected=Base64.decode(expectedText,Base64.NO_WRAP);val actual=hasher.hash(pin,salt);val valid=MessageDigest.isEqual(expected,actual);salt.fill(0);expected.fill(0);actual.fill(0);if(valid){context.securityDataStore.edit{it[Keys.attempts]=0;it[Keys.blocked]=0};return PinVerificationResult.Success};val attempts=(p[Keys.attempts]?:0)+1;val retry=nowEpochMillis+PinRateLimiter.delayMillisAfterFailure(attempts);context.securityDataStore.edit{it[Keys.attempts]=attempts;it[Keys.blocked]=retry};return PinVerificationResult.Invalid(attempts,retry)}
 override suspend fun setBiometricEnabled(enabled:Boolean){context.securityDataStore.edit{it[Keys.biometric]=enabled}}
 override suspend fun recordBiometricSuccess(){context.securityDataStore.edit{it[Keys.attempts]=0;it[Keys.blocked]=0}}
 override suspend fun setLockDelay(seconds:Long){require(seconds in setOf(0L,30L,60L,120L,300L));context.securityDataStore.edit{it[Keys.delay]=seconds}}
 override suspend fun setProtectScreen(enabled:Boolean){context.securityDataStore.edit{it[Keys.protect]=enabled}}
 override suspend fun disablePin(){context.securityDataStore.edit{it.remove(Keys.hash);it.remove(Keys.salt);it[Keys.biometric]=false;it[Keys.attempts]=0;it[Keys.blocked]=0};hasher.deleteKey()}
 override suspend fun eraseAllDataAfterForgottenPin(){WorkManager.getInstance(context).cancelAllWork();database.close();context.deleteDatabase(BudgetPlusDatabase.NAME);context.securityDataStore.edit{it.clear()};hasher.deleteKey()}
}
