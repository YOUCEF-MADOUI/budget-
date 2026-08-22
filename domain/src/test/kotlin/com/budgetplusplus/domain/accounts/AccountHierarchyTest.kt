package com.budgetplusplus.domain.accounts

import com.budgetplusplus.core.model.*
import org.junit.Assert.*
import org.junit.Test

class AccountHierarchyTest {
 private fun account(id:String,parent:String?=null,order:Int=0)=Account(id,id,AccountType.CASH,"DZD",0,0,displayOrder=order,parentAccountId=parent)
 @Test fun `supports a thousand levels without recursion overflow`(){val values=(0 until 1000).map{account("a$it",if(it==0)null else "a${it-1}")};val flat=AccountHierarchy.flatten(values,emptySet());assertEquals(1000,flat.size);assertEquals(999,flat.last().depth);assertEquals(999,AccountHierarchy.descendantIds("a0",values).size);assertEquals(1000,AccountHierarchy.breadcrumb("a999",values).size)}
 @Test fun `rejects self and descendant moves`(){val values=listOf(account("root"),account("child","root"),account("leaf","child"));val parents=values.associate{it.id to it.parentAccountId};assertFalse(AccountHierarchy.canMove("root","leaf",parents));assertFalse(AccountHierarchy.canMove("child","child",parents));assertTrue(AccountHierarchy.canMove("leaf","root",parents));assertTrue(AccountHierarchy.canMove("child",null,parents))}
 @Test fun `collapsed branch keeps its descendants out of flattened window`(){val values=listOf(account("root"),account("child","root"),account("leaf","child"));assertEquals(listOf("root","child"),AccountHierarchy.flatten(values,setOf("child")).map{it.account.id})}
}
