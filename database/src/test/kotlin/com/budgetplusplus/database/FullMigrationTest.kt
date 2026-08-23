package com.budgetplusplus.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.room.migration.Migration
import androidx.test.core.app.ApplicationProvider
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[35])
class FullMigrationTest {
 private val context:Context=ApplicationProvider.getApplicationContext<Context>();private val name="migration-chain.db"
 @Before fun clean(){context.deleteDatabase(name)}
 @After fun after(){context.deleteDatabase(name)}
 private val migrations:List<Migration> = listOf(MIGRATION_1_2,MIGRATION_2_3,MIGRATION_3_4,MIGRATION_4_5,MIGRATION_5_6,MIGRATION_6_7,MIGRATION_7_8,MIGRATION_8_9,MIGRATION_9_10,MIGRATION_10_11)
 @Test fun `migrates a populated schema from version one through eleven`(){
  createLegacyDatabase()
  val migrated=openCurrentDatabase();assertEquals(11,migrated.openHelper.writableDatabase.version);assertTrue(table(migrated.openHelper.writableDatabase,"subcategories"));assertTrue(table(migrated.openHelper.writableDatabase,"budgets"));assertTrue(table(migrated.openHelper.writableDatabase,"recurring_transactions"));assertTrue(table(migrated.openHelper.writableDatabase,"finance_transactions_fts"));assertTrue(table(migrated.openHelper.writableDatabase,"future_payments"));assertTrue(table(migrated.openHelper.writableDatabase,"due_payments"));assertTrue(table(migrated.openHelper.writableDatabase,"favorites"));assertTrue(table(migrated.openHelper.writableDatabase,"media_assets"));assertLegacyData(migrated.openHelper.writableDatabase);migrated.close()
 }
 @Test fun `every historical schema version has a non destructive path to eleven`(){
  for(startVersion in 1..10){
   context.deleteDatabase(name);createLegacyDatabase()
   if(startVersion>1){
    val helper=FrameworkSQLiteOpenHelperFactory().create(SupportSQLiteOpenHelper.Configuration.builder(context).name(name).callback(object:SupportSQLiteOpenHelper.Callback(1){override fun onCreate(db:SupportSQLiteDatabase)=Unit;override fun onUpgrade(db:SupportSQLiteDatabase,oldVersion:Int,newVersion:Int)=Unit}).build())
    val db=helper.writableDatabase
    migrations.take(startVersion-1).forEach{migration->migration.migrate(db);db.version=migration.endVersion}
    helper.close()
   }
   val migrated=openCurrentDatabase();assertEquals("Failed from schema $startVersion",11,migrated.openHelper.writableDatabase.version);assertLegacyData(migrated.openHelper.writableDatabase);migrated.close()
  }
 }
 private fun createLegacyDatabase(){val config=SupportSQLiteOpenHelper.Configuration.builder(context).name(name).callback(object:SupportSQLiteOpenHelper.Callback(1){override fun onCreate(db:SupportSQLiteDatabase){createV1(db);db.execSQL("INSERT INTO workspaces VALUES ('w','Legacy','PERSONAL','DZD',1,1,1,NULL)");db.execSQL("INSERT INTO accounts VALUES ('a','w','Cash','CASH','DZD',1000,'primary','account',0,0,0,1,1,NULL)")}override fun onUpgrade(db:SupportSQLiteDatabase,oldVersion:Int,newVersion:Int)=Unit}).build();FrameworkSQLiteOpenHelperFactory().create(config).also{it.writableDatabase;it.close()}}
 private fun openCurrentDatabase()=Room.databaseBuilder(context,BudgetPlusDatabase::class.java,name).allowMainThreadQueries().addMigrations(*migrations.toTypedArray()).build()
 private fun assertLegacyData(db:SupportSQLiteDatabase){db.query("SELECT name,initial_balance_minor FROM accounts WHERE id='a'").use{assertTrue(it.moveToFirst());assertEquals("Cash",it.getString(0));assertEquals(1000L,it.getLong(1))}}
 private fun table(db:SupportSQLiteDatabase,name:String)=db.query("SELECT 1 FROM sqlite_master WHERE name=?",arrayOf(name)).use{it.moveToFirst()}
 private fun createV1(db:SupportSQLiteDatabase){
  db.execSQL("CREATE TABLE workspaces(id TEXT NOT NULL PRIMARY KEY,name TEXT NOT NULL,kind TEXT NOT NULL,currency_code TEXT NOT NULL,is_active INTEGER NOT NULL,created_at INTEGER NOT NULL,updated_at INTEGER NOT NULL,deleted_at INTEGER)")
  db.execSQL("CREATE TABLE accounts(id TEXT NOT NULL PRIMARY KEY,workspace_id TEXT NOT NULL,name TEXT NOT NULL,type TEXT NOT NULL,currency_code TEXT NOT NULL,initial_balance_minor INTEGER NOT NULL,color_key TEXT NOT NULL,icon_key TEXT NOT NULL,display_order INTEGER NOT NULL,is_hidden INTEGER NOT NULL,is_archived INTEGER NOT NULL,created_at INTEGER NOT NULL,updated_at INTEGER NOT NULL,deleted_at INTEGER,FOREIGN KEY(workspace_id) REFERENCES workspaces(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
  db.execSQL("CREATE INDEX index_accounts_workspace_id_is_archived_deleted_at ON accounts(workspace_id,is_archived,deleted_at)");db.execSQL("CREATE INDEX index_accounts_name ON accounts(name)")
  db.execSQL("CREATE TABLE categories(id TEXT NOT NULL PRIMARY KEY,workspace_id TEXT NOT NULL,kind TEXT NOT NULL,name_key TEXT,custom_name TEXT,icon_key TEXT NOT NULL,color_key TEXT NOT NULL,is_system INTEGER NOT NULL,is_archived INTEGER NOT NULL,display_order INTEGER NOT NULL,created_at INTEGER NOT NULL,updated_at INTEGER NOT NULL,deleted_at INTEGER,FOREIGN KEY(workspace_id) REFERENCES workspaces(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
  db.execSQL("CREATE INDEX index_categories_workspace_id_kind_is_archived ON categories(workspace_id,kind,is_archived)");db.execSQL("CREATE UNIQUE INDEX index_categories_workspace_id_custom_name_kind ON categories(workspace_id,custom_name,kind)")
  db.execSQL("CREATE TABLE finance_transactions(id TEXT NOT NULL PRIMARY KEY,workspace_id TEXT NOT NULL,type TEXT NOT NULL,amount_minor INTEGER NOT NULL,currency_code TEXT NOT NULL,account_id TEXT NOT NULL,destination_account_id TEXT,category_id TEXT,occurred_at INTEGER NOT NULL,local_date TEXT NOT NULL,zone_id TEXT NOT NULL,description TEXT NOT NULL,note TEXT NOT NULL,merchant TEXT NOT NULL,payment_method TEXT,created_at INTEGER NOT NULL,updated_at INTEGER NOT NULL,deleted_at INTEGER,FOREIGN KEY(workspace_id) REFERENCES workspaces(id) ON UPDATE NO ACTION ON DELETE CASCADE,FOREIGN KEY(account_id) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE RESTRICT,FOREIGN KEY(destination_account_id) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE RESTRICT,FOREIGN KEY(category_id) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE SET NULL)")
  db.execSQL("CREATE INDEX index_finance_transactions_workspace_id_local_date_deleted_at ON finance_transactions(workspace_id,local_date,deleted_at)");db.execSQL("CREATE INDEX index_finance_transactions_account_id_occurred_at_deleted_at ON finance_transactions(account_id,occurred_at,deleted_at)");db.execSQL("CREATE INDEX index_finance_transactions_destination_account_id_occurred_at_deleted_at ON finance_transactions(destination_account_id,occurred_at,deleted_at)");db.execSQL("CREATE INDEX index_finance_transactions_category_id_local_date_type_deleted_at ON finance_transactions(category_id,local_date,type,deleted_at)")
 }
}
