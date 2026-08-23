package com.budgetplusplus.core.model

enum class CropMode { ORIGINAL, SQUARE }
enum class FavoriteBehavior { IMMEDIATE, QUICK_CONFIRM, FULL_FORM }
enum class IconTheme { FOOD, HOME, TRANSPORT, HEALTH, LEISURE, WORK, SERVICES, OTHER }

data class MediaAsset(val id:String,val fileName:String,val thumbnailFileName:String,val width:Int,val height:Int,val sizeBytes:Long)
data class FavoriteInput(val id:String?=null,val name:String,val mediaId:String?=null,val iconKey:String="category",val colorKey:String="primary",val unitPriceMinor:Long,val accountId:String,val categoryId:String,val subcategoryId:String?=null,val description:String="",val behavior:FavoriteBehavior=FavoriteBehavior.QUICK_CONFIRM)

data class Favorite(
 val id:String,val name:String,val mediaId:String?=null,val iconKey:String="category",val colorKey:String="primary",val unitPriceMinor:Long,val currencyCode:String="DZD",val accountId:String,val accountName:String,val categoryId:String,val categoryName:String,val subcategoryId:String?=null,val subcategoryName:String?=null,val description:String="",val behavior:FavoriteBehavior=FavoriteBehavior.QUICK_CONFIRM,val isArchived:Boolean=false
)
data class FavoriteClickResult(val transactionId:String,val quantity:Int,val unitPriceMinor:Long,val totalMinor:Long)
data class BudgetIconDefinition(val key:String,val theme:IconTheme,val descriptionKey:String)
