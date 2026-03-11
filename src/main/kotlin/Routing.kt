package com.example.com

import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import com.password4j.Password

fun Application.configureRouting() {
    routing {
        get("/") {
            val session = call.sessions.get<UserSession>()
            call.respondTemplate("search.peb", getSessionData(call))
            // Clear message after displaying
            if (session != null && session.message.isNotEmpty()) {
                call.sessions.set(session.copy(message = ""))
            }
        }

        post("/search-title") {
            val params = call.receiveParameters()
            val usrInput = params["usrInput"]

            if (!usrInput.isNullOrBlank()) {
                val results = BookSearch(usrInput)
                call.respondTemplate("search.peb", getSessionData(call) + mapOf(
                    "results" to results
                ))
            } else {
                val session = call.sessions.get<UserSession>() ?: UserSession()
                call.sessions.set(session.copy(message = "Please enter a title to search."))
                call.respondRedirect("/")
            }
        }

        get("/login") {
            call.respondTemplate("login.peb", mapOf(
                "error" to ""
            ))
        }

        post("/login") {
            val params = call.receiveParameters()
            val username = params["username"]
            val password = params["password"]

            val storedHash = getUserHashPassword(username.orEmpty())

            if (storedHash == null) {
                call.respondTemplate("login.peb", mapOf(
                    "loggedIn" to false,
                    "error" to "Username not found."
                ))
            }else {
                val passwordMatches = Password.check(password, storedHash).withScrypt()
                if (passwordMatches) {
                    call.sessions.set(UserSession(username = username.orEmpty(), loggedIn = true))
                    call.respondRedirect("/")
                } else {
                    call.respondTemplate("login.peb", mapOf(
                        "loggedIn" to false,
                        "error" to "Incorrect password."
                    ))
                }
            }    
        }

        get("/register") {
            call.respondTemplate("register.peb", mapOf(
                "error" to ""
            ))
        }

        post("/register") {
            val params = call.receiveParameters()
            val username = params["username"].orEmpty()
            val email = params["email"].orEmpty()
            val password = params["password"].orEmpty()
            val homeAddress = params["homeAddress"].orEmpty()

            val takenUsername = checkUsernameExists(username)
            if (!takenUsername && !username.isNullOrBlank() ) {
                addUser(username, email, password, homeAddress)
                call.respondRedirect("/login")
            } else {
                call.respondTemplate("register.peb", mapOf(
                    "error" to "This Username is taken."
                ))
            }
        }

        get("/profile") {
            val session = call.sessions.get<UserSession>()
            if (session != null && session.loggedIn) {
                val loans = getUserLoans(session.username)
                call.respondTemplate("profile.peb", getSessionData(call) + mapOf("loans" to loans))
            } else {
                call.respondRedirect("/login")
            }
        }

        get("/logout") {
            call.sessions.clear<UserSession>()
            call.respondRedirect("/")
        }
        
        get("/see-all-books") {
            val books = getAllBooks()
            call.respondTemplate("seeAllBooks.peb", getSessionData(call) + mapOf(
                "books" to books
            ))
        }

        get("/remove-account") {
            val session = call.sessions.get<UserSession>()
            if (session != null && session.loggedIn) {
                removeUser(session.username)
                call.sessions.clear<UserSession>()
            }
            call.respondRedirect("/")
        }

        get("/book/{isbn}") {
            val isbn = call.parameters["isbn"]
            val books = BookSearchISBN(isbn ?: "")
            call.respondTemplate("book.peb", getSessionData(call) + mapOf(
                "books" to books,
                "isAdmin" to isUserAdmin(call.sessions.get<UserSession>()?.username ?: "")
            ))
        }

        post("/borrow/{bookId}") {
            val session = call.sessions.get<UserSession>()
            if (session == null || !session.loggedIn) {
                call.respondRedirect("/login")
                return@post
            }
            val bookId = call.parameters["bookId"]?.toIntOrNull()
            if (bookId != null) {
                borrowBook(bookId, session.username)
                call.sessions.set(session.copy(message = "Book borrowed successfully! Due in 2 weeks."))
            }
            call.respondRedirect("/")
        }

        post("/reserve/{bookId}") {
            val session = call.sessions.get<UserSession>()
            if (session == null || !session.loggedIn) {
                call.respondRedirect("/login")
                return@post
        }
            val bookId = call.parameters["bookId"]?.toIntOrNull()
            if (bookId != null) {
                reserveBook(bookId, session.username)
                call.sessions.set(session.copy(message = "Book reserved successfully!"))
            }
            call.respondRedirect("/")
        }

        post("/return/{bookId}") {
            val session = call.sessions.get<UserSession>()
            if (session == null || !session.loggedIn) {
                call.respondRedirect("/login")
                return@post
            }
            val bookId = call.parameters["bookId"]?.toIntOrNull()
            if (bookId != null) {
                returnBook(bookId, session.username)
                call.sessions.set(session.copy(message = "Book returned successfully!"))
            }
            call.respondRedirect("/profile")
        }

        post("/admin/return/{bookId}") {
            val session = call.sessions.get<UserSession>()
            if (session == null || !session.loggedIn) {
                call.respondRedirect("/login")
                return@post
            }
            if (!isUserAdmin(session.username)) {
                call.respondRedirect("/")
                return@post
            }
            val bookId = call.parameters["bookId"]?.toIntOrNull()
            if (bookId != null) {
                adminReturnBook(bookId)
                call.sessions.set(session.copy(message = "Book marked as returned."))
            }
            call.respondRedirect("/")
        }
    }
}

