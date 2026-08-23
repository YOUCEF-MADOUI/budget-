package com.budgetplusplus.domain.favorites
import org.junit.Assert.*
import org.junit.Test
class FavoriteRulesTest{@Test fun `quantity total is exact and overflow is rejected`(){assertEquals(750L,FavoriteRules.total(250,3));assertTrue(runCatching{FavoriteRules.total(Long.MAX_VALUE,2)}.isFailure)};@Test fun `click batching uses bounded forward window`(){assertTrue(FavoriteRules.canMerge(1000,2499));assertFalse(FavoriteRules.canMerge(1000,2501));assertFalse(FavoriteRules.canMerge(1000,999))}}
