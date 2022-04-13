package com.alignTech.labelsPrinting.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

  val MIGRATION_0_1: Migration = object : Migration(0, 1) {
    override fun migrate(database: SupportSQLiteDatabase) {
    }
  }

}