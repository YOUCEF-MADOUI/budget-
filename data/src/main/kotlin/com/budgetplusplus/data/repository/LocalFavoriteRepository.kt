package com.budgetplusplus.data.repository

import androidx.room.withTransaction
import com.budgetplusplus.core.model.*
import com.budgetplusplus.database.BudgetPlusDatabase
import com.budgetplusplus.database.dao.*
import com.budgetplusplus.database.entity.*
import com.budgetplusplus.domain.repository.FavoriteRepository
import com.budgetplusplus.domain.favorites.FavoriteRules
import java.time.*
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.*

class LocalFavoriteRepository @Inject constructor(private val db:BudgetPlusDatabase,private val dao:FavoriteDao,private val transactions:TransactionDao,private val accounts:AccountDao,private val categories:CategoryDao,private val media:MediaDao):FavoriteRepository{
 override fun observeFavorites():Flow<List<Favorite>> =dao.observeAll().map{rows->rows.map{Favorite(it.id,it.name,it.mediaId,it.iconKey,it.colorKey,it.unitPriceMinor,it.currencyCode,it.accountId,it.accountName,it.categoryId,it.categoryName?:"",it.subcategoryId,it.subcategoryName,it.description,FavoriteBehavior.valueOf(it.behavior),it.isArchived)}}
 override suspend fun save(input:FavoriteInput):String=db.withTransaction{require(input.name.isNotBlank()&&input.unitPriceMinor>0&&accounts.exists(input.accountId)&&categories.kind(input.categoryId)==CategoryKind.EXPENSE);val sub=input.subcategoryId;require(sub==null||categories.subcategoryParent(sub)==input.categoryId);val mediaId=input.mediaId;if(mediaId!=null)requireNotNull(media.get(mediaId));val id=input.id?:UUID.randomUUID().toString();val old=input.id?.let{dao.get(it)};val now=System.currentTimeMillis();dao.upsert(FavoriteEntity(id,BudgetPlusDatabase.DEFAULT_WORKSPACE_ID,input.name.trim(),input.mediaId,input.iconKey,input.colorKey,input.unitPriceMinor,"DZD",input.accountId,input.categoryId,sub,input.description.trim(),input.behavior,old?.displayOrder?:0,old?.isArchived?:false,old?.createdAt?:now,now));id}
 override suspend fun setArchived(id:String,archived:Boolean){dao.setArchived(id,archived,System.currentTimeMillis())}
 override suspend fun recordImmediateClick(favoriteId:String,nowEpochMillis:Long):FavoriteClickResult=db.withTransaction{val favorite=requireNotNull(dao.get(favoriteId));require(!favorite.isArchived);val batch=dao.batch(favoriteId);val previous=batch?.takeIf{FavoriteRules.canMerge(it.lastClickedAt,nowEpochMillis)}?.let{transactions.getEntity(it.transactionId)};if(batch!=null&&previous!=null&&previous.deletedAt==null){val quantity=Math.addExact(batch.quantity,1);val total=FavoriteRules.total(favorite.unitPriceMinor,quantity);require(transactions.updateFavoriteOperation(previous.id,total,favorite.unitPriceMinor,quantity,favorite.id,nowEpochMillis)==1);dao.upsertBatch(batch.copy(lastClickedAt=nowEpochMillis,quantity=quantity));FavoriteClickResult(previous.id,quantity,favorite.unitPriceMinor,total)}else createInternal(favorite,UUID.randomUUID().toString(),1,favorite.unitPriceMinor,LocalDate.now().toString(),nowEpochMillis,true)}
 override suspend fun createOperation(favoriteId:String,requestId:String,quantity:Int,unitPriceMinor:Long,date:String):FavoriteClickResult=db.withTransaction{val transactionId="favorite:$requestId";transactions.getEntity(transactionId)?.let{return@withTransaction FavoriteClickResult(it.id,it.quantity,it.unitPriceMinor?:it.amountMinor,it.amountMinor)};requireNotNull(dao.get(favoriteId)).let{require(!it.isArchived);createInternal(it,requestId,quantity,unitPriceMinor,date,System.currentTimeMillis(),false)}}
 override suspend fun undo(transactionId:String)=db.withTransaction{transactions.softDelete(transactionId,System.currentTimeMillis());dao.clearBatch(transactionId)}
 private suspend fun createInternal(favorite:FavoriteEntity,request:String,quantity:Int,unit:Long,dateText:String,now:Long,batch:Boolean):FavoriteClickResult{require(quantity>0&&unit>0);val total=FavoriteRules.total(unit,quantity);val date=LocalDate.parse(dateText);val zone=ZoneId.systemDefault();val id=if(batch)"favorite-batch:$request" else "favorite:$request";transactions.insert(FinanceTransactionEntity(id,favorite.workspaceId,TransactionType.EXPENSE,total,favorite.currencyCode,favorite.accountId,null,favorite.categoryId,favorite.subcategoryId,date.atTime(12,0).atZone(zone).toInstant().toEpochMilli(),date.toString(),zone.id,favorite.description.ifBlank{favorite.name},createdAt=now,updatedAt=now,mediaId=favorite.mediaId,favoriteId=favorite.id,unitPriceMinor=unit,quantity=quantity));if(batch)dao.upsertBatch(FavoriteClickBatchEntity(favorite.id,id,now,quantity));return FavoriteClickResult(id,quantity,unit,total)}
}
