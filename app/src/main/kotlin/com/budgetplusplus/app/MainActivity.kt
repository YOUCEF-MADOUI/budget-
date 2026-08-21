package com.budgetplusplus.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.budgetplusplus.core.designsystem.theme.BudgetPlusPlusTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BudgetPlusPlusTheme {
                BudgetPlusPlusApp()
            }
        }
    }
}
