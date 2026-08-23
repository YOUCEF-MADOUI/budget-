package com.budgetplusplus.core.designsystem.icons
import com.budgetplusplus.core.model.IconTheme
import org.junit.Assert.*
import org.junit.Test
class BudgetIconCatalogTest{@Test fun `catalog covers every theme with stable unique keys`(){assertEquals(BudgetIconCatalog.icons.size,BudgetIconCatalog.icons.map{it.key}.toSet().size);IconTheme.entries.forEach{theme->assertTrue(BudgetIconCatalog.icons.any{it.theme==theme})};assertTrue(BudgetIconCatalog.icons.all{it.descriptionKey.isNotBlank()})}}
