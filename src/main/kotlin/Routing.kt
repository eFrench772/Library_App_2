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
            val role = params["role"] == "true"

            val takenUsername = checkUsernameExists(username)
            if (!takenUsername && !username.isNullOrBlank() ) {
                addUser(username, email, password, role)
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
                call.respondTemplate("profile.peb", getSessionData(call))
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
            val Books = BookSearchISBN(isbn ?: "")
            
            // use book ids for faster search up of if reserved or not 
            val bookIds = mutableListOf<Int>()
            for (book in Books) {
                bookIds.add(book.id)
            }

            call.respondTemplate("book.peb", getSessionData(call) + mapOf(
                "books" to Books
            ))
        }
    }
}
