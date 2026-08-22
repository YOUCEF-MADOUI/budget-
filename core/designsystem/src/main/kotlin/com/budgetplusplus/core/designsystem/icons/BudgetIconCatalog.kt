package com.budgetplusplus.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector
import com.budgetplusplus.core.model.BudgetIconDefinition
import com.budgetplusplus.core.model.IconTheme

object BudgetIconCatalog {
 val icons=listOf(
  BudgetIconDefinition("food",IconTheme.FOOD,"icon_food"),BudgetIconDefinition("drink",IconTheme.FOOD,"icon_drink"),BudgetIconDefinition("restaurant",IconTheme.FOOD,"icon_restaurant"),
  BudgetIconDefinition("home",IconTheme.HOME,"icon_home"),BudgetIconDefinition("construction",IconTheme.HOME,"icon_construction"),BudgetIconDefinition("plumbing",IconTheme.HOME,"icon_plumbing"),
  BudgetIconDefinition("car",IconTheme.TRANSPORT,"icon_car"),BudgetIconDefinition("bus",IconTheme.TRANSPORT,"icon_bus"),BudgetIconDefinition("fuel",IconTheme.TRANSPORT,"icon_fuel"),
  BudgetIconDefinition("health",IconTheme.HEALTH,"icon_health"),BudgetIconDefinition("pharmacy",IconTheme.HEALTH,"icon_pharmacy"),
  BudgetIconDefinition("sport",IconTheme.LEISURE,"icon_sport"),BudgetIconDefinition("games",IconTheme.LEISURE,"icon_games"),
  BudgetIconDefinition("work",IconTheme.WORK,"icon_work"),BudgetIconDefinition("tools",IconTheme.WORK,"icon_tools"),
  BudgetIconDefinition("service",IconTheme.SERVICES,"icon_service"),BudgetIconDefinition("invoice",IconTheme.SERVICES,"icon_invoice"),BudgetIconDefinition("phone",IconTheme.SERVICES,"icon_phone"),
  BudgetIconDefinition("other",IconTheme.OTHER,"icon_other")
 )
 fun vector(key:String):ImageVector=when(key){"home","construction","plumbing"->BudgetIcons.Home;"car","bus","fuel","work"->BudgetIcons.Account;"service","invoice","phone"->BudgetIcons.Search;"drink","restaurant","sport","games","tools"->BudgetIcons.Add;else->BudgetIcons.Category}
}
