package com.budgetplusplus.core.model

data class AppSecuritySettings(
 val pinEnabled:Boolean=false,
 val biometricEnabled:Boolean=false,
 val lockDelaySeconds:Long=0,
 val protectScreen:Boolean=true,
 val failedAttempts:Int=0,
 val blockedUntilEpochMillis:Long=0,
)
sealed interface PinVerificationResult{data object Success:PinVerificationResult;data class Invalid(val attempts:Int,val retryAtEpochMillis:Long):PinVerificationResult;data class Blocked(val retryAtEpochMillis:Long):PinVerificationResult}
