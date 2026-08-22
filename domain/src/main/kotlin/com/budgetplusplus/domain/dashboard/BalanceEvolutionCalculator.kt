package com.budgetplusplus.domain.dashboard

import com.budgetplusplus.core.model.BalancePoint
import com.budgetplusplus.core.model.DailyCashFlow

object BalanceEvolutionCalculator {
    fun calculate(fromInclusive: Long, openingBalanceMinor: Long, cashFlows: List<DailyCashFlow>): List<BalancePoint> {
        val points = mutableListOf(BalancePoint(fromInclusive, openingBalanceMinor, 0, 0))
        var balance = openingBalanceMinor
        cashFlows.sortedBy(DailyCashFlow::epochMillis).forEach { flow ->
            balance = Math.addExact(balance, Math.subtractExact(flow.incomeMinor, flow.expenseMinor))
            points += BalancePoint(flow.epochMillis, balance, flow.incomeMinor, flow.expenseMinor)
        }
        return points
    }
}
