package com.budgetplusplus.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.budgetplusplus.core.model.BudgetPeriodType
import com.budgetplusplus.core.model.BudgetScope

@Entity(
    tableName = "budgets",
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(entity = WorkspaceEntity::class, parentColumns = ["id"], childColumns = ["workspace_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["category_id"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index("workspace_id", "is_archived", "start_date", "deleted_at"), Index("category_id")],
)
data class BudgetEntity(
    val id: String,
    @ColumnInfo(name = "workspace_id") val workspaceId: String,
    val name: String,
    val scope: BudgetScope,
    @ColumnInfo(name = "category_id") val categoryId: String? = null,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,
    @ColumnInfo(name = "currency_code") val currencyCode: String,
    @ColumnInfo(name = "period_type") val periodType: BudgetPeriodType,
    @ColumnInfo(name = "start_date") val startDate: String,
    @ColumnInfo(name = "end_date") val endDate: String,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
)

@Entity(
    tableName = "budget_alert_thresholds",
    primaryKeys = ["budget_id", "percentage"],
    foreignKeys = [ForeignKey(entity = BudgetEntity::class, parentColumns = ["id"], childColumns = ["budget_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("budget_id")],
)
data class BudgetAlertThresholdEntity(
    @ColumnInfo(name = "budget_id") val budgetId: String,
    val percentage: Int,
    val enabled: Boolean = true,
)

@Entity(
    tableName = "budget_alert_events",
    primaryKeys = ["id"],
    foreignKeys = [ForeignKey(entity = BudgetEntity::class, parentColumns = ["id"], childColumns = ["budget_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["budget_id", "period_key", "percentage"], unique = true)],
)
data class BudgetAlertEventEntity(
    val id: String,
    @ColumnInfo(name = "budget_id") val budgetId: String,
    @ColumnInfo(name = "period_key") val periodKey: String,
    val percentage: Int,
    @ColumnInfo(name = "notified_at") val notifiedAt: Long,
)
