package com.budgetplusplus.domain.validation

import com.budgetplusplus.core.model.TransactionSearchFilter
import java.time.LocalDate

object SearchFilterValidator{
 fun isValid(value:TransactionSearchFilter):Boolean{
  val minimum=value.minimumMinor;val maximum=value.maximumMinor
  if(minimum!=null&&maximum!=null&&minimum>maximum)return false
  val from=value.fromDate?.let{runCatching{LocalDate.parse(it)}.getOrNull()?:return false};val to=value.toDate?.let{runCatching{LocalDate.parse(it)}.getOrNull()?:return false}
  return from==null||to==null||from<=to
 }
}
