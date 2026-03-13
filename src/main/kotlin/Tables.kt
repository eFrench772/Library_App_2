package com.example.com

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

object Books : Table() {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val author = varchar("author", 255)
    val isbn13 = varchar("isbn_13", 20).nullable()
    val formatCode = varchar("format_code", 10)
    val locationCode = varchar("location_code", 20)
    val notes = varchar("notes", 500).nullable()
    val isAvailable = bool("is_available").default(true)

    override val primaryKey = PrimaryKey(id)
}

object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 255)
    val email = varchar("email", 255)
    val passwordHash = varchar("password_hash", 255)
    val role = bool("role")
    val homeAddress = varchar("home_address", 500).nullable()

    override val primaryKey = PrimaryKey(id)
}

object Loans : Table() {
    val id = integer("id").autoIncrement()
    val bookId = integer("book_id").references(Books.id)
    val userId = integer("user_id").references(Users.id)
    val borrowedDate = date("borrowed_date")
    val dueDate = date("due_date")
    val returnedDate = date("returned_date").nullable()

    override val primaryKey = PrimaryKey(id)
}

object Reservations : Table() {
    val id = integer("id").autoIncrement()
    val isbn = varchar("isbn", 20)
    val userId = integer("user_id").references(Users.id)
    val reservedDate = date("reserved_date")
    val fulfilledDate = date("fulfilled_date").nullable()

    override val primaryKey = PrimaryKey(id)
}

