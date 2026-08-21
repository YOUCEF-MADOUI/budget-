package com.budgetplusplus.app

import com.budgetplusplus.app.navigation.AppDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AppDestinationTest {
    @Test
    fun `technical flow has stable distinct destinations`() {
        assertEquals("welcome", AppDestination.Welcome.route)
        assertNotEquals(AppDestination.Welcome.route, AppDestination.Home.route)
    }
}
