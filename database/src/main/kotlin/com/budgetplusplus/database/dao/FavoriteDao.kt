package com.budgetplusplus.database.dao

import androidx.room.*
import com.budgetplusplus.database.entity.*
import kotlinx.coroutines.flow.Flow

data class FavoriteRow(val id:String,val name:String,val mediaId:String?,val iconKey:String,val colorKey:String,val unitPriceMinor:Long,val currencyCode:String,val accountId:String,val accountName:String,val categoryId:String,val categoryName:String?,val subcategoryId:String?,val subcategoryName:String?,val description:String,val behavior:String,val isArchived:Boolean)
@Dao interface FavoriteDao{
 @Query("""SELECT f.id,f.name,f.media_id AS mediaId,f.icon_key AS iconKey,f.color_key AS colorKey,f.unit_price_minor AS unitPriceMinor,f.currency_code AS currencyCode,f.account_id AS accountId,a.name AS accountName,f.category_id AS categoryId,COALESCE(c.custom_name,c.name_key) AS categoryName,f.subcategory_id AS subcategoryId,s.custom_name AS subcategoryName,f.description,f.behavior,f.is_archived AS isArchived FROM favorites f JOIN accounts a ON a.id=f.account_id JOIN categories c ON c.id=f.category_id LEFT JOIN subcategories s ON s.id=f.subcategory_id WHERE f.deleted_at IS NULL ORDER BY f.is_archived,f.display_order,f.name""") fun observeAll():Flow<List<FavoriteRow>>
 @Query("SELECT * FROM favorites WHERE id=:id AND deleted_at IS NULL") suspend fun get(id:String):FavoriteEntity?
 @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsert(value:FavoriteEntity)
 @Query("UPDATE favorites SET is_archived=:archived,updated_at=:now WHERE id=:id") suspend fun setArchived(id:String,archived:Boolean,now:Long)
 @Query("SELECT * FROM favorite_click_batches WHERE favorite_id=:favoriteId") suspend fun batch(favoriteId:String):FavoriteClickBatchEntity?
 @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsertBatch(value:FavoriteClickBatchEntity)
 @Query("DELETE FROM favorite_click_batches WHERE transaction_id=:transactionId") suspend fun clearBatch(transactionId:String)
}
@Dao interface MediaDao{
 @Insert suspend fun insert(value:MediaAssetEntity)
 @Query("SELECT * FROM media_assets WHERE id=:id AND deleted_at IS NULL") suspend fun get(id:String):MediaAssetEntity?
 @Query("UPDATE accounts SET media_id=:mediaId,updated_at=:now WHERE id=:id") suspend fun attachAccount(id:String,mediaId:String?,now:Long)
 @Query("UPDATE categories SET media_id=:mediaId,updated_at=:now WHERE id=:id") suspend fun attachCategory(id:String,mediaId:String?,now:Long)
 @Query("UPDATE finance_transactions SET media_id=:mediaId,updated_at=:now WHERE id=:id") suspend fun attachTransaction(id:String,mediaId:String?,now:Long)
}
