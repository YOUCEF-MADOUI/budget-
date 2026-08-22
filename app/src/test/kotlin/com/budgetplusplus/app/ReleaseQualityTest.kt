package com.budgetplusplus.app

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.*
import org.junit.Test

class ReleaseQualityTest {
 private val root:File by lazy{generateSequence(File(System.getProperty("user.dir")).absoluteFile){it.parentFile}.first{File(it,"settings.gradle.kts").exists()}}
 @Test fun `debug APK stays below size budget`(){val apk=File(root,"app/build/outputs/apk/debug/app-debug.apk");assertTrue("assembleDebug must run before quality tests",apk.exists());assertTrue("Debug APK exceeds 50 MiB: ${apk.length()}",apk.length()<50L*1024*1024)}
 @Test fun `all localized modules keep French English Arabic parity`(){val modules=root.walkTopDown().filter{it.isFile&&it.path.endsWith("src/main/res/values/strings.xml")}.toList();assertTrue(modules.isNotEmpty());modules.forEach{base->val res=base.parentFile.parentFile;val expected=names(base);listOf("values-fr","values-en","values-ar").forEach{folder->val translated=File(res,"$folder/strings.xml");assertTrue("Missing $folder for ${base.relativeTo(root)}",translated.exists());assertEquals("Resource mismatch in ${translated.relativeTo(root)}",expected,names(translated))}}}
 @Test fun `application explicitly supports RTL`(){val manifest=File(root,"app/src/main/AndroidManifest.xml").readText();assertTrue(manifest.contains("android:supportsRtl=\"true\""))}
 private fun names(file:File):Set<String>{val document=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);val nodes=document.getElementsByTagName("string");return(0 until nodes.length).map{nodes.item(it).attributes.getNamedItem("name").nodeValue}.toSet()}
}
