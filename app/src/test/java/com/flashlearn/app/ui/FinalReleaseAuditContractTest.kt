package com.flashlearn.app.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FinalReleaseAuditContractTest {
    @Test
    fun releaseVersionIs667() {
        val buildFile = sequenceOf(File("build.gradle.kts"), File("app/build.gradle.kts"))
            .first { it.exists() && it.readText().contains("versionCode = 667") }
            .readText()
        assertTrue(buildFile.contains("versionCode = 666"))
        assertTrue(buildFile.contains("versionName = \"6.67\""))
    }

    @Test
    fun defaultAndEnglishStringCatalogsExist() {
        assertTrue(File("src/main/res/values/strings.xml").exists())
        assertTrue(File("src/main/res/values-en/strings.xml").exists())
    }
}
