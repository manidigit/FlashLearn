package com.flashlearn.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FinalReleaseAuditContractTest {
    @Test fun releaseVersionIs649() {
        val buildFile = File("build.gradle.kts").readText()
        assertTrue(buildFile.contains("versionCode = 649"))
        assertTrue(buildFile.contains("versionName = \"6.49\""))
    }

    @Test fun defaultAndEnglishStringCatalogsExist() {
        assertTrue(File("src/main/res/values/strings.xml").exists())
        assertTrue(File("src/main/res/values-en/strings.xml").exists())
    }

    @Test fun finalStaticContractReportExists() {
        assertTrue(File("FINAL_STATIC_CONTRACT_RESULT_v6.49.txt").exists())
        val report = File("FINAL_STATIC_CONTRACT_RESULT_v6.49.txt").readText()
        assertTrue(report.lineSequence().count { it.contains("PASS") } >= 2)
    }
}
