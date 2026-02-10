package com.example.com

import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// Simple in-memory search state
object SearchState {
    var message: String = ""
    var results: List<String> = emptyList()
}

// Simple in-memory user state (for demo purposes)
object UserState {
    var loggedIn: Boolean = false
    var username: String = ""
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondTemplate("search.peb", mapOf(
                "message" to SearchState.message,
                "results" to SearchState.results,
                "loggedIn" to UserState.loggedIn
            ))
        }

        post("/search-title") {
            val params = call.receiveParameters()
            val title = params["title"]

            if (!title.isNullOrBlank()) {
                SearchState.message = "You searched for: $title"
                // TODO: Add actual book search logic here
                SearchState.results = emptyList()
            } else {
                SearchState.message = "Please enter a title to search."
                SearchState.results = emptyList()
            }

            call.respondRedirect("/")
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

            // Simple demo login (accepts any non-empty username/password)
            if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
                UserState.loggedIn = true
                UserState.username = username
                call.respondRedirect("/")
            } else {
                call.respondTemplate("login.peb", mapOf(
                    "loggedIn" to false,
                    "error" to "Please enter username and password."
                ))
            }
        }

        get("/profile") {
            if (UserState.loggedIn) {
                call.respondTemplate("profile.peb", mapOf(
                    "loggedIn" to UserState.loggedIn,
                    "username" to UserState.username
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
    }
}
