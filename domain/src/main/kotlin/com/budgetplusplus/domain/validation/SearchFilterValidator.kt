package com.budgetplusplus.domain.validation

import com.budgetplusplus.core.model.TransactionSearchFilter
import java.time.LocalDate

object SearchFilterValidator{
 fun isValid(value:TransactionSearchFilter):Boolean{
  if(value.minimumMinor!=null&&value.maximumMinor!=null&&value.minimumMinor>value.maximumMinor)return false
  val from=value.fromDate?.let{runCatching{LocalDate.parse(it)}.getOrNull()?:return false};val to=value.toDate?.let{runCatching{LocalDate.parse(it)}.getOrNull()?:return false}
  return from==null||to==null||from<=to
 }
}
