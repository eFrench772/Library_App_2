package com.example.com

import org.jetbrains.exposed.sql.Table

object Books : Table() {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val author = varchar("author", 255)
    val isbn13 = varchar("isbn_13", 20).nullable()
    val formatCode = varchar("format_code", 10)
    val locationCode = varchar("location_code", 20)
    val notes = varchar("notes", 500).nullable()

    override val primaryKey = PrimaryKey(id)
}
