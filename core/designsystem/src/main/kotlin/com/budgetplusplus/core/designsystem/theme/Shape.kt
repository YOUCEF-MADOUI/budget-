package com.budgetplusplus.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import com.budgetplusplus.core.designsystem.tokens.BudgetRadii

val BudgetPlusPlusShapes = Shapes(
    extraSmall = RoundedCornerShape(BudgetRadii.ExtraSmall),
    small = RoundedCornerShape(BudgetRadii.Small),
    medium = RoundedCornerShape(BudgetRadii.Medium),
    large = RoundedCornerShape(BudgetRadii.Large),
    extraLarge = RoundedCornerShape(BudgetRadii.ExtraLarge),
)
