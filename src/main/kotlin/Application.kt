package com.example.com

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll
import org.h2.tools.Server

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()
    configureTemplates()
    configureRouting()
    configureSessions()
    Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start()

    routing {
        get("/debug/users") {
            val users = transaction {
                Users.selectAll().map {
                    mapOf(
                        "username" to it[Users.username],
                        "email" to it[Users.email],
                        "role" to it[Users.role]
                    )
                }
            }
            call.respond(users)
        }
    }
}