package com.flashlearn.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE review_sessions ADD COLUMN reviewType TEXT NOT NULL DEFAULT 'DAILY'")
    }
}
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_categories_name` ON `categories` (`name`)")
    }
}
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `parser_metadata` (`conceptId` TEXT NOT NULL, `breakdownJson` TEXT NOT NULL, `relationshipsJson` TEXT NOT NULL, `variantsJson` TEXT NOT NULL, `confidence` REAL NOT NULL, PRIMARY KEY(`conceptId`))")
    }
}
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `achievements` (`achievementId` TEXT NOT NULL, `unlocked` INTEGER NOT NULL, PRIMARY KEY(`achievementId`))")
    }
}
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE `contents_new` (`id` TEXT NOT NULL, `conceptId` TEXT NOT NULL, `languageCode` TEXT NOT NULL, `text` TEXT NOT NULL, `canonicalKey` TEXT NOT NULL, `notes` TEXT, `pronunciation` TEXT, `example` TEXT, `translationIndex` INTEGER NOT NULL, `grammarNote` TEXT, `possibleCorrection` TEXT, PRIMARY KEY(`id`))")
        db.execSQL("CREATE INDEX `index_contents_new_languageCode_canonicalKey` ON `contents_new` (`languageCode`, `canonicalKey`)")
        db.execSQL("INSERT INTO `contents_new` (`id`,`conceptId`,`languageCode`,`text`,`canonicalKey`,`notes`,`pronunciation`,`example`,`translationIndex`,`grammarNote`,`possibleCorrection`) SELECT `id`,`conceptId`,`languageCode`,`text`,`canonicalKey`,`notes`,`pronunciation`,`example`,0,NULL,NULL FROM `contents`")
        db.execSQL("DROP TABLE `contents`")
        db.execSQL("ALTER TABLE `contents_new` RENAME TO `contents`")
        db.execSQL("CREATE UNIQUE INDEX `index_contents_conceptId_languageCode_translationIndex` ON `contents` (`conceptId`, `languageCode`, `translationIndex`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `vocabulary_relations` (`id` TEXT NOT NULL, `sourceConceptId` TEXT NOT NULL, `targetConceptId` TEXT, `relationType` TEXT NOT NULL, `unresolvedText` TEXT, PRIMARY KEY(`id`))")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_vocabulary_relations_sourceConceptId_targetConceptId_relationType` ON `vocabulary_relations` (`sourceConceptId`, `targetConceptId`, `relationType`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_vocabulary_relations_targetConceptId` ON `vocabulary_relations` (`targetConceptId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `vocabulary_variants` (`id` TEXT NOT NULL, `conceptId` TEXT NOT NULL, `text` TEXT NOT NULL, `variantType` TEXT NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_vocabulary_variants_conceptId_text_variantType` ON `vocabulary_variants` (`conceptId`, `text`, `variantType`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `review_queue` (`id` TEXT NOT NULL, `conceptId` TEXT, `sourceText` TEXT NOT NULL, `targetText` TEXT, `confidence` REAL NOT NULL, `possibleCorrection` TEXT, `status` TEXT NOT NULL, `lineNumber` INTEGER, `warning` TEXT, PRIMARY KEY(`id`))")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_queue_status_confidence` ON `review_queue` (`status`, `confidence`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `languages` (`code` TEXT NOT NULL, `name` TEXT NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`code`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `language_pairs` (`sourceLanguageCode` TEXT NOT NULL, `targetLanguageCode` TEXT NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`sourceLanguageCode`, `targetLanguageCode`))")
    }
}
