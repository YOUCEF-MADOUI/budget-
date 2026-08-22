package com.budgetplusplus.domain.security

object PinRateLimiter {
 fun delayMillisAfterFailure(attempts:Int):Long=when(attempts){in Int.MIN_VALUE..2->0;3->5_000;4->30_000;5->120_000;else->300_000}
}
