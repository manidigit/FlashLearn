package com.flashlearn.app.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Regression for the Category screen crash (v6.74): a string resource with a numeric
 * specifier (%d) was formatted with a String argument (for example toFaDigits(...)),
 * which throws IllegalFormatConversionException the first time the screen is composed.
 */
class StringFormatArgumentContractTest {
    private val numericSpecifier = Regex("%(\\d+\\$)?[dfxXoeEgG]")
    private val textArgument = Regex("toFaDigits|\\.toString\\(|\"")
    private val resourceEntry = Regex("<string name=\"([^\"]+)\"[^>]*>(.*?)</string>", RegexOption.DOT_MATCHES_ALL)
    private val formattedCall = Regex("(?:stringResource|getString)\\(R\\.string\\.([a-z_0-9]+)\\s*,([^\\n]*)")

    private fun mainDir(): File =
        sequenceOf(File("src/main"), File("app/src/main")).first { it.exists() }

    @Test
    fun numericFormatStringsNeverReceiveTextArguments() {
        val main = mainDir()
        val numericResources = main.resolve("res").walkTopDown()
            .filter { it.isFile && it.extension == "xml" && it.parentFile.name.startsWith("values") }
            .flatMap { file -> resourceEntry.findAll(file.readText()).map { it.groupValues[1] to it.groupValues[2] } }
            .filter { (_, value) -> numericSpecifier.containsMatchIn(value) }
            .map { it.first }
            .toSet()
        assertTrue("No numeric format resources found; test is not scanning correctly", numericResources.isNotEmpty())

        val violations = main.resolve("java").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                formattedCall.findAll(file.readText())
                    .filter { it.groupValues[1] in numericResources && textArgument.containsMatchIn(it.groupValues[2]) }
                    .map { "${file.name}: ${it.groupValues[1]}" }
            }
            .toList()
        assertTrue("Numeric format resources given text arguments: $violations", violations.isEmpty())
    }
}
