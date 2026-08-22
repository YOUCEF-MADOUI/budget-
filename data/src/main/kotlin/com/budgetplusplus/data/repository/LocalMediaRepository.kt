package com.budgetplusplus.data.repository

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import com.budgetplusplus.core.model.*
import com.budgetplusplus.database.dao.MediaDao
import com.budgetplusplus.database.entity.MediaAssetEntity
import com.budgetplusplus.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalMediaRepository @Inject constructor(@ApplicationContext private val context:Context,private val dao:MediaDao):MediaRepository{
 private val directory get()=File(context.filesDir,"media").apply{mkdirs()}
 override suspend fun importMedia(sourceUri:String,cropMode:CropMode):MediaAsset=withContext(Dispatchers.IO){val source=decode(Uri.parse(sourceUri));val cropped=if(cropMode==CropMode.SQUARE)centerSquare(source)else source;val resized=scaleDown(cropped,1600);val thumb=scaleDown(centerSquare(resized),256);val id=UUID.randomUUID().toString();val image=File(directory,"$id.webp");val thumbnail=File(directory,"$id-thumb.webp");write(resized,image,82);write(thumb,thumbnail,75);val width=resized.width;val height=resized.height;if(source!==resized&&source!==cropped)source.recycle();if(cropped!==resized)cropped.recycle();if(thumb!==resized)thumb.recycle();resized.recycle();val entity=MediaAssetEntity(id,image.name,thumbnail.name,width,height,image.length(),System.currentTimeMillis());dao.insert(entity);MediaAsset(entity.id,entity.fileName,entity.thumbnailFileName,entity.width,entity.height,entity.sizeBytes)}
 override suspend fun attachToAccount(accountId:String,mediaId:String?){if(mediaId!=null)requireNotNull(dao.get(mediaId));dao.attachAccount(accountId,mediaId,System.currentTimeMillis())}
 override suspend fun attachToCategory(categoryId:String,mediaId:String?){if(mediaId!=null)requireNotNull(dao.get(mediaId));dao.attachCategory(categoryId,mediaId,System.currentTimeMillis())}
 override suspend fun attachToTransaction(transactionId:String,mediaId:String?){if(mediaId!=null)requireNotNull(dao.get(mediaId));dao.attachTransaction(transactionId,mediaId,System.currentTimeMillis())}
 override suspend fun mediaFilePath(mediaId:String,thumbnail:Boolean):String?=withContext(Dispatchers.IO){dao.get(mediaId)?.let{File(directory,if(thumbnail)it.thumbnailFileName else it.fileName).takeIf(File::exists)?.absolutePath}}
 private fun decode(uri:Uri):Bitmap{if(Build.VERSION.SDK_INT>=28){val source=ImageDecoder.createSource(context.contentResolver,uri);return ImageDecoder.decodeBitmap(source){decoder,_,_->decoder.allocator=ImageDecoder.ALLOCATOR_SOFTWARE}};return context.contentResolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it)}?:error("decode")}
 private fun centerSquare(value:Bitmap):Bitmap{if(value.width==value.height)return value;val side=minOf(value.width,value.height);return Bitmap.createBitmap(value,(value.width-side)/2,(value.height-side)/2,side,side)}
 private fun scaleDown(value:Bitmap,max:Int):Bitmap{val largest=maxOf(value.width,value.height);if(largest<=max)return value;val ratio=max.toFloat()/largest;return Bitmap.createScaledBitmap(value,(value.width*ratio).toInt().coerceAtLeast(1),(value.height*ratio).toInt().coerceAtLeast(1),true)}
 private fun write(value:Bitmap,file:File,quality:Int){val temp=File(file.parentFile,"${file.name}.tmp");temp.outputStream().use{value.compress(if(Build.VERSION.SDK_INT>=30)Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP,quality,it)};check(temp.renameTo(file))}
}
