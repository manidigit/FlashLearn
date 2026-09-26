package com.flashlearn.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FinalReleaseAuditContractTest {
    @Test fun releaseVersionIs650() {
        val buildFile = File("build.gradle.kts").readText()
        assertTrue(buildFile.contains("versionCode = 650"))
        assertTrue(buildFile.contains("versionName = \"6.50\""))
    }

    @Test fun defaultAndEnglishStringCatalogsExist() {
        assertTrue(File("src/main/res/values/strings.xml").exists())
        assertTrue(File("src/main/res/values-en/strings.xml").exists())
    }

}
