package com.budgetplusplus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.budgetplusplus.core.model.CategoryKind
import com.budgetplusplus.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE deleted_at IS NULL AND (:kind IS NULL OR kind = :kind) ORDER BY is_archived, display_order, custom_name")
    fun observeAll(kind: CategoryKind?): Flow<List<CategoryEntity>>
    @Insert suspend fun insert(entity: CategoryEntity)
    @Query("UPDATE categories SET is_archived = :archived, updated_at = :now WHERE id = :id AND is_system = 0")
    suspend fun setArchived(id: String, archived: Boolean, now: Long)
    @Query("SELECT kind FROM categories WHERE id = :id AND deleted_at IS NULL") suspend fun kind(id: String): CategoryKind?
}
