package com.budgetplusplus.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.budgetplusplus.core.model.RecurrenceFrequency
import com.budgetplusplus.core.model.RecurrenceOccurrenceStatus
import com.budgetplusplus.core.model.TransactionType

@Entity(
    tableName = "recurring_transactions",
    foreignKeys = [
        ForeignKey(entity=WorkspaceEntity::class,parentColumns=["id"],childColumns=["workspace_id"],onDelete=ForeignKey.CASCADE),
        ForeignKey(entity=AccountEntity::class,parentColumns=["id"],childColumns=["account_id"],onDelete=ForeignKey.RESTRICT),
        ForeignKey(entity=AccountEntity::class,parentColumns=["id"],childColumns=["destination_account_id"],onDelete=ForeignKey.RESTRICT),
        ForeignKey(entity=CategoryEntity::class,parentColumns=["id"],childColumns=["category_id"],onDelete=ForeignKey.SET_NULL),
        ForeignKey(entity=SubcategoryEntity::class,parentColumns=["id"],childColumns=["subcategory_id"],onDelete=ForeignKey.SET_NULL),
    ],
    indices=[Index("workspace_id","is_active","next_due_date","deleted_at"),Index("account_id"),Index("destination_account_id"),Index("category_id"),Index("subcategory_id")],
)
data class RecurringTransactionEntity(
    @PrimaryKey val id:String,
    @ColumnInfo(name="workspace_id") val workspaceId:String,
    val name:String,
    val type:TransactionType,
    @ColumnInfo(name="amount_minor") val amountMinor:Long,
    @ColumnInfo(name="currency_code") val currencyCode:String,
    @ColumnInfo(name="account_id") val accountId:String,
    @ColumnInfo(name="destination_account_id") val destinationAccountId:String?=null,
    @ColumnInfo(name="category_id") val categoryId:String?=null,
    @ColumnInfo(name="subcategory_id") val subcategoryId:String?=null,
    val description:String="",
    val frequency:RecurrenceFrequency,
    @ColumnInfo(name="anchor_month") val anchorMonth:Int,
    @ColumnInfo(name="anchor_day") val anchorDay:Int,
    @ColumnInfo(name="start_date") val startDate:String,
    @ColumnInfo(name="end_date") val endDate:String?=null,
    @ColumnInfo(name="next_due_date") val nextDueDate:String,
    @ColumnInfo(name="zone_id") val zoneId:String,
    @ColumnInfo(name="is_active") val isActive:Boolean=true,
    @ColumnInfo(name="created_at") val createdAt:Long,
    @ColumnInfo(name="updated_at") val updatedAt:Long,
    @ColumnInfo(name="deleted_at") val deletedAt:Long?=null,
)

@Entity(
    tableName="recurring_occurrences",
    foreignKeys=[ForeignKey(entity=RecurringTransactionEntity::class,parentColumns=["id"],childColumns=["recurring_transaction_id"],onDelete=ForeignKey.CASCADE)],
    indices=[Index(value=["recurring_transaction_id","due_date"],unique=true),Index("transaction_id")],
)
data class RecurringOccurrenceEntity(
    @PrimaryKey val id:String,
    @ColumnInfo(name="recurring_transaction_id") val recurringTransactionId:String,
    @ColumnInfo(name="due_date") val dueDate:String,
    val status:RecurrenceOccurrenceStatus,
    @ColumnInfo(name="transaction_id") val transactionId:String?=null,
    @ColumnInfo(name="error_code") val errorCode:String?=null,
    @ColumnInfo(name="processed_at") val processedAt:Long,
)
