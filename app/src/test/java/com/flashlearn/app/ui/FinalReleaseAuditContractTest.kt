package com.flashlearn.app.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FinalReleaseAuditContractTest {
    @Test
    fun releaseVersionIs658() {
        val buildFile = File("app/build.gradle.kts").readText()
        assertTrue(buildFile.contains("versionCode = 658"))
        assertTrue(buildFile.contains("versionName = \"6.58\""))
    }

    @Test
    fun defaultAndEnglishStringCatalogsExist() {
        assertTrue(File("app/src/main/res/values/strings.xml").exists())
        assertTrue(File("app/src/main/res/values-en/strings.xml").exists())
    }
}
