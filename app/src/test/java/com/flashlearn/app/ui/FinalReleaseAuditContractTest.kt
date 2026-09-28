package com.flashlearn.app.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FinalReleaseAuditContractTest {
    @Test
    fun releaseVersionIs660() {
        val buildFile = File("build.gradle.kts").readText()
        assertTrue(buildFile.contains("versionCode = 660"))
        assertTrue(buildFile.contains("versionName = \"6.60\""))
    }

    @Test
    fun defaultAndEnglishStringCatalogsExist() {
        assertTrue(File("src/main/res/values/strings.xml").exists())
        assertTrue(File("src/main/res/values-en/strings.xml").exists())
    }
}
