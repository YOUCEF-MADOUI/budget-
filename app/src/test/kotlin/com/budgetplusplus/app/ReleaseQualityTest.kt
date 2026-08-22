package com.budgetplusplus.app

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseQualityTest {
    private val root: File by lazy {
        generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").exists() }
    }

    @Test
    fun `debug APK stays below size budget`() {
        val apk = File(root, "app/build/outputs/apk/debug/app-debug.apk")
        assertTrue("assembleDebug must run before quality tests", apk.exists())
        assertTrue("Debug APK exceeds 50 MiB: ${apk.length()}", apk.length() < 50L * 1024 * 1024)
    }

    @Test
    fun `release identity is version one`() {
        val configuration = File(
            root,
            "build-logic/src/main/kotlin/com/budgetplusplus/buildlogic/AndroidConfiguration.kt",
        ).readText()
        assertTrue(configuration.contains("versionCode = 10_000"))
        assertTrue(configuration.contains("versionName = \"1.0.0\""))
    }

    @Test
    fun `official launcher icon is complete on every supported API`() {
        val manifest = File(root, "app/src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("android:icon=\"@mipmap/ic_launcher\""))
        assertTrue(manifest.contains("android:roundIcon=\"@mipmap/ic_launcher_round\""))

        val expectedSizes = mapOf(
            "mdpi" to 48,
            "hdpi" to 72,
            "xhdpi" to 96,
            "xxhdpi" to 144,
            "xxxhdpi" to 192,
        )
        expectedSizes.forEach { (density, expectedSize) ->
            listOf("ic_launcher.png", "ic_launcher_round.png").forEach { name ->
                val file = File(root, "app/src/main/res/mipmap-$density/$name")
                assertTrue("Missing ${file.relativeTo(root)}", file.exists())
                val image: BufferedImage = ImageIO.read(file)
                assertEquals(expectedSize, image.width)
                assertEquals(expectedSize, image.height)
            }
        }

        val adaptive = File(root, "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml").readText()
        assertTrue(adaptive.contains("@drawable/ic_launcher_foreground"))
        assertTrue(adaptive.contains("@color/ic_launcher_background"))
        val themed = File(root, "app/src/main/res/mipmap-anydpi-v33/ic_launcher.xml").readText()
        assertTrue(themed.contains("@drawable/ic_launcher_monochrome"))
    }

    @Test
    fun `all localized modules keep French English Arabic parity`() {
        val modules = root.walkTopDown()
            .filter { it.isFile && it.path.endsWith("src/main/res/values/strings.xml") }
            .toList()
        assertTrue(modules.isNotEmpty())
        modules.forEach { base ->
            val res = base.parentFile.parentFile
            val expected = names(base)
            listOf("values-fr", "values-en", "values-ar").forEach { folder ->
                val translated = File(res, "$folder/strings.xml")
                assertTrue("Missing $folder for ${base.relativeTo(root)}", translated.exists())
                assertEquals(
                    "Resource mismatch in ${translated.relativeTo(root)}",
                    expected,
                    names(translated),
                )
            }
        }
    }

    @Test
    fun `application supports RTL and never enables destructive Room fallback`() {
        val manifest = File(root, "app/src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("android:supportsRtl=\"true\""))
        val productionSources = sequenceOf("app", "data", "database")
            .flatMap { File(root, it).walkTopDown() }
            .filter { it.isFile && it.extension in setOf("kt", "java") }
            .joinToString("\n") { it.readText() }
        assertFalse(productionSources.contains("fallbackToDestructiveMigration"))
    }

    private fun names(file: File): Set<String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val nodes = document.getElementsByTagName("string")
        return (0 until nodes.length)
            .map { nodes.item(it).attributes.getNamedItem("name").nodeValue }
            .toSet()
    }
}
