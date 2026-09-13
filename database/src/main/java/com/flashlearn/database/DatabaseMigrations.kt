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
