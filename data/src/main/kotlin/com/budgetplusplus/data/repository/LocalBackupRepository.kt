package com.budgetplusplus.data.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.budgetplusplus.core.model.BackupPreview
import com.budgetplusplus.core.model.CsvDataset
import com.budgetplusplus.data.backup.AtomicFileReplacer
import com.budgetplusplus.database.BudgetPlusDatabase
import com.budgetplusplus.domain.backup.BackupCrypto
import com.budgetplusplus.domain.backup.DecryptedBackup
import com.budgetplusplus.domain.backup.InvalidBackupException
import com.budgetplusplus.domain.repository.BackupRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.*
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalBackupRepository @Inject constructor(@ApplicationContext private val context:Context,private val database:BudgetPlusDatabase):BackupRepository{
 override suspend fun createBackup(destinationUri:String,password:CharArray)=withContext(Dispatchers.IO){
  require(password.size>=8);database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use{};val file=context.getDatabasePath(BudgetPlusDatabase.NAME);val bytes=file.readBytes();try{context.contentResolver.openOutputStream(Uri.parse(destinationUri),"wt")?.use{BackupCrypto.encrypt(bytes,BudgetPlusDatabase.SCHEMA_VERSION,password,it)}?:throw IOException("output")}finally{bytes.fill(0);password.fill('\u0000')}
 }
 override suspend fun previewBackup(sourceUri:String,password:CharArray):BackupPreview=withContext(Dispatchers.IO){val decoded=decrypt(sourceUri,password);val temp=temp(decoded.databaseBytes);try{inspect(temp,decoded)}finally{temp.delete();decoded.databaseBytes.fill(0);password.fill('\u0000')}}
 override suspend fun restoreBackup(sourceUri:String,password:CharArray)=withContext(Dispatchers.IO){val decoded=decrypt(sourceUri,password);val target=context.getDatabasePath(BudgetPlusDatabase.NAME);val staged=File(target.parentFile,"${target.name}.restore-staged");try{staged.outputStream().use{it.write(decoded.databaseBytes);it.flush();(it as FileOutputStream).fd.sync()};validate(staged);database.close();File(target.path+"-wal").delete();File(target.path+"-shm").delete();AtomicFileReplacer.replace(target,staged,::validate)}finally{decoded.databaseBytes.fill(0);staged.delete();password.fill('\u0000')}}
 override suspend fun exportCsv(destinationUri:String,dataset:CsvDataset)=withContext(Dispatchers.IO){val spec=csvSpec(dataset);context.contentResolver.openOutputStream(Uri.parse(destinationUri),"wt")?.use{stream->OutputStreamWriter(stream,Charsets.UTF_8).buffered().use{writer->writer.appendLine(spec.headers.joinToString(","));database.openHelper.readableDatabase.query(spec.sql).use{cursor->while(cursor.moveToNext()){writer.appendLine((0 until cursor.columnCount).joinToString(","){index->escape(if(cursor.isNull(index))"" else cursor.getString(index))})}}}}?:throw IOException("output")}
 private fun decrypt(uri:String,password:CharArray):DecryptedBackup=try{context.contentResolver.openInputStream(Uri.parse(uri))?.use{BackupCrypto.decrypt(it,password)}?:throw IOException("input")}finally{password.fill('\u0000')}
 private fun temp(bytes:ByteArray)=File.createTempFile("budgetplusplus-restore-",".db",context.cacheDir).also{it.writeBytes(bytes)}
 private fun validate(file:File){if(file.length()<100)throw InvalidBackupException("sqlite");val db=SQLiteDatabase.openDatabase(file.path,null,SQLiteDatabase.OPEN_READONLY);db.use{it.rawQuery("PRAGMA integrity_check(1)",null).use{c->if(!c.moveToFirst()||c.getString(0)!="ok")throw InvalidBackupException("integrity")};val version=it.version;if(version !in 1..BudgetPlusDatabase.SCHEMA_VERSION)throw InvalidBackupException("schema")}}
 private fun inspect(file:File,value:DecryptedBackup):BackupPreview{validate(file);SQLiteDatabase.openDatabase(file.path,null,SQLiteDatabase.OPEN_READONLY).use{db->return BackupPreview(value.formatVersion,db.version,value.createdAt,count(db,"accounts"),count(db,"finance_transactions"),count(db,"categories"),count(db,"budgets"))}}
 private fun count(db:SQLiteDatabase,table:String):Int{db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?",arrayOf(table)).use{if(!it.moveToFirst()||it.getInt(0)==0)return 0};db.rawQuery("SELECT COUNT(*) FROM $table",null).use{return if(it.moveToFirst())it.getInt(0)else 0}}
 private data class CsvSpec(val headers:List<String>,val sql:String)
 private fun csvSpec(value:CsvDataset)=when(value){
  CsvDataset.ACCOUNTS->CsvSpec(listOf("id","parent_account_id","name","type","currency_code","initial_balance_minor","icon_key","color_key","description","display_order","archived"),"SELECT id,parent_account_id,name,type,currency_code,initial_balance_minor,icon_key,color_key,description,display_order,is_archived FROM accounts WHERE deleted_at IS NULL ORDER BY display_order,name")
  CsvDataset.TRANSACTIONS->CsvSpec(listOf("id","type","amount_minor","currency_code","account_id","destination_account_id","category_id","subcategory_id","local_date","description"),"SELECT id,type,amount_minor,currency_code,account_id,destination_account_id,category_id,subcategory_id,local_date,description FROM finance_transactions WHERE deleted_at IS NULL ORDER BY occurred_at")
  CsvDataset.CATEGORIES->CsvSpec(listOf("id","kind","name_key","custom_name","icon_key","color_key","archived"),"SELECT id,kind,name_key,custom_name,icon_key,color_key,is_archived FROM categories WHERE deleted_at IS NULL ORDER BY kind,display_order")
  CsvDataset.BUDGETS->CsvSpec(listOf("id","name","scope","category_id","amount_minor","currency_code","period_type","start_date","end_date","archived"),"SELECT id,name,scope,category_id,amount_minor,currency_code,period_type,start_date,end_date,is_archived FROM budgets WHERE deleted_at IS NULL ORDER BY start_date")}
 private fun escape(value:String):String="\"${value.replace("\"","\"\"")}\""
}
