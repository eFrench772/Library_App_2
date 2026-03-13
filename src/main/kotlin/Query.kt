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
    val notes: String?,
    val isAvailable: Boolean = true
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
                notes = it[Books.notes],
                isAvailable = it[Books.isAvailable]
            )
        }
    }
}

fun BookSearch(query: String): List<Book> {
    val trimmed = query.trim()
    val isIsbn = trimmed.all { it.isDigit() } && (trimmed.length == 10 || trimmed.length == 13)

    if (isIsbn) {
        return BookSearchISBN(trimmed)
    } else {
        val titleResults = BookSearchTitle(trimmed)
        val authorResults = BookSearchAuthor(trimmed)
        return (titleResults + authorResults).distinctBy { it.id }
    }
}

fun BookSearchTitle(title: String): List<Book> {
    return transaction {
        Books.selectAll()
            .where { Books.title.lowerCase() like "%${title.lowercase()}%" }
            .map {
                Book(
                    id = it[Books.id],
                    title = it[Books.title],
                    author = it[Books.author],
                    isbn13 = it[Books.isbn13],
                    formatCode = it[Books.formatCode],
                    locationCode = it[Books.locationCode],
                    notes = it[Books.notes],
                    isAvailable = it[Books.isAvailable]
                )
            }
    }
}

fun BookSearchAuthor(author: String): List<Book> {
    return transaction {
        Books.selectAll()
            .where { Books.author.lowerCase() like "%${author.lowercase()}%" }
            .map {
                Book(
                    id = it[Books.id],
                    title = it[Books.title],
                    author = it[Books.author],
                    isbn13 = it[Books.isbn13],
                    formatCode = it[Books.formatCode],
                    locationCode = it[Books.locationCode],
                    notes = it[Books.notes],
                    isAvailable = it[Books.isAvailable]
                )
            }
    }
}

fun BookSearchISBN(isbn: String): List<Book> {
    return transaction {
        Books.selectAll()
            .where { Books.isbn13 eq isbn }
            .map {
                Book(
                    id = it[Books.id],
                    title = it[Books.title],
                    author = it[Books.author],
                    isbn13 = it[Books.isbn13],
                    formatCode = it[Books.formatCode],
                    locationCode = it[Books.locationCode],
                    notes = it[Books.notes],
                    isAvailable = it[Books.isAvailable]
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

fun addUser(username: String, email: String, password: String, homeAddress: String) {
    transaction {
        Users.insert {
            it[Users.username] = username
            it[Users.email] = email
            it[Users.passwordHash] = Password.hash(password).addRandomSalt(8).withScrypt().result
            it[Users.role] = false
            it[Users.homeAddress] = homeAddress
        }
    }
}

fun removeUser(username: String) {
    transaction {
        Users.deleteWhere { Users.username eq username }
    }
}

fun getUserId(username: String): Int? {
    return transaction {
        Users.selectAll().where { Users.username eq username }
            .singleOrNull()?.get(Users.id)
    }
}

fun borrowBook(bookId: Int, username: String) {
    val userId = getUserId(username) ?: return
    transaction {
        Books.update({ Books.id eq bookId }) {
            it[isAvailable] = false
        }
        Loans.insert {
            it[Loans.bookId] = bookId
            it[Loans.userId] = userId
            it[borrowedDate] = java.time.LocalDate.now()
            it[dueDate] = java.time.LocalDate.now().plusWeeks(2)
        }
    }
}

fun reserveBook(isbn: String, username: String) {
    val userId = getUserId(username) ?: return
    transaction {
        Reservations.insert {
            it[Reservations.isbn] = isbn
            it[Reservations.userId] = userId
            it[reservedDate] = java.time.LocalDate.now()
        }
    }
}

data class LoanDetails(
    val bookId: Int,
    val bookTitle: String,
    val bookAuthor: String,
    val borrowedDate: String,
    val dueDate: String
)

fun getUserLoans(username: String): List<LoanDetails> {
    val userId = getUserId(username) ?: return emptyList()
    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return transaction {
        (Loans innerJoin Books)
            .selectAll()
            .where { (Loans.userId eq userId) and (Loans.returnedDate.isNull()) }
            .map {
                LoanDetails(
                    bookId = it[Books.id],
                    bookTitle = it[Books.title],
                    bookAuthor = it[Books.author],
                    borrowedDate = it[Loans.borrowedDate].format(formatter),
                    dueDate = it[Loans.dueDate].format(formatter)
                )
            }
    }
}

fun returnBook(bookId: Int, username: String) {
    val userId = getUserId(username) ?: return
    transaction {
        Loans.update({ (Loans.bookId eq bookId) and (Loans.userId eq userId) and (Loans.returnedDate.isNull()) }) {
            it[returnedDate] = java.time.LocalDate.now()
        }
    }
    val isbn = transaction { Books.selectAll().where { Books.id eq bookId }.singleOrNull()?.get(Books.isbn13) }
    if (!fulfillNextReservation(bookId, isbn)) {
        transaction {
            Books.update({ Books.id eq bookId }) {
                it[isAvailable] = true
            }
        }
    }
}

fun isUserAdmin(username: String): Boolean {
    if (username.isEmpty()) return false
    return transaction {
        Users.selectAll().where { Users.username eq username }
            .singleOrNull()?.get(Users.role) ?: false
    }
}

fun adminReturnBook(bookId: Int) {
    transaction {
        Loans.update({ (Loans.bookId eq bookId) and (Loans.returnedDate.isNull()) }) {
            it[returnedDate] = java.time.LocalDate.now()
        }
    }
    val isbn = transaction { Books.selectAll().where { Books.id eq bookId }.singleOrNull()?.get(Books.isbn13) }
    if (!fulfillNextReservation(bookId, isbn)) {
        transaction {
            Books.update({ Books.id eq bookId }) {
                it[isAvailable] = true
            }
        }
    }
}

fun hasUserReservedIsbn(isbn: String, username: String): Boolean {
    val userId = getUserId(username) ?: return false
    return transaction {
        Reservations.selectAll()
            .where { (Reservations.isbn eq isbn) and (Reservations.userId eq userId) and (Reservations.fulfilledDate.isNull()) }
            .count() > 0
    }
}

fun fulfillNextReservation(bookId: Int, isbn: String?): Boolean {
    if (isbn == null) return false
    return transaction {
        // find oldest unfulfilled reservation for this isbn
        val reservation = Reservations.selectAll()
            .where { (Reservations.isbn eq isbn) and (Reservations.fulfilledDate.isNull()) }
            .orderBy(Reservations.reservedDate, SortOrder.ASC)
            .firstOrNull() ?: return@transaction false

        val userId = reservation[Reservations.userId]

        // mark book as unavailable and create loan
        Books.update({ Books.id eq bookId }) {
            it[isAvailable] = false
        }
        Loans.insert {
            it[Loans.bookId] = bookId
            it[Loans.userId] = userId
            it[borrowedDate] = java.time.LocalDate.now()
            it[dueDate] = java.time.LocalDate.now().plusWeeks(2)
        }

        // mark reservation as fulfilled
        Reservations.update({ Reservations.id eq reservation[Reservations.id] }) {
            it[fulfilledDate] = java.time.LocalDate.now()
        }
        true
    }
}