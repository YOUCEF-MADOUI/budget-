package com.budgetplusplus.data.backup

import android.content.Context
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object MediaRestoreCoordinator{
 fun stagedDirectory(context:Context)=File(context.filesDir,"media-restore-staged")
 fun apply(context:Context){val staged=stagedDirectory(context);if(!staged.exists())return;val target=File(context.filesDir,"media").apply{mkdirs()};staged.listFiles().orEmpty().forEach{source->val destination=File(target,source.name);runCatching{Files.move(source.toPath(),destination.toPath(),StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING)}.getOrElse{Files.move(source.toPath(),destination.toPath(),StandardCopyOption.REPLACE_EXISTING)}};staged.deleteRecursively()}
}
