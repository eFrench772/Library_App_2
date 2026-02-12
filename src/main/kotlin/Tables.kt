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

object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 255)
    val email = varchar("email", 255)
    val passwordHash = varchar("password_hash", 255)
    val role = bool("role")

    override val primaryKey = PrimaryKey(id)
}

object Loans : Table() {
    val id = integer("id").autoIncrement()
    val bookId = integer("book_id").references(Books.id)
    val userId = integer("user_id").references(Users.id)
    val loanDate = varchar("loan_date", 20)      // when they borrowed it
    val dueDate = varchar("due_date", 20)        // when it's due back
    val returned = bool("returned").default(false)

    override val primaryKey = PrimaryKey(id)
}