package com.budgetplusplus.core.designsystem.util

import java.util.Locale
import kotlin.system.measureTimeMillis
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyFormatterPerformanceTest {
 @Test fun `formats one hundred thousand cached DZD values within safety budget`(){val elapsed=measureTimeMillis{repeat(100_000){MoneyFormatter.format(it.toLong(),"DZD",Locale.FRANCE)}};assertTrue("Formatting took ${elapsed}ms",elapsed<5_000)}
}
