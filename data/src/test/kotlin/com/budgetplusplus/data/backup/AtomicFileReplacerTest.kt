package com.budgetplusplus.data.backup

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Test

class AtomicFileReplacerTest {
 @Test fun `commits staged file after validation`(){val dir=Files.createTempDirectory("restore").toFile();val original=dir.resolve("db").apply{writeText("old")};val staged=dir.resolve("stage").apply{writeText("new")};AtomicFileReplacer.replace(original,staged){check(it.readText()=="new")};assertEquals("new",original.readText());dir.deleteRecursively()}
 @Test fun `rolls back original when validation is interrupted`(){val dir=Files.createTempDirectory("restore").toFile();val original=dir.resolve("db").apply{writeText("old")};val staged=dir.resolve("stage").apply{writeText("broken")};runCatching{AtomicFileReplacer.replace(original,staged){error("interrupted")}};assertEquals("old",original.readText());dir.deleteRecursively()}
}
