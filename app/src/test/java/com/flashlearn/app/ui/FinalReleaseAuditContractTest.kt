package com.flashlearn.app.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FinalReleaseAuditContractTest {
    @Test
    fun releaseVersionIs670() {
        val buildFile = sequenceOf(File("build.gradle.kts"), File("app/build.gradle.kts"))
            .first { it.exists() && it.readText().contains("versionCode = 670") }
            .readText()
        assertTrue(buildFile.contains("versionName = \"6.70\""))
    }

    @Test
    fun defaultAndEnglishStringCatalogsExist() {
        assertTrue(File("src/main/res/values/strings.xml").exists())
        assertTrue(File("src/main/res/values-en/strings.xml").exists())
    }
}
