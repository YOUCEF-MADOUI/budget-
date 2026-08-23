package com.budgetplusplus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.budgetplusplus.core.model.CategoryKind
import com.budgetplusplus.database.entity.CategoryEntity
import com.budgetplusplus.database.entity.SubcategoryEntity
import kotlinx.coroutines.flow.Flow

data class CategoryDetails(
    val id: String, val kind: String, val nameKey: String?, val customName: String?,
    val iconKey: String, val colorKey: String, val mediaId:String?, val isSystem: Boolean, val isArchived: Boolean,
    val usageCount: Int,
)
data class SubcategoryDetails(
    val id: String, val categoryId: String, val nameKey: String?, val customName: String?,
    val isSystem: Boolean, val isArchived: Boolean, val usageCount: Int,
)

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE deleted_at IS NULL AND (:kind IS NULL OR kind = :kind) ORDER BY is_archived, display_order, custom_name")
    fun observeAll(kind: CategoryKind?): Flow<List<CategoryEntity>>

    @Query("""SELECT c.id, c.kind, c.name_key AS nameKey, c.custom_name AS customName,
        c.icon_key AS iconKey, c.color_key AS colorKey, c.media_id AS mediaId, c.is_system AS isSystem, c.is_archived AS isArchived,
        (SELECT COUNT(*) FROM finance_transactions t WHERE t.category_id = c.id AND t.deleted_at IS NULL) AS usageCount
        FROM categories c WHERE c.deleted_at IS NULL ORDER BY c.is_archived, c.display_order, c.custom_name""")
    fun observeDetails(): Flow<List<CategoryDetails>>

    @Query("""SELECT s.id, s.category_id AS categoryId, s.name_key AS nameKey, s.custom_name AS customName,
        s.is_system AS isSystem, s.is_archived AS isArchived,
        (SELECT COUNT(*) FROM finance_transactions t WHERE t.subcategory_id = s.id AND t.deleted_at IS NULL) AS usageCount
        FROM subcategories s WHERE s.deleted_at IS NULL ORDER BY s.is_archived, s.display_order, s.custom_name""")
    fun observeSubcategories(): Flow<List<SubcategoryDetails>>

    @Insert suspend fun insert(entity: CategoryEntity)
    @Insert suspend fun insertSubcategory(entity: SubcategoryEntity)
    @Query("UPDATE categories SET custom_name = :name, icon_key = :iconKey, color_key = :colorKey, updated_at = :now WHERE id = :id")
    suspend fun update(id: String, name: String, iconKey: String, colorKey: String, now: Long)
    @Query("UPDATE subcategories SET custom_name = :name, updated_at = :now WHERE id = :id AND is_system = 0")
    suspend fun updateSubcategory(id: String, name: String, now: Long)
    @Query("UPDATE categories SET is_archived = :archived, updated_at = :now WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean, now: Long)
    @Query("UPDATE subcategories SET is_archived = :archived, updated_at = :now WHERE id = :id AND is_system = 0")
    suspend fun setSubcategoryArchived(id: String, archived: Boolean, now: Long)
    @Query("UPDATE categories SET deleted_at = :now, updated_at = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long)
    @Query("UPDATE finance_transactions SET category_id = :replacementId, subcategory_id = NULL, updated_at = :now WHERE category_id = :sourceId AND deleted_at IS NULL")
    suspend fun reassignCategory(sourceId: String, replacementId: String, now: Long)
    @Query("UPDATE finance_transactions SET subcategory_id = :replacementId, updated_at = :now WHERE subcategory_id = :sourceId AND deleted_at IS NULL")
    suspend fun reassignSubcategory(sourceId: String, replacementId: String?, now: Long)
    @Query("SELECT kind FROM categories WHERE id = :id AND deleted_at IS NULL") suspend fun kind(id: String): CategoryKind?
    @Query("SELECT COUNT(*) FROM finance_transactions WHERE category_id = :id AND deleted_at IS NULL") suspend fun usageCount(id: String): Int
    @Query("SELECT COUNT(*) FROM finance_transactions WHERE subcategory_id = :id AND deleted_at IS NULL") suspend fun subcategoryUsageCount(id: String): Int
    @Query("SELECT category_id FROM subcategories WHERE id = :id AND deleted_at IS NULL") suspend fun subcategoryParent(id: String): String?
    @Query("SELECT is_system FROM categories WHERE id = :id AND deleted_at IS NULL") suspend fun isSystem(id: String): Boolean?
    @Query("SELECT is_system FROM subcategories WHERE id = :id AND deleted_at IS NULL") suspend fun isSubcategorySystem(id: String): Boolean?
}
