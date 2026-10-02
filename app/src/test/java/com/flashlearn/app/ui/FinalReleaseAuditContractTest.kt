package com.flashlearn.app.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FinalReleaseAuditContractTest {
    @Test
    fun releaseVersionIs673() {
        val buildFile = sequenceOf(File("build.gradle.kts"), File("app/build.gradle.kts"))
            .first { it.exists() && it.readText().contains("versionCode = 673") }
            .readText()
        assertTrue(buildFile.contains("versionName = \"6.73\""))
    }

    @Test
    fun defaultAndEnglishStringCatalogsExist() {
        assertTrue(File("src/main/res/values/strings.xml").exists())
        assertTrue(File("src/main/res/values-en/strings.xml").exists())
    }
}
