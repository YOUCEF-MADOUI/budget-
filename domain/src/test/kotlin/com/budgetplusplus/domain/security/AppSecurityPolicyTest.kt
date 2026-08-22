package com.budgetplusplus.domain.security

import org.junit.Assert.*
import org.junit.Test

class AppSecurityPolicyTest {
 @Test fun `pin accepts only four to eight digits`(){assertTrue(AppSecurityPolicy.validPin("1234".toCharArray()));assertTrue(AppSecurityPolicy.validPin("12345678".toCharArray()));assertFalse(AppSecurityPolicy.validPin("123".toCharArray()));assertFalse(AppSecurityPolicy.validPin("12a4".toCharArray()))}
 @Test fun `progressive delays increase after repeated failures`(){assertEquals(0L,PinRateLimiter.delayMillisAfterFailure(2));assertEquals(5_000L,PinRateLimiter.delayMillisAfterFailure(3));assertEquals(30_000L,PinRateLimiter.delayMillisAfterFailure(4));assertEquals(120_000L,PinRateLimiter.delayMillisAfterFailure(5));assertEquals(300_000L,PinRateLimiter.delayMillisAfterFailure(10))}
 @Test fun `background lock honors configured delay`(){assertFalse(AppSecurityPolicy.shouldLockAfterBackground(29_999,30));assertTrue(AppSecurityPolicy.shouldLockAfterBackground(30_000,30));assertTrue(AppSecurityPolicy.shouldLockAfterBackground(0,0))}
}
