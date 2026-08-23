package com.budgetplusplus.domain.security

object AppSecurityPolicy {
 fun validPin(pin:CharArray):Boolean=pin.size in 4..8&&pin.all(Char::isDigit)
 fun shouldLockAfterBackground(elapsedMillis:Long,delaySeconds:Long):Boolean=elapsedMillis>=0&&delaySeconds>=0&&elapsedMillis>=delaySeconds*1000
}
