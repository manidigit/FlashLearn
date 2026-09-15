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
class DataExportRepositoryImpl @Inject constructor(private val db: RoomFlashLearnDatabase,@ApplicationContext private val context: Context):DataExportRepository{
 override suspend fun export(format:ExportFormat):File=when(format){ExportFormat.JSON->json();ExportFormat.CSV->csv();ExportFormat.XLSX->xlsx();ExportFormat.SQLITE->sqlite()}
 private suspend fun rows():List<Array<String?>>{val cs=db.conceptDao().getAll();val contents=db.contentDao().getAll();val by=cs.associateBy{it.id};return contents.map{c->arrayOf(c.conceptId.toString(),by[c.conceptId]?.entryType,c.languageCode,c.text,c.canonicalKey,c.notes,c.grammarNote,c.possibleCorrection,c.pronunciation,c.example,c.translationIndex.toString())}}
 private suspend fun csv():File{val f=File(context.cacheDir,"flashlearn-vocabulary.csv");f.bufferedWriter().use{w->w.appendLine("conceptId,entryType,languageCode,text,canonicalKey,notes,grammarNote,possibleCorrection,pronunciation,example,translationIndex");rows().forEach{r->w.appendLine(r.joinToString(","){escape(it)})}};return f}
 private fun escape(v:String?):String="\"${(v?:("" )).replace("\"","\"\"")}\""
 private suspend fun json():File{val f=File(context.cacheDir,"flashlearn-vocabulary.json");val a=rows();f.writeText(buildString{append("{\"format\":\"FlashLearn JSON\",\"version\":2,\"contents\":[");a.forEachIndexed{i,r->if(i>0)append(',');append('{');val names=listOf("conceptId","entryType","languageCode","text","canonicalKey","notes","grammarNote","possibleCorrection","pronunciation","example","translationIndex");names.forEachIndexed{j,n->if(j>0)append(',');append('"').append(n).append("\":").append(jsonString(r[j]))};append('}')};append("]}" )});return f}
 private fun jsonString(v:String?):String="\"${(v?:"").replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n")}\""
 private suspend fun xlsx():File{val f=File(context.cacheDir,"flashlearn-vocabulary.xlsx");ZipOutputStream(f.outputStream()).use{z->fun put(n:String,s:String){z.putNextEntry(ZipEntry(n));z.write(s.toByteArray(Charsets.UTF_8));z.closeEntry()};put("[Content_Types].xml","<?xml version=\"1.0\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/></Types>");put("_rels/.rels","<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>");put("xl/workbook.xml","<?xml version=\"1.0\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets><sheet name=\"Vocabulary\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");put("xl/_rels/workbook.xml.rels","<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/></Relationships>");val r=rows();val names=listOf("conceptId","entryType","languageCode","text","canonicalKey","notes","grammarNote","possibleCorrection","pronunciation","example","translationIndex");fun cell(s:String?):String="<c t=\"inlineStr\"><is><t>${xml(s)}</t></is></c>";val sheet=buildString{append("<?xml version=\"1.0\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");append("<row>");names.forEach{append(cell(it))};append("</row>");r.forEach{append("<row>");it.forEach{v->append(cell(v))};append("</row>")};append("</sheetData></worksheet>")};put("xl/worksheets/sheet1.xml",sheet)};return f}
 private fun xml(v:String?):String=(v?:"").replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&apos;")
 private suspend fun sqlite():File{db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use{};val source=context.getDatabasePath("flashlearn.db");val out=File(context.cacheDir,"flashlearn.db");source.copyTo(out,true);return out}
}
