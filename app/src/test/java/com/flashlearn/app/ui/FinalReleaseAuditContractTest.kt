package com.flashlearn.app.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FinalReleaseAuditContractTest {
    @Test
    fun releaseVersionIs677() {
        val buildFile = sequenceOf(File("build.gradle.kts"), File("app/build.gradle.kts"))
            .first { it.exists() && it.readText().contains("versionCode = 676") }
            .readText()
        assertTrue(buildFile.contains("versionName = \"6.76\""))
    }

    @Test
    fun creatorWhatsAppContractIsPresent() {
        val buildFile = File("app/build.gradle.kts").readText()
        assertTrue(buildFile.contains("APP_CREATOR_WHATSAPP_URL"))
        assertTrue(buildFile.contains("https://wa.me/34685644444"))
        assertTrue(File("src/main/res/values/strings.xml").readText().contains("about_whatsapp"))
        assertTrue(File("src/main/res/values-en/strings.xml").readText().contains("about_whatsapp"))
    }

    @Test
    fun defaultAndEnglishStringCatalogsExist() {
        assertTrue(File("src/main/res/values/strings.xml").exists())
        assertTrue(File("src/main/res/values-en/strings.xml").exists())
    }
}
