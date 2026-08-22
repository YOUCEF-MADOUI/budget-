package com.budgetplusplus.domain.accounts

import com.budgetplusplus.core.model.Account
import com.budgetplusplus.core.model.AccountTreeNode
import java.util.ArrayDeque

object AccountHierarchy {
    fun canMove(accountId: String, newParentId: String?, parentById: Map<String, String?>): Boolean {
        if (newParentId == null) return true
        if (accountId == newParentId || newParentId !in parentById) return false
        val visited = mutableSetOf<String>()
        var cursor: String? = newParentId
        while (cursor != null && visited.add(cursor)) {
            if (cursor == accountId) return false
            cursor = parentById[cursor]
        }
        return cursor == null
    }

    fun descendantIds(accountId: String, accounts: List<Account>): Set<String> {
        val children = accounts.groupBy { it.parentAccountId }
        val result = linkedSetOf<String>()
        val queue = ArrayDeque<String>().apply { add(accountId) }
        while (queue.isNotEmpty()) {
            children[queue.removeFirst()].orEmpty().forEach { child -> if (result.add(child.id)) queue.add(child.id) }
        }
        result.remove(accountId)
        return result
    }

    fun breadcrumb(accountId: String, accounts: List<Account>): List<Account> {
        val byId = accounts.associateBy(Account::id)
        val reversed = mutableListOf<Account>()
        val visited = mutableSetOf<String>()
        var cursor = byId[accountId]
        while (cursor != null && visited.add(cursor.id)) {
            reversed += cursor
            cursor = cursor.parentAccountId?.let(byId::get)
        }
        return reversed.asReversed()
    }

    fun flatten(accounts: List<Account>, collapsedIds: Set<String>): List<AccountTreeNode> {
        val ordered = accounts.sortedWith(compareBy<Account> { it.displayOrder }.thenBy { it.name })
        val children = ordered.groupBy { it.parentAccountId }
        data class Pending(val value: Account, val depth: Int)
        val stack = ArrayDeque<Pending>()
        children[null].orEmpty().asReversed().forEach { stack.push(Pending(it, 0)) }
        val result = mutableListOf<AccountTreeNode>()
        val visited = mutableSetOf<String>()
        while (stack.isNotEmpty()) {
            val (account, depth) = stack.pop()
            if (!visited.add(account.id)) continue
            val descendants = children[account.id].orEmpty()
            result += AccountTreeNode(account, depth, descendants.isNotEmpty())
            if (account.id !in collapsedIds) descendants.asReversed().forEach { stack.push(Pending(it, depth + 1)) }
        }
        return result
    }
}
