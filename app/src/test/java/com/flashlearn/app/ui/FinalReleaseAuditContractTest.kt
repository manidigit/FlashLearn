package com.flashlearn.app.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FinalReleaseAuditContractTest {
    @Test
    fun releaseVersionIs668() {
        val buildFile = File("app/build.gradle.kts").readText()
        assertTrue(buildFile.contains("versionCode = 668"))
        assertTrue(buildFile.contains("versionName = \"6.68\""))
        val workflow = File(".github/workflows/android-ci.yml").readText()
        assertTrue(workflow.contains("FL_PREVIOUS_VERSION_CODE: \"667\""))
        assertTrue(workflow.contains("FL_PREVIOUS_VERSION_NAME: \"6.67\""))
    }
    @Test
    fun defaultAndEnglishStringCatalogsExist() {
        assertTrue(File("src/main/res/values/strings.xml").exists())
        assertTrue(File("src/main/res/values-en/strings.xml").exists())
    }
}
