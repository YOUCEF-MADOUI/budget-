package com.budgetplusplus.data.backup

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object AtomicFileReplacer {
 fun replace(original:File,staged:File,validate:(File)->Unit){
  val rollback=File(original.parentFile,"${original.name}.restore-rollback");rollback.delete()
  try{
   if(original.exists())move(original,rollback)
   move(staged,original)
   validate(original)
   rollback.delete()
  }catch(error:Throwable){
   original.delete()
   if(rollback.exists())move(rollback,original)
   staged.delete()
   throw error
  }
 }
 private fun move(from:File,to:File){runCatching{Files.move(from.toPath(),to.toPath(),StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING)}.getOrElse{Files.move(from.toPath(),to.toPath(),StandardCopyOption.REPLACE_EXISTING)}}
}
