package com.budgetplusplus.database

import org.junit.Assert.assertEquals
import org.junit.Test

class MigrationsTest {
    @Test fun `dashboard index migration advances schema one to two`() {
        assertEquals(1, MIGRATION_1_2.startVersion)
        assertEquals(2, MIGRATION_1_2.endVersion)
        assertEquals(2, MIGRATION_2_3.startVersion)
        assertEquals(3, MIGRATION_2_3.endVersion)
        assertEquals(3, MIGRATION_3_4.startVersion)
        assertEquals(4, MIGRATION_3_4.endVersion)
    }
}
