package com.example.com

import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.password4j.Password

// Simple in-memory search state
object SearchState {
    var message: String = ""
}

// Simple in-memory user state (for demo purposes)
object UserState {
    var loggedIn: Boolean = false
    var username: String = ""
    var password: String = ""
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondTemplate("search.peb", mapOf(
                "message" to SearchState.message,
                "loggedIn" to UserState.loggedIn
            ))
        }

        post("/search-title") {
            val params = call.receiveParameters()
            val usrInput = params["usrInput"]

            if (!usrInput.isNullOrBlank()) {
                val results = BookSearchAuthor(usrInput)
                call.respondTemplate("search.peb", mapOf(
                    "results" to results,
                ))

            } else {
                SearchState.message = "Please enter a title to search."
                call.respondRedirect("/")
            }
        }

        get("/login") {
            call.respondTemplate("login.peb", mapOf(
                "loggedIn" to UserState.loggedIn,
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
                    UserState.loggedIn = true
                    UserState.username = username.orEmpty()
                    UserState.password = password.orEmpty()
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
            if (UserState.loggedIn) {
                call.respondTemplate("profile.peb", mapOf(
                    "loggedIn" to UserState.loggedIn,
                    "username" to UserState.username,
                    "password" to UserState.password
                ))
            } else {
                call.respondRedirect("/login")
            }
        }

        get("/logout") {
            UserState.loggedIn = false
            UserState.username = ""
            call.respondRedirect("/")
        }
        
        get("/see-all-books") {
            val books = getAllBooks()
            call.respondTemplate("seeAllBooks.peb", mapOf(
                "books" to books,
                "loggedIn" to UserState.loggedIn
            ))
        }

        get("/remove-account") {
            removeUser(UserState.username)
            UserState.loggedIn = false
            UserState.username = ""
            UserState.password = ""
            call.respondRedirect("/")
        }
    }
}
