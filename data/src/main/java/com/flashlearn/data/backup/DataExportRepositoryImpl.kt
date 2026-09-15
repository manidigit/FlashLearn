package com.flashlearn.data.backup

import android.content.Context
import com.flashlearn.database.RoomFlashLearnDatabase
import com.flashlearn.domain.repository.DataExportRepository
import com.flashlearn.domain.repository.ExportFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExportRepositoryImpl @Inject constructor(
    private val db: RoomFlashLearnDatabase,
    @ApplicationContext private val context: Context
) : DataExportRepository {

    override suspend fun export(format: ExportFormat): File = when (format) {
        ExportFormat.JSON -> json()
        ExportFormat.CSV -> csv()
        ExportFormat.XLSX -> xlsx()
        ExportFormat.SQLITE -> sqlite()
    }

    private suspend fun rows(): List<Array<String?>> {
        val concepts = db.conceptDao().getAll()
        val contents = db.contentDao().getAll()
        val byId = concepts.associateBy { it.id }
        return contents.map { content ->
            val concept = byId[content.conceptId]
            arrayOf(
                content.conceptId.toString(),
                concept?.entryType,
                content.languageCode,
                content.text,
                content.canonicalKey,
                content.notes,
                content.grammarNote,
                content.possibleCorrection,
                content.pronunciation,
                content.example,
                content.translationIndex.toString()
            )
        }
    }

    private suspend fun csv(): File {
        val file = File(context.cacheDir, "flashlearn-vocabulary.csv")
        file.bufferedWriter().use { writer ->
            writer.appendLine("conceptId,entryType,languageCode,text,canonicalKey,notes,grammarNote,possibleCorrection,pronunciation,example,translationIndex")
            rows().forEach { row ->
                writer.appendLine(row.joinToString(",") { value -> escapeCsv(value) })
            }
        }
        return file
    }

    private fun escapeCsv(value: String?): String {
        return "\"${(value ?: "").replace("\"", "\"\"")}\""
    }

    private suspend fun json(): File {
        val file = File(context.cacheDir, "flashlearn-vocabulary.json")
        val values = rows()
        val names = listOf(
            "conceptId", "entryType", "languageCode", "text", "canonicalKey",
            "notes", "grammarNote", "possibleCorrection", "pronunciation",
            "example", "translationIndex"
        )
        file.writeText(buildString {
            append("{\"format\":\"FlashLearn JSON\",\"version\":2,\"contents\":[")
            values.forEachIndexed { index, row ->
                if (index > 0) append(',')
                append('{')
                names.forEachIndexed { fieldIndex, name ->
                    if (fieldIndex > 0) append(',')
                    append('"').append(name).append("\":")
                    append(jsonString(row[fieldIndex]))
                }
                append('}')
            }
            append("]}")
        })
        return file
    }

    private fun jsonString(value: String?): String {
        return "\"${(value ?: "")
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")}\""
    }

    private suspend fun xlsx(): File {
        val file = File(context.cacheDir, "flashlearn-vocabulary.xlsx")
        val names = listOf(
            "conceptId", "entryType", "languageCode", "text", "canonicalKey",
            "notes", "grammarNote", "possibleCorrection", "pronunciation",
            "example", "translationIndex"
        )
        ZipOutputStream(file.outputStream()).use { zip ->
            putZipEntry(
                zip,
                "[Content_Types].xml",
                "<?xml version=\"1.0\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/></Types>"
            )
            putZipEntry(
                zip,
                "_rels/.rels",
                "<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>"
            )
            putZipEntry(
                zip,
                "xl/workbook.xml",
                "<?xml version=\"1.0\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets><sheet name=\"Vocabulary\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>"
            )
            putZipEntry(
                zip,
                "xl/_rels/workbook.xml.rels",
                "<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/></Relationships>"
            )

            val sheet = buildString {
                append("<?xml version=\"1.0\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>")
                append("<row>")
                names.forEach { name -> append(xlsxCell(name)) }
                append("</row>")
                rows().forEach { row ->
                    append("<row>")
                    row.forEach { value -> append(xlsxCell(value)) }
                    append("</row>")
                }
                append("</sheetData></worksheet>")
            }
            putZipEntry(zip, "xl/worksheets/sheet1.xml", sheet)
        }
        return file
    }

    private fun putZipEntry(zip: ZipOutputStream, name: String, value: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(value.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun xlsxCell(value: String?): String {
        return "<c t=\"inlineStr\"><is><t>${xml(value)}</t></is></c>"
    }

    private fun xml(value: String?): String {
        return (value ?: "")
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private suspend fun sqlite(): File {
        db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { }
        val source = context.getDatabasePath("flashlearn.db")
        val output = File(context.cacheDir, "flashlearn.db")
        source.copyTo(output, overwrite = true)
        return output
    }
}
