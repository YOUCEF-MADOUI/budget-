package com.budgetplusplus.database

import org.junit.Assert.assertEquals
import org.junit.Test

class MigrationsTest {
    @Test fun `dashboard index migration advances schema one to two`() {
        assertEquals(1, MIGRATION_1_2.startVersion)
        assertEquals(2, MIGRATION_1_2.endVersion)
    }
}
