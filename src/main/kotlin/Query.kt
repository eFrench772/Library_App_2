package com.example.com

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import com.password4j.Password

//Books
data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val isbn13: String?,
    val formatCode: String,
    val locationCode: String,
    val notes: String?
)

fun getAllBooks(): List<Book> {
    return transaction {
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
}


//Users
fun checkUsernameExists(username: String): Boolean {
    return transaction {
        Users.selectAll().where { Users.username eq username }.count() > 0
    }
}

fun getUserHashPassword(username: String): String? {
    return transaction {
        val user = Users.selectAll().where { Users.username eq username }.singleOrNull()
        user?.get(Users.passwordHash)
    }
}

fun addUser(username: String, email: String, password: String, role: Boolean) {
    transaction {
        Users.insert {
            it[Users.username] = username
            it[Users.email] = email
            it[Users.passwordHash] = Password.hash(password).addRandomSalt(8).withScrypt().result
            it[Users.role] = role
        }
    }
}

fun removeUser(username: String) {
    transaction {
        Users.deleteWhere { Users.username eq username }
    }
}