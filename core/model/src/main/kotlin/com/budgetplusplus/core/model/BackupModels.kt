package com.budgetplusplus.core.model

data class BackupPreview(val formatVersion:Int,val schemaVersion:Int,val createdAt:Long,val accounts:Int,val transactions:Int,val categories:Int,val budgets:Int)
enum class CsvDataset { ACCOUNTS, TRANSACTIONS, CATEGORIES, BUDGETS }
