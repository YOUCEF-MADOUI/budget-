package com.budgetplusplus.domain.favorites

object FavoriteRules{fun total(unitPriceMinor:Long,quantity:Int):Long{require(unitPriceMinor>0&&quantity>0);return Math.multiplyExact(unitPriceMinor,quantity.toLong())};fun canMerge(lastClick:Long,now:Long,windowMillis:Long=1500)=now>=lastClick&&now-lastClick<=windowMillis}
