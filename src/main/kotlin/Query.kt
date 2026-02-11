package com.example.com

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


fun getAllBooks(): List<Book> = transaction {
    Books.selectAll().map {
        Book(
            id = it[Books.id],
            title = it[Books.title],
            author = it[Books.author],
            isbn13 = it[Books.isbn13],
            formatCode = it[Books.formatCode],
            locationCode = it[Books.locationCode],
            notes = it[Books.notes]
        )
    }
}

data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val isbn13: String?,
    val formatCode: String,
    val locationCode: String,
    val notes: String?
)

