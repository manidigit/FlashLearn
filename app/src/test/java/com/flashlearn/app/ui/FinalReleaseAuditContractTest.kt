package com.flashlearn.app.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FinalReleaseAuditContractTest {
    @Test
    fun releaseVersionIs679() {
        val buildFile = File("build.gradle.kts")
        assertTrue(buildFile.exists())
        val content = buildFile.readText()
        assertTrue(content.contains("versionCode = 679"))
        assertTrue(content.contains("versionName = \"6.79\""))
    }

    @Test
    fun creatorWhatsAppContractIsPresent() {
        val buildFile = File("build.gradle.kts")
        assertTrue(buildFile.exists())
        val values = File("src/main/res/values/strings.xml")
        val english = File("src/main/res/values-en/strings.xml")
        assertTrue(buildFile.readText().contains("APP_CREATOR_WHATSAPP_URL"))
        assertTrue(buildFile.readText().contains("https://wa.me/34685644444"))
        assertTrue(values.readText().contains("about_whatsapp"))
        assertTrue(english.readText().contains("about_whatsapp"))
    }

    @Test
    fun defaultAndEnglishStringCatalogsExist() {
        assertTrue(File("src/main/res/values/strings.xml").exists())
        assertTrue(File("src/main/res/values-en/strings.xml").exists())
    }
}
