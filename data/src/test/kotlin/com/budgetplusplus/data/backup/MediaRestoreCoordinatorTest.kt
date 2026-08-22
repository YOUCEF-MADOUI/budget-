package com.budgetplusplus.data.backup
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class) @Config(sdk=[35]) class MediaRestoreCoordinatorTest{@Test fun `staged private media is recovered atomically on startup`(){val context=ApplicationProvider.getApplicationContext<Context>();val staged=MediaRestoreCoordinator.stagedDirectory(context).apply{deleteRecursively();mkdirs()};staged.resolve("photo.webp").writeBytes(byteArrayOf(1,2,3));MediaRestoreCoordinator.apply(context);assertArrayEquals(byteArrayOf(1,2,3),context.filesDir.resolve("media/photo.webp").readBytes());assertFalse(staged.exists())}}
